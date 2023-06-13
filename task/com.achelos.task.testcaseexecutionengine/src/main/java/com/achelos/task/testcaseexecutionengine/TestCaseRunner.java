package com.achelos.task.testcaseexecutionengine;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.abstracttestsuite.ExecutionControl;
import com.achelos.task.abstracttestsuite.Summary;
import com.achelos.task.abstracttestsuite.TestCaseRun;
import com.achelos.task.abstracttestsuite.TestSuiteRun;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * Test case runner
 */
public class TestCaseRunner {

	private final static String LOCAL_TESTCASE_PREFIX = "com.achelos.task.tr03116ts.testcases.";
	private final static String JAR_FILE_ENDING = ".jar";

	private final LoggingConnector logger;

	/**
	 * Constructor setting the LoggingConnector to use when running test cases.
	 */
	public TestCaseRunner() {
		this.logger = LoggingConnector.getInstance();
	}

	/**
	 * Execute a TestSuiteRun.
	 * @param testSuite The Test Suite to execute.
	 */
	public void executeTestCases(final TestSuiteRun testSuite) {
		testSuite.setStartTime();
		logger.tellLogger(BasicLogger.MSG_NEW_TESTSUITE, testSuite);
		var testcases = testSuite.getTestCases();
		int totalNoOfTestcases = testcases.size();
		List<AbstractTestCase> testcasesClasses = getTestcaseClassesByNames(testcases, TestRunPlanConfiguration.getInstance());

		try {
			ExecutionControl executionControl = ExecutionControl.getInstance();
			List<TestCaseRun> testCaseRuns = executionControl.addAll(testcasesClasses);
			Summary.printTestSuiteSummary(testCaseRuns, "TR-03116-TS Testcases", totalNoOfTestcases);
		} catch (Exception e) {
			logger.error("Error occurred while executing the test case", e);
		}
		testSuite.setEndTime();
		logger.tellLogger(BasicLogger.MSG_TESTSUITE_ENDED, testSuite);
	}

	private List<AbstractTestCase> getTestcaseClassesByNames(final List<String> testCaseNames, TestRunPlanConfiguration configuration) {
		List<AbstractTestCase> testcaseClasses = new ArrayList<>();
		try {
			var jarUrlList = getJarUrls(configuration.getAdditionalTestSuiteJars());
			ClassLoader classLoader;
			if (jarUrlList.length == 0) {
				classLoader = getClass().getClassLoader();
			} else {
				logger.info("TaSK: Additional Testsuite JARS where provided: " + String.join(", ", configuration.getAdditionalTestSuiteJars()));
				classLoader = new URLClassLoader(jarUrlList, getClass().getClassLoader());
			}
			var testCaseMap = loadTestSuitesFromJar(configuration.getAdditionalTestSuiteJars(), classLoader);
			for (var testCaseName : testCaseNames) {
				String testCaseFQName;
				if (testCaseMap.containsKey(testCaseName)) {
					testCaseFQName = testCaseMap.get(testCaseName);
				} else {
					testCaseFQName = getTestCaseClassPath(testCaseName, LOCAL_TESTCASE_PREFIX);
				}
				try {
					var testCaseImpl = getAbstractTestcaseImplForClass(testCaseFQName, classLoader);
					testcaseClasses.add(testCaseImpl);
				} catch (Exception e) {
					logger.error("TaSK: Error loading Test Case " + testCaseName + " from " + testCaseFQName, e);
				}
			}
		} catch (Exception e) {
			logger.error("TaSK: Error trying to load Test Cases.", e);
		}
		return testcaseClasses;
	}

	private URL[] getJarUrls(List<String> testSuiteJars) {
		var urlList = new ArrayList<URL>();
		for (var testSuiteJar : testSuiteJars) {
			var jarFile = new File(testSuiteJar);
			try {
				urlList.add(jarFile.toURI().toURL());
			} catch (Exception e) {
				logger.warning("TaSK: Unable to add JAR " + jarFile + " to the classloading mechanism.");
				logger.debug("TaSK: " + e.getMessage());
			}
		}
		return urlList.toArray(new URL[]{});
	}

	private HashMap<String, String> loadTestSuitesFromJar(List<String> testSuiteJars,  ClassLoader cl) {
		var testCasesMap = new HashMap<String, String>();

		for (var testSuiteJarName : testSuiteJars) {
			if (testSuiteJarName == null || !testSuiteJarName.endsWith(JAR_FILE_ENDING)) {
				continue;
			}
			try (var testSuiteJar = new JarFile(testSuiteJarName)) {
				for (var entryEnumeration = testSuiteJar.entries(); entryEnumeration.hasMoreElements();) {
					var entryName = entryEnumeration.nextElement().getName();
					if (!entryName.endsWith(".class")) {
						continue;
					}
					var classLoadingName = entryName.substring(0,entryName.length() - 6).replaceAll("/|\\\\", "\\.");
					var clazz = Class.forName(classLoadingName, false, cl);
					if (!AbstractTestCase.class.isAssignableFrom(clazz)) {
						continue;
					}
					var clazzName = classLoadingName.substring(classLoadingName.lastIndexOf(".") + 1);
					if (testCasesMap.containsKey(clazzName)) {
						var warningMessage = "TaSK: Multiple implementations for Test Case with Name: " + clazzName + " Skipping implementation from: " +  testSuiteJarName + ": " + entryName + "\nUsing implementation from " + testCasesMap.get(clazzName);
						logger.warning(warningMessage);
						continue;
					}
					testCasesMap.put(clazzName, classLoadingName);
				}
			} catch (Exception e) {
				logger.error("TaSK: Error loading Test Suite Jar: " + testSuiteJarName, e);
			}
		}

		return testCasesMap;
	}

	private String getTestCaseClassPath(final String testCaseClassName, final String prefix) {
		String moduleAlphabet = testCaseClassName.substring(4, 5).toLowerCase().concat(".");
		String moduleName = testCaseClassName.substring(4, 10).toLowerCase().replace("_", ".");
		return prefix.concat(moduleAlphabet).concat(moduleName).concat(testCaseClassName);
	}
	
	private AbstractTestCase getAbstractTestcaseImplForClass(final String testCase, final ClassLoader classLoader) throws Exception {
		// Load the class
		Class<?> clazz = classLoader.loadClass(testCase);
		
		// Initialize the class constructor.
		Object instance = clazz.getDeclaredConstructor().newInstance();
		if (!(instance instanceof AbstractTestCase)) {
			throw new RuntimeException("Test Case Object with Name " + testCase + " is not of type " + AbstractTestCase.class.getName());
		}
		return (AbstractTestCase) instance;
	}

}