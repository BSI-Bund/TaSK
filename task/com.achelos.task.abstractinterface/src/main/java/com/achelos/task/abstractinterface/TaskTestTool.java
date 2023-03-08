package com.achelos.task.abstractinterface;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.achelos.task.abstracttestsuite.TestSuiteRun;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.logging.ReportDutInfoFields;
import com.achelos.task.logging.ReportMetadataFields;
import com.achelos.task.micsverifier.MICSVerifier;
import com.achelos.task.testcaseexecutionengine.TestCaseRunner;
import com.achelos.task.testsuitesetup.TestSuiteSetup;
import com.achelos.task.utilities.DateTimeUtils;
import com.achelos.task.utilities.FileUtils;
import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigChecker;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameterNames;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.xmlparser.datastructures.mics.MICS;

import jakarta.xml.bind.DatatypeConverter;


/**
 * Class abstracting the execution of the TaSK Test Tool and splitting it from the GUI/CLI.
 */
public class TaskTestTool {

	private final MICSVerifier micsVerifier;
	private final HashMap<String, GlobalConfigParameter> globalConfiguration;
	private final LoggingConnector logger;

	/**
	 * Constructor for the MICS file execution mode.
	 *
	 * @param logger The {@link LoggingConnector} to use.
	 * @param configFile The global configuration file
	 * @throws Exception if an error occurs while initializing the MICS verifier.
	 */
	private TaskTestTool(final LoggingConnector logger, final File configFile)
																				throws Exception {
		this.logger = logger;

		this.logger.info("TaSK: Parsing the global configuration file.");
		globalConfiguration = ConfigParser.parseGlobalConfig(configFile);
		if (globalConfiguration.isEmpty()) {
			throw new Exception("Unable to parse the global configuration file.");
		}
		this.logger.info("TaSK: Successfully parsed the global configuration file.");

		this.logger.debug("TaSK: Trying to read specification directory from the global configuration file.");
		var specificationDir = new File(GlobalConfigChecker
				.getGlobalConfigParameter(globalConfiguration, GlobalConfigParameterNames.SpecificationDirectory)
				.getValueAsString());
		if (!specificationDir.exists()) {
			throw new Exception("Specification directory contained in the global configuration file does not exist.");
		}
		this.logger.debug("TaSK: Successfully read specification directory from the global configuration file.");

		this.logger.info("TaSK: Initializing the MICS verifier.");
		// Modules 1 and 2
		// Parse the global configuration file
		// Parse the test profiles
		// Parse the Test case definitions (Specifications in the xml format)
		// Parse the application specific profiles
		// Parse the TR specifications
		micsVerifier = initializeMicsVerifier(specificationDir, configFile);
		if (micsVerifier == null) {
			throw new Exception("Unable to initialize the MICS verifier.");
		}
		logger.info("TaSK: Initialization of the MICS verifier successful.");

		var testerInCharge
				= globalConfiguration.containsKey(GlobalConfigParameterNames.TesterInCharge.getParameterName())
						? globalConfiguration.get(GlobalConfigParameterNames.TesterInCharge.getParameterName())
								.getValueAsString()
						: "/";
		var entry = new AbstractMap.SimpleEntry<>(
				ReportMetadataFields.TESTER_IN_CHARGE, testerInCharge);
		logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		var systemName = "localhost";
		try {
			systemName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warning("UnknownHostName: " + e.getMessage());
		}
		entry = new AbstractMap.SimpleEntry<>(
				ReportMetadataFields.EXECUTION_MACHINE_NAME, systemName);
		logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		entry = new AbstractMap.SimpleEntry<>(
				ReportMetadataFields.EXECUTION_TYPE, "Executed via the MICS file");
		logger.tellLogger(BasicLogger.MSG_METADATA, entry);
	}

	private void executeTaskTestTool(final File micsFile, final List<File> certificateFileList,
			final boolean ignoreMicsVerification, final String reportDirectory, final String clientAuthCertChainFile, final String clientAuthKeyFile) {

		// Check if the MICS verifier has been initialized.
		if (micsVerifier == null) {
			logger.error("TaSK: Cannot  execute the TaSK framework. The MICS verifier has not been initialized.");
			return;
		}

		// Start of Phase I: Create the TRP
		// Module 4: Parse the MICS document
		MICS mics = null;
		try {
			logger.info("TaSK: Parsing the MICS file.");
			logDutInputFile(logger, micsFile);
			mics = micsVerifier.parseMICS(micsFile);
			if (mics == null) {
				throw new Exception("TaSK: Parsing of the MICS file failed.");
			}
			logDutInformation(logger, mics);
			logger.info("TaSK: Parsing of the MICS file successful.");
		} catch (Exception e) {
			logger.error("TaSK: Unable to parse the MICS file.", e);
			return;
		}
		// Module 5: Verify the DUT
		// Module 6: application profile Verifier
		// Module 7: Checklist verifier
		// Module 8: X.509 Certificate verifier
		try {
			logger.info("TaSK: Verifying the parsed MICS file.");
			var result = micsVerifier.verifyMICS(mics, certificateFileList.toArray(File[]::new));
			if (!result) {
				if (!ignoreMicsVerification) {
					throw new Exception("Verification of the MICS file failed.");
				}
				logger.error("TaSK: Verification of the MICS file failed.");
			} else {
				logger.info("TaSK: Verification of the parsed MICS file is successful.");
			}

		} catch (Exception e) {
			logger.error("TaSK: Unable to verify the MICS file.", e);
			return;
		}

		// Module 9: Initialize the TRP Parameters
		// Module 10: Add test cases to the TRP
		// At this stage the TRP should be created
		File testRunPlanFile = null;
		try {
			logger.info("TaSK: Generating the test run plan.");
			logger.debug("TaSK: Initializing the test run plan parameters.");
			logger.debug("TaSK: Adding test cases to the test run plan.");
			testRunPlanFile = TestSuiteSetup.executeTestSuiteSetup(mics, micsFile, micsVerifier.getConfiguration(),
					reportDirectory);
			if (testRunPlanFile == null) {
				throw new Exception("Unable to generate the test run plan file.");
			}
			if (!testRunPlanFile.exists()) {
				throw new Exception("Unable to read generated the test run plan file.");
			}
			logger.info("TaSK: Generation of the test run plan file is successful.");
			logger.info("TaSK: Test run plan file is written to: " + testRunPlanFile.getAbsolutePath());
		} catch (Exception e) {
			logger.error("TaSK: Error occurred while trying to run test suite setup.", e);

		}

		// Start of Phase II: Execute the test cases in the TRP
		executeTaskTestToolRun(testRunPlanFile, reportDirectory, false, clientAuthCertChainFile, clientAuthKeyFile);
	}

	private void executeTaskTestTool(final File testRunPlanFile, final String reportDirectory, final String clientAuthCertChainFile, final String clientAuthKeyFile) {
		logger.warning(
				"TaSK: test run plan file is provided. Skipping the MICS file verification and running provided test run plan.");
		executeTaskTestToolRun(testRunPlanFile, reportDirectory, true, clientAuthCertChainFile, clientAuthKeyFile);
	}


	/**
	 * Execute the TaSK Test Tool with the given set of parameters.
	 * @param executionParameters The Parameters to use when executing the TaSK Framework.
	 */
	public static void executeTaskTestTool(final TaskExecutionParameters executionParameters) {
		try {
			// Start time
			var entry = new AbstractMap.SimpleEntry<>(
					ReportMetadataFields.START_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			executionParameters.getLogger().tellLogger(BasicLogger.MSG_METADATA, entry);

			var taskTestTool = new TaskTestTool(executionParameters.getLogger(), executionParameters.getConfigFile());
			taskTestTool.executeTaskTesttool(executionParameters);

		} catch (Exception e) {
			executionParameters.getLogger().error(e.getMessage(), e);
		} finally {
			var entry = new AbstractMap.SimpleEntry<>(ReportMetadataFields.END_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			executionParameters.getLogger().tellLogger(BasicLogger.MSG_METADATA, entry);
			waitForLogger();
		}
	}

	private void executeTaskTesttool(TaskExecutionParameters executionParameters) {
		switch (executionParameters.getExecutionMode()) {
			case MICS:
				this.executeTaskTestTool(executionParameters.getMicsFile(),
						executionParameters.getCertificateFileList(),
						executionParameters.isIgnoreMicsVerification(),
						executionParameters.getReportDirectory(),
						executionParameters.getClientAuthCertChain(),
						executionParameters.getClientAuthKeyFile());
				break;
			case TRP:
				this.executeTaskTestTool(executionParameters.getTestRunPlanFile(),
						executionParameters.getReportDirectory(),
						executionParameters.getClientAuthCertChain(),
						executionParameters.getClientAuthKeyFile());
				break;
			default:
				throw new RuntimeException("Executionmode is not implemented.");
		}
	}

	/*
	 * Execute the TaSK Test Tool with a MICS file as parameter.
	 * @param logger The LoggingConnector instance to use.
	 * @param configFile The GlobalConfiguration File to use.
	 * @param micsFile The MICS File to use.
	 * @param certificateFileList A List of Certificate Files referenced by the MICS.
	 * @param ignoreMicsVerification A boolean flag indicating whether a failing result of the MICS Verifier shall be ignored.
	 * @param reportDirectory The Directory Path in which reports and results shall be saved in.
	 * @param clientAuthCertChain Absolute Path to PEM encoded Client Authentication Certificate Chain
	 * @param clientAuthKey Absolute Path to PEM encoded Private Key for Client Authentication Certificate
	 */
	/*
	public static void executeTaskTestTool(final LoggingConnector logger,
			final File configFile, final File micsFile, final List<File> certificateFileList,
			final boolean ignoreMicsVerification, final String reportDirectory, final String clientAuthCertChain, final String clientAuthKey) {
		try {
			// Start time
			var entry = new AbstractMap.SimpleEntry<>(
					ReportMetadataFields.START_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);

			var taskTesTool = new TaskTestTool(logger, configFile);
			taskTesTool.executeTaskTestTool(micsFile, certificateFileList, ignoreMicsVerification, reportDirectory, clientAuthCertChain, clientAuthKey);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			var entry = new AbstractMap.SimpleEntry<>(ReportMetadataFields.END_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);

			waitForLogger();
		}
	}
	*/

	private static void waitForLogger() {
		// We need to wait for the logger to finish because end time in the XML/PDF report is sometimes missing.
		try {
			final int sleepTime = 1000;
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// Do nothing.
		}
	}

	/*
	 * Execute the TaSK Test Tool with a Test Run Plan file as parameter.
	 * @param logger The LoggingConnector instance to use.
	 * @param testRunPlanFile The Test Run Plan File to use.
	 * @param configFile The GlobalConfiguration File to use.
	 * @param reportDirectory The Directory Path in which reports and results shall be saved in.
	 * @param clientAuthCertChain Absolute Path to PEM encoded Client Authentication Certificate Chain
	 * @param clientAuthKeyFile Absolute Path to PEM encoded Private Key for Client Authentication Certificate
	 */
	/*
	public static void executeTaskTestTool(final LoggingConnector logger, final File testRunPlanFile,
										   final File configFile,
										   final String reportDirectory,
										   final String clientAuthCertChain,
										   final String clientAuthKeyFile) {
		try {
			// Start time
			var entry = new AbstractMap.SimpleEntry<>(
					ReportMetadataFields.START_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);

			var taskTesTool = new TaskTestTool(logger, configFile);
			taskTesTool.executeTaskTestTool(testRunPlanFile, reportDirectory, clientAuthCertChain, clientAuthKeyFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			var entry = new AbstractMap.SimpleEntry<>(ReportMetadataFields.END_OF_EXECUTION,
					DateTimeUtils.getISOFormattedTimeStamp());
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
			waitForLogger();
		}
	}
	*/


	private void executeTaskTestToolRun(final File testRunPlanFile, final String reportDirectory,
			final boolean doLogDutInformation, final String clientAuthCertChainFile, final String clientAuthKeyFile) {
		// Start of Phase II: Execute the test cases in the TRP
		// Parse the contents of the TRP
		// Initialize the TRP Library
		TestRunPlanConfiguration testRunPlanConfig = null;
		try {
			logger.info("TaSK: Parsing the test run plan file.");
			if (doLogDutInformation) {
				logDutInputFile(logger, testRunPlanFile);
			}
			testRunPlanConfig = TestRunPlanConfiguration.parseRunPlanConfiguration(testRunPlanFile, globalConfiguration,
					reportDirectory, clientAuthCertChainFile, clientAuthKeyFile);
			if (testRunPlanConfig == null) {
				throw new Exception("Unable to parse the test run plan file.");
			}
			if (doLogDutInformation) {
				logDutInformation(logger, testRunPlanConfig);
			}
			logger.info("TaSK: Parsing of the test run plan file is successful.");
		} catch (Exception e) {
			logger.error("TaSK: Error occurred while trying to parse the test run plan file.", e);
			return;
		}

		// Get an ordered list of a test cases to be executed and store in test suite run
		// data structure.
		var testSuite = new TestSuiteRun("TaSK TLS TestSuite", testRunPlanConfig.getTestCases());

		// Execute the test cases
		var testCaseRunner = new TestCaseRunner();
		testCaseRunner.executeTestCases(testSuite);
	}

	private static MICSVerifier initializeMicsVerifier(final File specificationDir, final File globalConfig) {
		// Configuration Files
		// Application Specifications
		var applicationSpecificationList = new LinkedList<File>();
		var applicationSpecificationsDirectory = new File(specificationDir, "ApplicationSpecifications");
		var xmlFilter =  new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		if (!applicationSpecificationsDirectory.isDirectory()) {
			throw new RuntimeException("ApplicationSpecifications in Specification Directory is no valid directory.");
		}
		var applicationSpecificationFiles = applicationSpecificationsDirectory.listFiles(xmlFilter);
		if (applicationSpecificationFiles == null || applicationSpecificationFiles.length == 0) {
			throw new RuntimeException("ApplicationSpecifications in Specification Directory could not be read.");
		}
		for (var xmlFile : applicationSpecificationFiles) {
			if (!xmlFile.isDirectory()) {
				applicationSpecificationList.add(xmlFile);
			}
		}

		// Application Mappings
		var applicationMappingDirectory = new File(specificationDir, "ApplicationSpecificProfiles");
		var applicationMappingList = new LinkedList<File>();

		if (!applicationMappingDirectory.isDirectory()) {
			throw new RuntimeException("ApplicationSpecificProfiles in Specification Directory is no valid directory.");
		}
		var applicationMappingFiles = applicationMappingDirectory.listFiles(xmlFilter);
		if (applicationMappingFiles == null || applicationMappingFiles.length == 0) {
			throw new RuntimeException("ApplicationSpecificProfiles in Specification Directory could not be read.");
		}
		for (var xmlFile : applicationMappingFiles) {
			if (!xmlFile.isDirectory()) {
				applicationMappingList.add(xmlFile);
			}
		}

		final var testCasesDir = new File(specificationDir, "TestCases");
		final var testProfiles = new File(specificationDir, "TestProfiles.xml");
		final var tlsConfigData = new File(specificationDir, "TlsConfigurationData.xml");

		// Initialize the MICS verifier with these files.
		MICSVerifier micsVerifier = new MICSVerifier(applicationSpecificationList,
				applicationMappingList, testCasesDir, testProfiles, globalConfig,
				tlsConfigData);

		return micsVerifier;
	}

	private static void logDutInformation(final LoggingConnector logger, final MICS mics) {
		if (logger == null || mics == null) {
			return;
		}
		// Title
		var value = mics.getTitle();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.TITLE, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}
		// Application Type
		value = mics.getApplicationType();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.APPLICATION_TYPE, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}
		// Description
		value = mics.getDescription();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.DESCRIPTION, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}
		// Version
		value = mics.getVersion();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.VERSION, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}
	}


	private static void logDutInformation(final LoggingConnector logger, final TestRunPlanConfiguration testRunPlan) {
		if (logger == null || testRunPlan == null) {
			return;
		}
		// Title
		var value = testRunPlan.getMicsInfo().getMicsName();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.TITLE, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}

		// Description
		value = testRunPlan.getMicsInfo().getMicsDescription();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.DESCRIPTION, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}
	}

	private static void logDutInputFile(final LoggingConnector logger, final File inputFile) {
		if (logger == null || inputFile == null) {
			return;
		}
		// File Absolute Path
		var value = inputFile.getAbsolutePath();
		if (value != null && !value.isEmpty()) {
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.FILE, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		}

		// File Fingerprint
		try {
			value = DatatypeConverter.printHexBinary(FileUtils.getFileFingerprint(inputFile));
			var entry = new AbstractMap.SimpleEntry<>(
					ReportDutInfoFields.FINGERPRINT, value);
			logger.tellLogger(BasicLogger.MSG_METADATA, entry);
		} catch (Exception ignored) {
			// Do nothing.
		}
	}
}
