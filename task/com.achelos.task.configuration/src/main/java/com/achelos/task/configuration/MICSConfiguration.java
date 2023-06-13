package com.achelos.task.configuration;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.applicationmapping.AppMapping;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.testcase.TestCaseInfo;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;

/**
 * Internal data structure representing the configurations and specifications used to parse and verify MICS files.
 */
public class MICSConfiguration {

	/**
	 * List of available Application Specifications.
	 */
	public List<TlsSpecification> applicationSpecifications;
	/**
	 * List of available Application Profile Mappings.
	 */
	public List<AppMapping> applicationMappings;
	/**
	 * List of available Test Cases.
	 */
	public List<TestCaseInfo> testCases;
	/**
	 * List of available Test Profiles
	 */
	public List<String> testProfiles;
	/**
	 * The available Global Configuration.
	 */
	public HashMap<String, GlobalConfigParameter> globalConfiguration;
	/**
	 * List of all possible TLS Versions.
	 */
	public List<String> allTlsVersions;
	/**
	 * List of possible Elliptic Curves.
	 */
	public List<String> allNamedCurves;
	/**
	 * List of Key Lengths.
	 */
	public List<BigInteger> allKeyLengths;

	private static final String LOGGING_COMPONENT = "TaSK: ";

	private MICSConfiguration() {
		// Empty.
	}

	/**
	 * Initialize a MICS Configuration by parsing the necessary specification and configuration files and storing the information in internal data structures.
	 * @return the resulting MICSConfiguration object.
	 */
	public static MICSConfiguration initializeConfiguration(final List<File> applicationSpecifications,
			final List<File> applicationProfilesMappings, final File testCasesDir, final File testProfiles,
			final File globalConfig,
			final File tlsConfigurationData) {
		var configuration = new MICSConfiguration();
		var logger = LoggingConnector.getInstance();
		logger.info(LOGGING_COMPONENT + "Configuration Initialization.");

		// Application specification list
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading application specifications.");
		if (applicationSpecifications == null || applicationSpecifications.isEmpty()) {
			logger.error(LOGGING_COMPONENT
					+ "Configuration Initialization: Application specification list is \\\"null\\\" or empty.");
			throw new IllegalArgumentException("Application specification list is \"null\" or empty.");
		}

		configuration.applicationSpecifications = ConfigParser.parseSpecificationList(applicationSpecifications);
		if (configuration.applicationSpecifications == null
				|| configuration.applicationSpecifications.size() != applicationSpecifications.size()) {
			logger.error(
					LOGGING_COMPONENT + "Configuration Initialization: Error occurred while parsing the application specification list.");
			throw new IllegalArgumentException("An error occurred while parsing the application specification list.");
		}
		logger.debug(LOGGING_COMPONENT
				+ "Configuration Initialization: Application specifications are successfully initialized.");

		// Application specific profiles
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading the application profile mapping.");
		if (applicationProfilesMappings == null || applicationProfilesMappings.isEmpty()) {
			logger.error(LOGGING_COMPONENT
					+ "Configuration Initialization: Application specific profiles list is \\\"null\\\" or empty.");
			throw new IllegalArgumentException("Application specific profiles list is \"null\" or empty.");
		}
		configuration.applicationMappings = ConfigParser.parseApplicationMappingList(applicationProfilesMappings);
		if (configuration.applicationMappings == null
				|| configuration.applicationMappings.size() != applicationProfilesMappings.size()) {
			logger.error(LOGGING_COMPONENT
					+ "Configuration Initialization: Error occurred while parsing the application profile mapping list.");
			throw new IllegalArgumentException("An error occurred while parsing the application profile mapping list.");
		}
		logger.debug(LOGGING_COMPONENT
				+ "Configuration Initialization: Application profile mapping successfully initialized.");

		// Test cases
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading the test cases.");
		if (testCasesDir == null) {
			throw new IllegalArgumentException("The test cases directory is \"null\".");
		}
		configuration.testCases = ConfigParser.parseTestCases(testCasesDir);
		if (configuration.testCases == null || configuration.testCases.isEmpty()) {
			logger.error(LOGGING_COMPONENT + "Configuration Initialization: Error occurred while parsing the test cases.");
			throw new IllegalArgumentException("An error occurred while parsing the test cases.");
		}
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: The test cases are parsed successfully.");

		// Test Profiles
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading the test profiles file.");
		if (testProfiles == null) {
			throw new IllegalArgumentException("The test profiles file is \"null\".");
		}
		configuration.testProfiles = ConfigParser.parseTestProfiles(testProfiles);
		if (configuration.testProfiles == null || configuration.testProfiles.isEmpty()) {
			throw new IllegalArgumentException("An error occurred while parsing test profiles file.");
		}
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: The test profiles file is parsed successfully.");

		// Global Configuration
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading the global configuration file.");
		if (globalConfig == null) {
			throw new IllegalArgumentException("The global configuration file is \"null\".");
		}
		configuration.globalConfiguration = ConfigParser.parseGlobalConfig(globalConfig);
		if (configuration.globalConfiguration == null || configuration.globalConfiguration.isEmpty()) {
			throw new IllegalArgumentException("An error occurred while parsing the global configuration file.");
		}
		logger.debug(
				LOGGING_COMPONENT + "Configuration Initialization: The global configuration file is parsed successfully.");

		// TLS configuration Data.
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading further TLS configuration data.");
		if (tlsConfigurationData == null) {
			throw new IllegalArgumentException("The TLS configuration data file is \"null\".");
		}
		configuration.allTlsVersions = ConfigParser.parseTlsVersionsFromConfigData(tlsConfigurationData);
		configuration.allKeyLengths = ConfigParser.parseKeyLengthsFromConfigData(tlsConfigurationData);
		configuration.allNamedCurves = ConfigParser.parseNamedCurvesFromConfigData(tlsConfigurationData);
		if (configuration.allTlsVersions == null || configuration.allTlsVersions.isEmpty()
				|| configuration.allKeyLengths == null || configuration.allKeyLengths.isEmpty()
				|| configuration.allNamedCurves == null || configuration.allNamedCurves.isEmpty()) {
			throw new IllegalArgumentException("An error occurred while parsing the TLS configuration data file.");
		}
		logger.debug(LOGGING_COMPONENT + "Configuration Initialization: Reading further TLS configuration data.");

		logger.info(LOGGING_COMPONENT + "Configuration Initialization successful.");

		return configuration;
	}
}
