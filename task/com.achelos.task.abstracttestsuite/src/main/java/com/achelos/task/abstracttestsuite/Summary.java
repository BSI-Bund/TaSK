package com.achelos.task.abstracttestsuite;

import java.util.ArrayList;
import java.util.List;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.DateTimeUtils;


/**
 * Class representing a summary of test case runs.
 */
public class Summary {

	private final List<TestCaseRun> testCaseRuns;
	private final String testSuiteDescription;

	/**
	 * Default constructor for the summary of the test case runs.
	 * Sets the provided test case runs list or creates an empty test case runs list if the provided list is null. 
	 * @param testCaseRuns a list of test case runs to represent.
	 * @param testSuiteDescription the description of the test suite for summary.
	 */
	public Summary(final List<TestCaseRun> testCaseRuns, final String testSuiteDescription) {
		if (testCaseRuns != null) {
			this.testCaseRuns = new ArrayList<>(testCaseRuns);
		} else {
			this.testCaseRuns = new ArrayList<>();
		}
		this.testSuiteDescription = testSuiteDescription;
	}

	/**
	 * Gets the list of test case runs to represent.
	 * @return a list of test case runs to represent.
	 */
	public List<TestCaseRun> getTestCaseRuns() {
		return new ArrayList<>(testCaseRuns);
	}

	/**
	 * Gets the description of the test suite.
	 * @return the description of the test suite.
	 */
	public String getTestSuiteDescription() {
		return testSuiteDescription;
	}

	/**
	 * This method checks for errors in the test case runs list and 
	 * Returns {@code true} if and only if none of the test case runs were failed.
	 * 
	 * @return {@code true} if all test case runs were successful, {@code false} otherwise. 
	 */
	public boolean wasSuccessful() {
		for (var testRun : testCaseRuns) {
			if (testRun.getFatalErrorCount() + testRun.getErrorCount() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prints a test suite summary to the logger.
	 */
	public void printTestSuiteSummary() {
		printTestSuiteSummary(getTestCaseRuns(), getTestSuiteDescription(), getTestCaseRuns().size());
	}

	/**
	 * Prints a test suite summary to the logger.
	 *
	 * @param testCaseRuns the list of test case runs.
	 * @param testSuiteDescription the test suite description.
	 * @param totalNoOfTestcases the total number of test cases.
	 */
	public static void printTestSuiteSummary(final List<TestCaseRun> testCaseRuns, final String testSuiteDescription,
			final int totalNoOfTestcases) {
		var logger = LoggingConnector.getInstance();

		logger.info("===============================================");
		logger.info("Summary of " + testSuiteDescription + ":");
		logger.info("Created: " + DateTimeUtils.getISOFormattedTimeStamp());
		int passed = 0, failed = 0, warnings = 0;

		for (TestCaseRun testCaseRun : testCaseRuns) {
			if (testCaseRun.getErrorCount() + testCaseRun.getFatalErrorCount() > 0) {
				failed++;
			} else if (testCaseRun.getWarningCount() > 0) {
				warnings++;
			} else {
				passed++;
			}
		}
		logger.info("Total no. of test cases: " + totalNoOfTestcases);
		logger.info("Executed test cases: " + testCaseRuns.size());
		logger.info("Passed: " + passed);
		logger.info("Passed with warning(s): " + warnings);
		logger.info("Failed: " + failed);

		for (TestCaseRun tcr : testCaseRuns) {
			printTestCaseResult(tcr, logger);
		}
		logger.info("===============================================");

	}

	/**
	 * Print the result of a single test case run to the logger.
	 *
	 * @param testCaseRun the test case run to get the result from.
	 * @param logger the logger to write to.
	 */
	private static void printTestCaseResult(final TestCaseRun testCaseRun, final LoggingConnector logger) {
		var stringBuilder = new StringBuilder(testCaseRun.getTestCaseName() + ": ");

		if (testCaseRun.getErrorCount() + testCaseRun.getFatalErrorCount() > 0) {
			stringBuilder.append("FAILED");
		} else if (testCaseRun.getWarningCount() > 0) {
			stringBuilder.append("PASSED (with warnings)");
		} else {
			stringBuilder.append("PASSED");
		}
		if (testCaseRun.getStatusMessages() != null && !testCaseRun.getStatusMessages().isEmpty()) {
			for (var statusMessage : testCaseRun.getStatusMessages()) {
				stringBuilder.append(System.lineSeparator());
				stringBuilder.append("Status Message: ");
				stringBuilder.append(statusMessage);
			}
		}

		logger.info(stringBuilder.toString());
	}
}
