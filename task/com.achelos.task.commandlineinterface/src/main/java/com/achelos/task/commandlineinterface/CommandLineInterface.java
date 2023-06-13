package com.achelos.task.commandlineinterface;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.achelos.task.abstractinterface.TaskExecutionParameters;
import com.achelos.task.abstractinterface.TaskTestTool;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.reporting.datastructures.ReportLogger;
import com.achelos.task.reporting.pdfreport.PdfReport;
import com.achelos.task.reporting.xmlreport.XmlReport;
import com.achelos.task.restimpl.server.TaSKRestServer;
import com.achelos.task.utilities.DateTimeUtils;
import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameterNames;
import com.achelos.task.xmlparser.datastructures.testrunplan.TestRunPlanData;
import com.achelos.task.xmlparser.runplanparsing.RunPlanParser;

/**
 * Class representing the CommandLine Interface of the TaSK Test Tool. Provides a main method.
 */
public class CommandLineInterface {

	private final static String VERSION = "1.0.1";

	private enum EXEC_MODES {
		LOCAL_MICS_MODE,
		LOCAL_TRP_MODE,
		SERVER_MODE;
	}

	private static LoggingConnector logger;
	private static Options options;
	private static HelpFormatter helpFormatter;
	private static final String LOGGER_PREFIX = "TaSK CLI: ";

	private static Option micsOption;
	private static Option certOption;
	private static Option configOption;
	private static Option testRunPlanOption;
	private static Option onlyGenerateTrpOption;
	private static Option xmlReportOption;
	private static Option pdfReportOption;
	private static Option ignoreMicsVerificationOption;
	private static Option restServerOption;
	private static Option clientAuthCertChainOption;
	private static Option clientAuthKeyOption;
	private static Option certValidationCAOption;

	/**
	 * Hide default Constructor.
	 */
	private CommandLineInterface() {
		//Empty.
	}
	public static void main(final String[] args) {

		final var log_verbosity = "DEBUG";
		initializeLogger(log_verbosity);

		// Parse the command line arguments
		options = new Options();

		// Options
		var debugOption = new Option("g", "debug", false,
				"A flag which can be set when executing the TaSK framework. When set, additional debug information is printed to the console during the execution.");
		debugOption.setRequired(false);
		options.addOption(debugOption);

		micsOption = new Option("m", "mics-file", true,
				"Specifies the path to the Machine-readable ICS file which shall be used by the TaSK framework for the execution.");
		micsOption.setRequired(false);
		options.addOption(micsOption);

		certOption = new Option("d", "certificate-directory", true,
				"Specifies the path to a directory, which is searched for certificates in either DER or PEM encoding.");
		certOption.setRequired(false);
		options.addOption(certOption);

		configOption = new Option("c", "config-file", true,
				"Specifies the path to the global configuration XML file which shall be used by the TaSK framework.");
		configOption.setRequired(true);
		options.addOption(configOption);

		testRunPlanOption = new Option("t", "testrunplan", true,
				"Specifies the path to the TestRunPlan XML file which shall be executed by the TaSK framework.");
		testRunPlanOption.setRequired(false);
		options.addOption(testRunPlanOption);
		xmlReportOption = new Option("x", "generate-xml-report", false,
				"A flag which can be set when executing the TaSK framework. "
						+ "When set, XML report file shall be created.");
		xmlReportOption.setRequired(false);
		options.addOption(xmlReportOption);

		pdfReportOption = new Option("p", "generate-pdf-report", false,
				"A flag which can be set when executing the TaSK framework. "
						+ "When set, PDF and XML report file shall be created.");
		pdfReportOption.setRequired(false);
		options.addOption(pdfReportOption);

		ignoreMicsVerificationOption = new Option("i", "ignore-mics-verification", false,
				"Flag indicating whether the result of the MICS file verification should be ignored when running the resulting test cases.");
		ignoreMicsVerificationOption.setRequired(false);
		options.addOption(ignoreMicsVerificationOption);

		restServerOption = new Option("s", "rest-server", false,
				"Flag indicating whether the TaSK framework shall be executed as a REST server.");
		restServerOption.setRequired(false);
		options.addOption(restServerOption);

		onlyGenerateTrpOption = new Option("n", "only-generate-trp", false,
				"If set, only a test run plan will be generated from MICS. No tests are executed.");
		onlyGenerateTrpOption.setRequired(false);
		options.addOption(onlyGenerateTrpOption);

		clientAuthCertChainOption = new Option(null, "client-auth-certchain", true,
				"Specifies the PEM encoded client authentication certificate chain to be used by a test client to test the client authentication mechanism of a server.");
		clientAuthCertChainOption.setRequired(false);
		options.addOption(clientAuthCertChainOption);

		clientAuthKeyOption = new Option(null, "client-auth-key", true,
				"Specifies the PEM encoded private key file for the client authentication certificate to be used by a test client to test the client authentication mechanism of a server.");
		clientAuthKeyOption.setRequired(false);
		options.addOption(clientAuthKeyOption);
		options.addOption(clientAuthKeyOption);

		certValidationCAOption = new Option(null, "certificate-validation-ca", true, "Specifies the PEM encoded Root CA Certificate which shall be used as the trust anchor for the client authentication mechanism of the TaSK Framework test server.");
		certValidationCAOption.setRequired(false);
		options.addOption(certValidationCAOption);

		var defaultParser = new DefaultParser();
		helpFormatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = defaultParser.parse(options, args);
		} catch (Exception e) {
			exit(1, e.getMessage());
		}
		if (cmd == null) {
			exit(1, "Command line Parsing failed.");
			return;
		}

		// DEBUG Flag - Logger Verbosity
		// If DEBUG Flag is set, let logging verbosity be "debug", otherwise set logging
		// verbosity to "info".
		boolean debugFlagSet = cmd.hasOption(debugOption);
		if (!debugFlagSet) {
			logger.setLogVerbosity("INFO");
		} else {
			logger.setLogVerbosity("DEBUG");
		}

		// Switch between server and local mode.
		var execMode = selectExecMode(cmd);

		// Call method corresponding to each exec mode.
		try {
			switch (execMode) {
				case LOCAL_MICS_MODE:
					executeMicsMode(cmd);
					break;
				case LOCAL_TRP_MODE:
					executeTrpMode(cmd);
					break;
				case SERVER_MODE:
					executeServerMode(cmd);
					break;
				default:
					exit(1, "Unknown execution mode: " + execMode.name());
			}
		} catch (Exception e) {
			logger.error("Unhandled exception was caught in the CLI.", e);
		}

		stopLogger();
	}

	/**
	 * Execute the TaSK framework in the REST server mode.
	 *
	 * @param cmd The parsed {@link CommandLine} call.
	 */
	private static void executeServerMode(CommandLine cmd) {

		// Config Option and File
		if (!cmd.hasOption(configOption)) {
			exit(1, "If the MICS file is provided, the global configuration files also have to be provided.");
		}
		var configFile = new File(cmd.getOptionValue(configOption));
		if (!configFile.exists()) {
			exit(1, "File provided as " + configOption.getLongOpt() + " does not exist.");
		}
		HashMap<String, GlobalConfigParameter> configuration = null;
		try {
			configuration = ConfigParser.parseGlobalConfig(configFile);
		} catch (IllegalArgumentException e) {
			exit(1, "An error occurred while parsing the global configuration File: " + e.getMessage());
		}
		if (configuration == null || configuration.isEmpty()) {
			exit(1, "An error occurred while parsing the global configuration file. " + configFile);
		}
		logger.debug("Configuration Initialization: The global configuration file is parsed successfully.");

		if (configuration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName()) == null) {
			exit(1, "Unspecified required global configuration file: "
					+ GlobalConfigParameterNames.ReportDirectory.getParameterName());
		}

		// Start Server
		// Get HostName and Port from GlobalConfig
		var port = configuration.get(GlobalConfigParameterNames.RestApiPort.getParameterName()).getValueAsInteger();
		// Optionally, get SSL Credentials from Global Configuration
		SSLContext sslContext = null;
		var tlsCredentialsConfig = configuration.get(GlobalConfigParameterNames.RestApiCredentials.getParameterName());
		if (tlsCredentialsConfig != null && !tlsCredentialsConfig.getValueAsString().isBlank()) {
			var tlsCredentials = configuration.get(GlobalConfigParameterNames.RestApiCredentials.getParameterName()).getValueAsString();
			var tlsCredentialPassword = configuration.get(GlobalConfigParameterNames.RestApiCredentialPassword.getParameterName()).getValueAsString();
			try(var keyInput = new FileInputStream(tlsCredentials);) {
				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				keyStore.load(keyInput, tlsCredentialPassword.toCharArray());
				keyInput.close();
				keyManagerFactory.init(keyStore, tlsCredentialPassword.toCharArray());

				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
			} catch (Exception e) {
				logger.error("Error creating TLS context.", e);
				return;
			}
		} else {
			logger.warning("No TLS Credentials provided for REST Server. Running without TLS.");
		}
		logger.info("Trying to run TaSK REST Server on port " + Integer.toString(port));

		TaSKRestServer server = null;
		try {
			server = new TaSKRestServer(port, sslContext, logger, configFile);
			server.start();
			System.in.read(); //Maybe we should do something else here? This should be okay for now.
		} catch (Exception e) {
			logger.error("Error while executing the TaSK Framework REST Server.", e);
		} finally {
			if (server != null) {
				server.stop();
			}
		}

	}

	/**
	 * Execute the TaSK framework locally in the MICS mode.
	 *
	 * @param cmd The parsed {@link CommandLine} call.
	 */
	private static void executeMicsMode(final CommandLine cmd) {

		// Reporting
		// XML Reporter
		// If XML or PDF report Option is set, the corresponding logger shall be added
		// to the logging connector.
		boolean xmlReportSet = cmd.hasOption(xmlReportOption);
		boolean pdfReportSet = cmd.hasOption(pdfReportOption);
		ReportLogger reportLogger = null;
		if (xmlReportSet || pdfReportSet) {
			reportLogger = new ReportLogger();
			LoggingConnector.addLogger(List.of(reportLogger));
		}

		if (!cmd.hasOption(micsOption) || !cmd.hasOption(configOption)) {
			exit(1, "If the MICS file is provided, the global configuration files also have to be provided.");
		}

		// Config File
		var configFile = new File(cmd.getOptionValue(configOption));
		if (!configFile.exists()) {
			exit(1, "File provided as " + configOption.getLongOpt() + " does not exist.");
		}

		// Input Files
		var micsFile = new File(cmd.getOptionValue(micsOption));
		if (!micsFile.exists()) {
			exit(1, "File provided as " + micsOption.getLongOpt() + " does not exist.");
		}

		// Ignore MICS verification
		boolean ignoreMicsVerification = cmd.hasOption(ignoreMicsVerificationOption);

		// No run plan execution
		boolean onlyGenerateTRP = cmd.hasOption(onlyGenerateTrpOption);

		// Certificate Files
		ArrayList<File> certificateFileList = new ArrayList<>();
		var certDirOptionValue = cmd.getOptionValue(certOption);
		if (certDirOptionValue != null) {
			var certDir = new File(certDirOptionValue);
			if (certDir == null || !certDir.exists() || !certDir.isDirectory() || certDir.listFiles() == null) {
				exit(1, "Directory provided as " + certOption.getLongOpt() + " does not exist.");
			} else {
				var fileList = certDir.listFiles();
				if (fileList == null) {
					exit(1, "Directory provided as " + certOption.getLongOpt() + " could not be read.");
					return;
				} else {
					for (var file : fileList) {
						if (file != null && file.isFile()) {
							certificateFileList.add(file);
						}
					}
				}
			}
		}

		// Client Authentication Certificate Chain File and Private Key File
		var clientAuthCertChainOptionValue = cmd.getOptionValue(clientAuthCertChainOption);
		File clientAuthCertChainFile = null;
		String clientAuthCertChainPath = "";
		if (clientAuthCertChainOptionValue != null) {
			clientAuthCertChainFile = new File(clientAuthCertChainOptionValue);
			if (!clientAuthCertChainFile.exists()) {
				exit(1, "File provided as " + clientAuthCertChainOption.getLongOpt() + " does not exist.");
			}
			clientAuthCertChainPath = clientAuthCertChainFile.getAbsolutePath();
		}

		var clientAuthKeyOptionValue = cmd.getOptionValue(clientAuthKeyOption);
		File clientAuthKeyFile = null;
		String clientAuthKeyPath = "";
		if (clientAuthKeyOptionValue != null) {
			clientAuthKeyFile = new File(clientAuthKeyOptionValue);
			if (!(clientAuthKeyFile.exists())) {
				exit(1, "File provided as " + clientAuthKeyOption.getLongOpt() + " does not exist.");
			}
			clientAuthKeyPath = clientAuthKeyFile.getAbsolutePath();
		}

		// Certificate Validation Root CA Option
		var certValidationRootCAValue = cmd.getOptionValue(certValidationCAOption);
		File certValidationRootCAFile = null;
		String certValidationRootCAPath = "";
		if (certValidationRootCAValue != null) {
			certValidationRootCAFile = new File(certValidationRootCAValue);
			if (!(certValidationRootCAFile.exists())) {
				exit(1, "File provided as " + certValidationCAOption.getLongOpt() + " does not exist.");
			}
			certValidationRootCAPath = certValidationRootCAFile.getAbsolutePath();
		}

		HashMap<String, GlobalConfigParameter> configuration = null;
		try {
			configuration = ConfigParser.parseGlobalConfig(configFile);
		} catch (IllegalArgumentException e) {
			exit(1, "An error occurred while parsing the global configuration File: " + e.getMessage());
		}

		if (configuration == null || configuration.isEmpty()) {
			exit(1, "An error occurred while parsing the global configuration file. " + configFile);
		}
		logger.debug("Configuration Initialization: The global configuration file is parsed successfully.");

		if (configuration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName()) == null) {
			exit(1, "Unspecified required global configuration file: "
					+ GlobalConfigParameterNames.ReportDirectory.getParameterName());
		}
		var reportDir = configuration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
				.getValueAsString();
		var date = DateTimeUtils.getTimeStampForFileAndDirectoryNames();
		var reportDirectory = Paths.get(reportDir, date + "_TestReport").toString();

		var executionParameters = new TaskExecutionParameters(logger, configFile, micsFile, certificateFileList,
				ignoreMicsVerification, onlyGenerateTRP, reportDirectory, clientAuthCertChainPath, clientAuthKeyPath, certValidationRootCAPath);

		TaskTestTool.executeTaskTestTool(executionParameters);

		if (pdfReportSet) {
			pdfReportGeneration(reportLogger, reportDirectory, configuration);
		} else if (xmlReportSet) {
			xmlReportGeneration(reportLogger, reportDirectory);
		}
	}

	/**
	 * Execute the TaSK framework locally in the MICS mode.
	 *
	 * @param cmd The parsed {@link CommandLine} call.
	 */
	private static void executeTrpMode(final CommandLine cmd) {
		// Reporting
		// XML Reporter
		// If XML or PDF report Option is set, the corresponding logger shall be added
		// to the logging connector.
		boolean xmlReportSet = cmd.hasOption(xmlReportOption);
		boolean pdfReportSet = cmd.hasOption(pdfReportOption);
		ReportLogger reportLogger = null;
		if (xmlReportSet || pdfReportSet) {
			reportLogger = new ReportLogger();
			LoggingConnector.addLogger(List.of(reportLogger));
		}

		if (!cmd.hasOption(testRunPlanOption) || !cmd.hasOption(configOption)) {
			exit(1, "If the TaSK framework should run from a test run plan file, "
					+ "A global configuration files also have to be provided.");
		}

		// Check if file exists.
		var testRunPlanFile = new File(cmd.getOptionValue(testRunPlanOption));
		if (!testRunPlanFile.exists()) {
			exit(1, "Provided test run plan file does not exist.");
		}
		TestRunPlanData testRunPlan = null;
		try {
			testRunPlan = RunPlanParser.parseRunPlan(testRunPlanFile);
			if (testRunPlan == null) {
				exit(1, "Unable to parse test run plan file.");
			}
		} catch (Exception e) {
			exit(1, "An error occurred while trying to parse test run plan file.", e);
		}

		// Client Authentication Certificate Chain File and Private Key File
		var clientAuthCertChainOptionValue = cmd.getOptionValue(clientAuthCertChainOption);
		File clientAuthCertChainFile = null;
		String clientAuthCertChainPath = "";
		if (clientAuthCertChainOptionValue != null) {
			clientAuthCertChainFile = new File(clientAuthCertChainOptionValue);
			if (!clientAuthCertChainFile.exists()) {
				exit(1, "File provided as " + clientAuthCertChainOption.getLongOpt() + " does not exist.");
			}
			clientAuthCertChainPath = clientAuthCertChainFile.getAbsolutePath();
		}

		var clientAuthKeyOptionValue = cmd.getOptionValue(clientAuthKeyOption);
		File clientAuthKeyFile = null;
		String clientAuthKeyPath = "";
		if (clientAuthKeyOptionValue != null) {
			clientAuthKeyFile = new File(clientAuthKeyOptionValue);
			if (!(clientAuthKeyFile.exists())) {
				exit(1, "File provided as " + clientAuthKeyOption.getLongOpt() + " does not exist.");
			}
			clientAuthKeyPath = clientAuthKeyFile.getAbsolutePath();
		}

		// Certificate Validation Root CA Option
		var certValidationRootCAValue = cmd.getOptionValue(certValidationCAOption);
		File certValidationRootCAFile = null;
		String certValidationRootCAPath = "";
		if (certValidationRootCAValue != null) {
			certValidationRootCAFile = new File(certValidationRootCAValue);
			if (!(certValidationRootCAFile.exists())) {
				exit(1, "File provided as " + certValidationCAOption.getLongOpt() + " does not exist.");
			}
			certValidationRootCAPath = certValidationRootCAFile.getAbsolutePath();
		}

		var configFile = new File(cmd.getOptionValue(configOption));
		if (!configFile.exists()) {
			exit(1, "File provided as " + configOption.getLongOpt() + " does not exist.");
		}
		HashMap<String, GlobalConfigParameter> configuration = ConfigParser.parseGlobalConfig(configFile);
		if (configuration == null || configuration.isEmpty()) {
			exit(1, "An error occurred while parsing the global configuration file. " + configFile);
		}
		logger.debug("Configuration Initialization: The global configuration file is parsed successfully.");
		if (configuration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName()) == null) {
			exit(1, "Unspecified required global configuration file: "
					+ GlobalConfigParameterNames.ReportDirectory.getParameterName());
		}
		var reportDir = configuration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
				.getValueAsString();
		var date = DateTimeUtils.getTimeStampForFileAndDirectoryNames();
		var reportDirectory = Paths.get(reportDir, date + "_TestReport").toString();

		var executionParameters = new TaskExecutionParameters(logger, testRunPlanFile, configFile, reportDirectory, clientAuthCertChainPath, clientAuthKeyPath,  certValidationRootCAPath);

		TaskTestTool.executeTaskTestTool(executionParameters);
		if (pdfReportSet) {
			pdfReportGeneration(reportLogger, reportDirectory, configuration);
		} else if (xmlReportSet) {
			xmlReportGeneration(reportLogger, reportDirectory);
		}
	}

	/**
	 * Check which execution mode has been selected via the command line interface and return the corresponding
	 * enumeration value here.
	 *
	 * @param cmd The parsed {@link CommandLine} call.
	 * @return The selected execution mode as enumeration.
	 */
	private static EXEC_MODES selectExecMode(final CommandLine cmd) {
		EXEC_MODES execMode = null;
		// Either the MICS file or TestRunPlan file has to be provided.
		boolean onlyOneOptionSet = cmd.hasOption(testRunPlanOption) ^ cmd.hasOption(micsOption) ^ cmd.hasOption(restServerOption);
		if (!onlyOneOptionSet) {
			exit(1,
					"Either a run plan file, a MICS file, or the REST server option should be provided, but not multiple.");
		} else {
			if (cmd.hasOption(testRunPlanOption)) {
				execMode = EXEC_MODES.LOCAL_TRP_MODE;
			} else if (cmd.hasOption(micsOption)) {
				execMode = EXEC_MODES.LOCAL_MICS_MODE;
			} else {
				execMode = EXEC_MODES.SERVER_MODE;
			}
		}
		return execMode;
	}

	private static void exit(final int returnCode, final String errorMessage, final Throwable t) {
		logger.error(LOGGER_PREFIX + errorMessage, t);
		stopLogger();
		helpFormatter.printHelp("TaSK CLI " + VERSION , options);
		System.exit(returnCode);
	}

	private static void exit(final int returnCode, final String errorMessage) {
		exit(returnCode, errorMessage, null);
	}

	private static void initializeLogger(final String log_verbosity) {
		logger = LoggingConnector.getInstance(log_verbosity);
	}

	private static File xmlReportGeneration(final ReportLogger reportLogger, final String reportDirectory) {
		if (logger == null) {
			return null;
		}
		if (reportLogger == null) {
			logger.error(
					LOGGER_PREFIX + "Trying to generate the XML report, but report logger was never created. Aborting.");
			return null;
		}
		try {
			var xmlReport = XmlReport.fromReportInstance(reportLogger.generateReport());
			var xmlReportFileName = new File(reportDirectory, "Report.xml");
			xmlReport.writeToFile(xmlReportFileName);
			return xmlReportFileName;
		} catch (Exception e) {
			logger.error(LOGGER_PREFIX + "An error occurred while generating the XML report.", e);
			return null;
		}
	}

	private static void pdfReportGeneration(final ReportLogger reportLogger, final String reportDirectory, final HashMap<String, GlobalConfigParameter> configuration) {
		try {
			var xmlReportFileName = xmlReportGeneration(reportLogger, reportDirectory);
			if (configuration.containsKey(GlobalConfigParameterNames.PdfReportStylesheet.getParameterName())) {
				var styleSheetFile = new File(configuration.get(GlobalConfigParameterNames.PdfReportStylesheet.getParameterName()).getValueAsString());
				PdfReport.convertToPDF(xmlReportFileName, styleSheetFile);
			} else {
				PdfReport.convertToPDF(xmlReportFileName);
			}
		} catch (Exception e) {
			logger.error(LOGGER_PREFIX + "An error occurred while generating PDF Report.", e);
			return;
		}
	}

	private static void stopLogger() {
		if (logger != null) {
			// Wait for the logger to finish
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.stop();
		}
	}

}
