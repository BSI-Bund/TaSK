package com.achelos.task.configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.InvalidAttributeValueException;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.*;
import com.achelos.task.dutcommandgenerators.DUTCommandGenerator;
import com.achelos.task.dutcommandgenerators.EIDClientTls12DUTCommandGenerator;
import com.achelos.task.dutcommandgenerators.EIDClientTls2DUTCommandGenerator;
import com.achelos.task.dutcommandgenerators.GenericClientExecutableDUTCommandGenerator;
import com.achelos.task.dutpreparation.DUTPreparer;
import com.achelos.task.dutpreparation.EIdECardAPIDUTPreparer;
import com.achelos.task.dutpreparation.GenericServerDUTPreparer;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.common.CertificateIdentifier;
import com.achelos.task.xmlparser.datastructures.common.TR03145CertificationInfo;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigChecker;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameterNames;
import com.achelos.task.xmlparser.datastructures.testrunplan.DUTCapabilities;
import com.achelos.task.xmlparser.datastructures.testrunplan.RunPlanMicsInfo;
import com.achelos.task.xmlparser.datastructures.testrunplan.TestRunPlanData;
import com.achelos.task.xmlparser.runplanparsing.RunPlanParser;


/**
 * Internal data structure combining all configuration values necessary to execute the TaSK Test Tool from an Test Run Plan file.
 * Currently implemented as a singleton.
 */
public class TestRunPlanConfiguration {
	private static final String CRL_OCSP_CERT_DIR_NAME = "ocsp_crl_testtool_certificates";
	private static final String MOTIVATOR_CERT_DIR_NAME = "motivator_test_certificates";
	private static final String LOGGING_COMPONENT = "TaSK: ";
	private HashMap<String, GlobalConfigParameter> globalConfiguration;
	private TestRunPlanData testRunPlanData;
	private DUTCommandGenerator dutCallCommandGenerator;
	private DUTPreparer dutPreparer;
	private String reportDirectory;
	private String clientAuthCertChainFile;
	private String clientAuthKeyFile;

	private static TestRunPlanConfiguration singleton;

	/**
	 * Parse the Test Run Plan file, and combine it with the other provided information. Store as singleton instance.
	 * @param runPlanFile Test Run Plan file to parse
	 * @param globalConfiguration Global Configuration to use.
	 * @param reportDirectory The report directory to store the output in.
	 * @param clientAuthCertChainFile The client authentication certificate chain file.
	 * @param clientAuthKeyFile The client authentication private key file.
	 * @return TestRunPlanConfiguration instance used for the execution of the TaSK Test Tool.
	 */
	public static TestRunPlanConfiguration parseRunPlanConfiguration(final File runPlanFile,
			final HashMap<String, GlobalConfigParameter> globalConfiguration, final String reportDirectory, final String clientAuthCertChainFile, final String clientAuthKeyFile) {
		singleton = new TestRunPlanConfiguration();
		singleton.setTestRunPlanData(RunPlanParser.parseRunPlan(runPlanFile));
		singleton.setGlobalConfiguration(globalConfiguration);
		singleton.setReportDirectory(reportDirectory);
		// Maybe he following two lines maybe should be a single call?
		singleton.setDUTCommandGenerator(singleton.getTestRunPlanData().getDUTApplicationType());
		singleton.setDUTPreparator(singleton.getTestRunPlanData().getDUTApplicationType());
		singleton.clientAuthCertChainFile = clientAuthCertChainFile;
		singleton.clientAuthKeyFile = clientAuthKeyFile;

		return singleton;
	}

	/*
	 * Parse the Test Run Plan file, and combine it with the other provided information. Store as singleton instance.
	 * @param runPlanFile Test Run Plan file to parse
	 * @param globalConfiguration Global Configuration to use.
	 * @param reportDirectory The report directory to store the output in.
	 * @return TestRunPlanConfiguration instance used for the execution of the TaSK Test Tool.

	public static TestRunPlanConfiguration parseRunPlanConfiguration(final File runPlanFile,
																	 final HashMap<String, GlobalConfigParameter> globalConfiguration, final String reportDirectory) {
		return parseRunPlanConfiguration(runPlanFile, globalConfiguration, reportDirectory, null, null);
	}
	 */

	/**
	 * If a TestRunPlanConfiguration has been parsed and stored as singleton instance, get the instance.
	 * @return singleton instance, if has been set.
	 * @throws NullPointerException if no singleton instance has been set yet.
	 */
	public static TestRunPlanConfiguration getInstance() {
		if (singleton != null) {
			return singleton;
		}
		throw new NullPointerException("TestRunPlanConfiguration not initialized yet.");
	}

	private TestRunPlanConfiguration() {
		// Empty.
	}

	private void setTestRunPlanData(final TestRunPlanData testRunPlanData) {
		this.testRunPlanData = testRunPlanData;
	}

	private void setGlobalConfiguration(final HashMap<String, GlobalConfigParameter> globalConfiguration) {
		this.globalConfiguration = globalConfiguration;
	}

	private void setReportDirectory(final String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}

	private void setDUTCommandGenerator(final String applicationType) {
		if (applicationType.equalsIgnoreCase("TR-03116-4-CLIENT")) {
			this.dutCallCommandGenerator = new GenericClientExecutableDUTCommandGenerator(this);
		} else if (applicationType.equalsIgnoreCase("TR-03124-1-EID-CLIENT-TLS-1-2")) {
			try {
				this.dutCallCommandGenerator = new EIDClientTls12DUTCommandGenerator(this);
			} catch (Exception e) {
				throw new RuntimeException("Unable to instantiate DUT Command Generator", e);
			}
		} else if (applicationType.equalsIgnoreCase("TR-03124-1-EID-CLIENT-TLS-2")) {
			try {
				this.dutCallCommandGenerator = new EIDClientTls2DUTCommandGenerator(this);
			} catch (Exception e) {
				throw new RuntimeException("Unable to instantiate DUT Command Generator", e);
			}
		} else if (applicationType.toLowerCase().contains("server")) {
			this.dutCallCommandGenerator = null;
		} else {
			throw new RuntimeException("Unknown ApplicationType in TestRunplan");
		}
	}

	private void setDUTPreparator(final String applicationType) {
		if (applicationType.equalsIgnoreCase("TR-03130-1-EID-SERVER-ECARD-PSK")) {
			if (getDUTCapabilities().contains(DUTCapabilities.PSK)) {
				if (getDutRMIURL().isBlank()) {
					throw new RuntimeException("RMI for DUT is required, but no address is provided in TRP file.");
				}
				var rmiUrl = getDutRMIURL();
				var rmiPort = !getDutRMIPort().isBlank() ? getDutRMIPort() : "1099";
				this.dutPreparer = new EIdECardAPIDUTPreparer(rmiUrl, Integer.parseInt(rmiPort));
			} else {
				this.dutPreparer = new GenericServerDUTPreparer();
			}
		} else {
			this.dutPreparer = new GenericServerDUTPreparer();
		}
	}

	/**
	 * Returns the contained DUT Call Command Generator instance.
	 * @return the contained DUT Call Command Generator instance.
	 */
	public DUTCommandGenerator getDutCallCommandGenerator() {
		return this.dutCallCommandGenerator;
	}

	/**
	 * Returns the contained DUT Preparer instance;
	 * @return the contained DUT Preparer instance.
	 */
	public DUTPreparer getDutPreparer() {
		return this.dutPreparer;
	}


	/**
	 * Returns the list of Test Cases stored in the Test Run Plan.
	 * @return the list of Test Cases stored in the Test Run Plan.
	 */
	public List<String> getTestCases() {
		return testRunPlanData.getTestCases();
	}

	/**
	 * Returns the tester in charge as specified in the global configuration XML file.
	 *
	 * @return the tester in charge as specified in the global configuration XML file.
	 */
	public String getTesterInCharge() {
		return globalConfiguration.get(GlobalConfigParameterNames.TesterInCharge.getParameterName()).getValueAsString();
	}
	
	/**
	 * Gets the test suite jar file path as specified in the global configuration XML file.
	 *
	 * @return the test suite jar file path as specified in the global configuration XML file.
	 */
	public List<String> getAdditionalTestSuiteJars() {
		if (!globalConfiguration.containsKey(GlobalConfigParameterNames.TestSuiteJars.getParameterName())) {
			return new ArrayList<String>();
		}
		return globalConfiguration.get(GlobalConfigParameterNames.TestSuiteJars.getParameterName()).getValueAsStringList();
	}

	/**
	 * Returns the path to the TLS Test Tool executable as specified in the global configuration file.
	 *
	 * @return path to the TLS Test Tool executable as specified in the global configuration file.
	 */
	public Path getTLSTestToolExecutable() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TlsTestToolPath)) {
			var logger = LoggingConnector.getInstance();

			Path path = Paths
					.get(getGlobalConfigParameter(GlobalConfigParameterNames.TlsTestToolPath).getValueAsString());

			logger.debug(LOGGING_COMPONENT + "TLS Test Tool executable path: '" + path + "'");
			// Check if the TLS Test Tool executable exists
			if (path.toFile().exists() && !startsWithSpecialNames(path) && path.toFile().isFile()) {
				return path;
			}

			logger.debug(
					LOGGING_COMPONENT + "Unable to retrieve the TLS Test Tool executable from the path: '" + path
							+ "'. Trying to resolve using relative path.");

			// TLS Test Tool file is not found. Maybe its a relative path. Try to find it from relative path to the jar
			// file.
			try {
				Path jarFileDirectory = getJarFileDirectory();

				if (jarFileDirectory != null) {
					logger.debug(
							LOGGING_COMPONENT + "Relative path: '" + jarFileDirectory + "'");

					File tlsTestToolFileRelativePath
							= Paths.get(jarFileDirectory.toString(), path.toString()).normalize().toFile();

					logger.debug(LOGGING_COMPONENT + "The relative path: '" + tlsTestToolFileRelativePath + "'");

					if (tlsTestToolFileRelativePath.exists() && tlsTestToolFileRelativePath.isFile()) {
						return tlsTestToolFileRelativePath.toPath();
					}
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e.toString());
			}

		}
		throw new RuntimeException("Unable to retrieve the TLS Test Tool executable.");
	}

	/**
	 * Returns the TLS Test Tool Log Level as specified in the global configuration XML file.
	 *
	 * @throws IllegalArgumentException If the parsed TLS Test Tool Log Level is of unknown value.
	 * @return the TLS Test Tool Log Level as specified in the global configuration XML file.
	 */
	public TlsTestToolLogLevel getLogLevel() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TlsTestToolLogLevel)) {
			try {
				return TlsTestToolLogLevel
						.getElement(getGlobalConfigParameter(GlobalConfigParameterNames.TlsTestToolLogLevel)
								.getValueAsString());
			} catch (InvalidAttributeValueException e) {
				throw new RuntimeException("Unable to retrieve log level element.", e);
			}
		}
		return TlsTestToolLogLevel.low;
	}

	/**
	 * Returns the path to the TLS Test Tool executable certificate directory as specified in the global configuration file.
	 *
	 * @return path to the TLS Test Tool executable certificate directory as specified in the global configuration file.
	 */
	public String getTlsTestToolCertificatesPath() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TlsTestToolCertificatesPath)) {

			var logger = LoggingConnector.getInstance();

			Path path = Paths.get(getGlobalConfigParameter(GlobalConfigParameterNames.TlsTestToolCertificatesPath)
					.getValueAsString());

			// Check if user has specified the path.
			if (path.toString().isEmpty()) {
				// Path is not set get the certificates directory from TLS Test Tool path.
				return getCertificatePathFromTlsTestTool();
			} 

			logger.debug(LOGGING_COMPONENT + "Certificates directory path: '" + path + "'");

				// Check if the directory exists. or else throw an error.
				if (path.toFile().exists() && !startsWithSpecialNames(path)) {

					return path.toString();
				} else {
					// A relative path is set.
					Path jarFileDirectory;
					try {
						jarFileDirectory = getJarFileDirectory();

						if (jarFileDirectory != null) {
							// Its a relative path, Try to find certificates directory from relative path to the jar
							// file.
							logger.debug(
									LOGGING_COMPONENT + "Trying to find certificates directory in the base directory: '"
											+ jarFileDirectory + "'");

							File tlsTestToolFileRelativePath
									= Paths.get(jarFileDirectory.toString(), path.toString()).normalize().toFile();

							logger.debug(LOGGING_COMPONENT + "Relative path: '" + tlsTestToolFileRelativePath + "'");

							if (tlsTestToolFileRelativePath.exists()) {
								return tlsTestToolFileRelativePath.toPath().toString();
							}
						}
					} catch (URISyntaxException e) {
						throw new RuntimeException(e.toString());
					}

				}


		} else {
			// Global configuration parameter is not set. Get the certificates directory from TLS Test Tool path.
			return getCertificatePathFromTlsTestTool();
		}
		throw new RuntimeException("Unable to retrieve the certificates path.");
	}

	private boolean startsWithSpecialNames(final Path path) {
		return path.startsWith(".") || path.startsWith("..");
	}

	private Path getJarFileDirectory() throws URISyntaxException {
		return Paths
				.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
	}

	private String getCertificatePathFromTlsTestTool() {
		var tlsTestToolExecPath = getTLSTestToolExecutable();
		var parentPath = tlsTestToolExecPath.getParent();
		if (parentPath == null) {
			parentPath = tlsTestToolExecPath;
		}
		return parentPath.toAbsolutePath().toString() + "/certificates/";
	}

	/**
	 * Returns the port of the Tls Test Tool as specified in the global configuration file.
	 *
	 * @return port of the Tls Test Tool as specified in the global configuration file.
	 */
	public int getTlsTestToolPort() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.TlsTestToolPort).getValueAsInteger();
	}

	/**
	 * Returns The maximum wait time for reading log messages value as specified in the global configuration file. If
	 * not specified, default value of 60 seconds will be used.
	 * 
	 * @return log message search timeout value as specified in the global configuration file.
	 */
	public int getMaximumWaitTimeForReadingLogMessage() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.MaximumWaitTimeForReadingLogMessage)
				.getValueAsInteger();
	}


	/**
	 * Returns the TlsWaitBeforeClose configuration as specified in the global configuration XML file. It specifies the
	 * waiting time before the TLS tool closes the connection. If not specified, default value of 5 seconds will be
	 * used.
	 *
	 * @return waiting time in seconds
	 */
	public int getTlsWaitBeforeClose() {
		if (globalConfiguration.containsKey(GlobalConfigParameterNames.TlsTestToolWaitBeforeClose.getParameterName())) {
			return globalConfiguration.get(GlobalConfigParameterNames.TlsTestToolWaitBeforeClose.getParameterName())
					.getValueAsInteger();
		}
		return 0;
	}

	/**
	 * Returns the report directory configuration as specified in the global configuration XML file.
	 *
	 * @return the report directory configuration as specified in the global configuration XML file.
	 */
	public File getReportDirectory() {
		if (reportDirectory != null && !reportDirectory.isEmpty()) {
			return new File(reportDirectory);
		}
		if (globalConfiguration.containsKey(GlobalConfigParameterNames.ReportDirectory.getParameterName())) {
			return new File(globalConfiguration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
					.getValueAsString());
		}
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.ReportDirectory)) {
			return new File(getGlobalConfigParameter(GlobalConfigParameterNames.ReportDirectory).getValueAsString());
		}
		return null;
	}

	/**
	 * Returns the directory for ocsp and crl certificates in the report directory.
	 *
	 * @return the directory for ocsp and crl certificates in the report directory.
	 */
	public File getCrlOcspCertDirectory() {
		var reportDir = getReportDirectory();
		if (reportDir == null) {
			return null;
		}
		return new File(reportDir, CRL_OCSP_CERT_DIR_NAME);
	}

	/**
	 * Returns the directory for motivator certificates in the report directory.
	 *
	 * @return the directory for motivator certificates in the report directory.
	 */
	public File getMotivatorCertDirectory() {
		var reportDir = getReportDirectory();
		if (reportDir == null) {
			return null;
		}
		return new File(reportDir, MOTIVATOR_CERT_DIR_NAME);
	}

	/**
	 * Returns the directory for ocsp and crl certificates in the report directory that are signed with the signature
	 * algorithm from cipher suite.
	 *
	 * @return the directory for ocsp and crl certificates in the report directory.
	 */
	public File getCrlOcspCertDirectoryWithMatchingKeyType(final TlsCipherSuite cipherSuite) {

		var reportDir = getCrlOcspCertDirectory();
		if (reportDir == null) {
			return null;
		}
		String certIntermediateFolder = "";
		if (TlsCipherSuite.filterByName("_ECDSA_").contains(cipherSuite)) {
			certIntermediateFolder = "certificateEcdsa";
		} else { // Fallback to RSA in all other cases.
			certIntermediateFolder = "certificateRsa";
		}
		return new File(reportDir, certIntermediateFolder);
	}

	/**
	 * Returns the directory for ocsp and crl certificates in the report directory that are signed with default
	 * signature algorithm RSA
	 *
	 * @return the directory for ocsp and crl certificates in the report directory
	 */
	public File getCrlOcspCertDirectoryWithMatchingKeyType() {
		return getCrlOcspCertDirectoryWithMatchingKeyType(TlsCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256);
	}
	
	/**
	 * Returns the certificate and private key path for ocsp and crl.
	 *
	 * @return the certificate and private key path for ocsp and crl
	 */
	public String[] getCrlOcspCertificate(TlsCipherSuite cipherSuite,
			TlsTestToolCertificateTypes certificateType) {
		String path = "";
		if (cipherSuite == null) {
			path = getCrlOcspCertDirectoryWithMatchingKeyType().getAbsolutePath();
		} else {
			path = getCrlOcspCertDirectoryWithMatchingKeyType(cipherSuite).getAbsolutePath();
		}
		String keyDirectories = "/server-certificate/private/";
		String certificateDirectories = "/server-certificate/certs/";
		
		String certificateFileName;
		if (certificateType.equals(TlsTestToolCertificateTypes.CERT_REVOKED)) {
			certificateFileName = "revoked-server-certificate.pem";
		} else if (certificateType.equals(TlsTestToolCertificateTypes.CERT_DEFAULT)) {
			certificateFileName = "server-certificate.pem";
		} else {
			return null;
		}
		
		String certPath = path + certificateDirectories + certificateFileName;
		String keyPath = path + keyDirectories + "server-certKey.pem";
		
		return new String[] {certPath, keyPath};
	}

	/**
	 * Returns the information whether TShark shall be enabled as specified in the global configuration XML file.
	 *
	 * @return true, if TShark is enabled, otherwise false
	 */
	public Boolean isTsharkEnabled() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TSharkEnabled)) {
			return getGlobalConfigParameter(GlobalConfigParameterNames.TSharkEnabled).getValueAsBoolean();
		}
		return false;
	}

	/**
	 * Returns the TShark interface (use for sniffing) as specified in the global configuration XML file.
	 *
	 * @return name or number of network interface
	 */
	public String getTsharkInterface() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TSharkInterface)) {
			return getGlobalConfigParameter(GlobalConfigParameterNames.TSharkInterface).getValueAsString();
		}
		return "";
	}

	/**
	 * Returns the TShark options as specified in the global configuration XML file.
	 *
	 * @return the TShark options as specified in the global configuration XML file.
	 */
	public String getTsharkOptions() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TSharkOptions)) {
			return getGlobalConfigParameter(GlobalConfigParameterNames.TSharkOptions).getValueAsString();
		}
		return "";
	}

	/**
	 * Returns the Path to TShark as Specified in the global configuration XML file.
	 *
	 * @return file path
	 */
	public String getTsharkPath() {
		if (isGlobalConfigParameterSet(GlobalConfigParameterNames.TSharkPath)) {
			return getGlobalConfigParameter(GlobalConfigParameterNames.TSharkPath).getValueAsString();
		}
		return "";
	}

	/**
	 * Returns the name of the file with the TLS secret.
	 *
	 * @return a filename
	 */
	public String getTlsSecretFile() {
		return Paths.get(getReportDirectory().getAbsolutePath(), "tlsSecretFile.txt").toString();
	}

	/**
	 * Returns the port of the OCSP Responder as specified in the global configuration file.
	 *
	 * @return port of the OCSP Responder as specified in the global configuration file.
	 */
	public int getOcspResponderPort() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.OcspResponderPort).getValueAsInteger();
	}

	/**
	 * Returns the port of the CRL Responder as specified in the global configuration file.
	 *
	 * @return port of the CRL Responder as specified in the global configuration file.
	 */
	public int getCrlResponderPort() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.CrlResponderPort).getValueAsInteger();
	}

	/**
	 * Returns the openssl executable specified in the global configuration file.
	 *
	 * @return openssl executable specified in the global configuration file.
	 */
	public String getOpenSSLExecutable() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.OpenSSLExecutablePath).getValueAsString();
	}

	private boolean isGlobalConfigParameterSet(final GlobalConfigParameterNames paramName) {
		return GlobalConfigChecker.isGlobalConfigParameterSet(globalConfiguration, paramName);
	}

	private GlobalConfigParameter getGlobalConfigParameter(final GlobalConfigParameterNames paramName) {
		return GlobalConfigChecker.getGlobalConfigParameter(globalConfiguration, paramName);
	}
	
	/**
	 * Returns the timeout in seconds of DUT executable as specified in the global configuration file.
	 *
	 * @return the timeout in seconds of DUT executable as specified in the global configuration file.
	 */
	public int getDutExecutableTimeout() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.DutExecutableTimeout).getValueAsInteger();
	}

	/**
	 * Returns the contained {@link TestRunPlanData} object.
	 *
	 * @return the contained {@link TestRunPlanData} object.
	 */
	private TestRunPlanData getTestRunPlanData() {
		return testRunPlanData;
	}

	/**
	 * Returns the generation time
	 *
	 * @return the generationTime
	 */
	public String getGenerationTime() {
		return testRunPlanData.getGenerationTime();
	}

	/**
	 * Return the list of supported TLS versions corresponding to the Table 4 of the TR-03116-TS ICS document.
	 *
	 * @return list of supported TLS versions.
	 */
	public List<TlsVersion> getSupportedTLSVersions() {
		return testRunPlanData.getSupportedTLSVersions();
	}

	/**
	 * Using a list of all TLS version, which comes from a Configuration File corresponding to Table 20 of the
	 * TR-03116-TS ICS document, generates a list of all TLS versions which are not supported by the DUT.
	 *
	 * @return A list of not supported TLS versions.
	 */
	public List<TlsVersion> getNotSupportedTLSVersions() {
		return testRunPlanData.getNotSupportedTLSVersions();
	}

	/**
	 * Looks for the highest supported TLS version of the DUT according to the MICS file and returns it.
	 *
	 * @return The highest supported TLS version of the DUT.
	 */
	public TlsVersion getHighestSupportedTlsVersion() {
		return testRunPlanData.getHighestSupportedTlsVersion();
	}

	/**
	 * Generates a list of all supported cipher suites for the specified TlsVersion, according to Table 5 of the
	 * TR-03116-TS ICS document, which contain the "_CBC_" tag in the identifier. The order of the list shall correspond
	 * to the preferences of the DUT.
	 *
	 * @param tlsVersion the TLS version the cipher suites should be applicable for.
	 * @return a list of all supported cipher suites for the specified Tls version which contain the "_CBC_" tag in the
	 * identifier.
	 */
	public List<TlsCipherSuite> getCBCBasedSupportedCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getCBCBasedSupportedCipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all supported cipher suites for the specified TLS version according to Table 5 of the
	 * TR-03116-TS ICS document. The order of the list shall correspond to the preferences of the DUT.
	 *
	 * @param tlsVersion The TLS version the cipher suites should be applicable for.
	 * @return a list of all supported cipher suites for the specified TLS version
	 */
	public List<TlsCipherSuite> getSupportedCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedCipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> getSupportedGroups(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedGroups(tlsVersion);
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> filterSupportedGroupsToEllipticCurveGroups(TlsVersion tlsVersion) {
		return testRunPlanData.filterSupportedGroupsToEllipticCurveGroups(tlsVersion);
	}

	/**
	 * Gets insufficient DHE key lengths for specified tls version.
	 * @param tlsVersion The TLS Version to get values for.
	 * @return insufficient DHE key lengths for specified tls version.
	 */
	public List<TlsDHGroup> getInsufficientDHEKeyLengths(TlsVersion tlsVersion) {
		return testRunPlanData.getInsufficientDHEKeyLengths(tlsVersion);
	}

	/**
	 * Gets sufficient DHE key lengths for specified tls version.
	 * @param tlsVersion The TLS Version to get values for.
	 * @return sufficient DHE key lengths for specified tls version.
	 */
	public List<TlsDHGroup> getSufficientDHEKeyLengths(TlsVersion tlsVersion) {
		return testRunPlanData.getSufficientDHEKeyLengths(tlsVersion);
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> filterSupportedGroupsToFFDHEGroups(TlsVersion tlsVersion) {
		return testRunPlanData.filterSupportedGroupsToFFDHEGroups(tlsVersion);
	}

	/**
	 * Using a list of all Elliptic Curves, which comes from a Configuration File corresponding to Table 21 of the
	 * TR-03116-TS ICS document, generates a list of all Elliptic Curves which are not supported by the DUT for the
	 * specified TlsVersion.
	 *
	 * @param tlsVersion The TlsVersion the list should be generated for.
	 * @return A list of all Elliptic Curves which are not supported by the DUT for the specified TlsVersion
	 */
	public List<TlsNamedCurves> getNotSupportedEllipticCurves(TlsVersion tlsVersion) {
		return testRunPlanData.getNotSupportedEllipticCurves(tlsVersion);
	}

	/**
	 * Using a list of all FFDHE Groups, which comes from a Configuration File of the
	 * TR-03116-TS ICS document, generates a list of all FFDHE Groups which are not supported by the DUT for the
	 * specified TlsVersion.
	 *
	 * @param tlsVersion The TlsVersion the list should be generated for.
	 * @return A list of all FFDHE Groups which are not supported by the DUT for the specified TlsVersion
	 */
	public List<TlsNamedCurves> getNotSupportedDHEGroups(TlsVersion tlsVersion) {
		return testRunPlanData.getNotSupportedDHEGroups(tlsVersion);
	}

	/**
	 * Generates a list of all TlsSignatureAlgorithmWithHash which are supported by the DUT for the specified TLS version.
	 * @param tlsVersion TLS Version which shall be used.
	 *
	 * @return A list of all TlsSignatureAlgorithmWithHash which are supported by the DUT.
	 */
	public List<TlsSignatureAlgorithmWithHash> getSupportedSignatureAlgorithms(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedSignatureAlgorithms(tlsVersion);
	}

	/**
	 * Generates a list of all Signature Algorithms in certificates which are supported by the DUT, if the DUT supports
	 * TLSv1.3. Note: Currently, this only makes sense for TLS version 1.3, hence, this returns the list of supported
	 * Signature Algorithms for Certificates for TLSv1.3.
	 *
	 * @return A list of all Signature Algorithms in certificates which are supported by the DUT, if the DUT supports
	 * TLSv1.3.
	 */
	public List<TlsSignatureScheme> getSupportedSignatureAlgorithmsForCertificates() {
		return testRunPlanData.getSupportedSignatureAlgorithmsForCertificates();
	}

	/**
	 * Return a single by the DUT supported cipher suite for the specified TlsVersion.
	 *
	 * @param tlsVersion The TlsVersion to get a single supported cipher suite for.
	 * @return a single by the DUT supported cipher suite for the specified TlsVersion or 'null' if no cipher suites are
	 * supported for the provided TlsVersion.
	 */
	public TlsCipherSuite getSingleSupportedCipherSuite(TlsVersion tlsVersion) {
		return testRunPlanData.getSingleSupportedCipherSuite(tlsVersion);
	}

	/**
	 * Return a single by the DUT supported ECC CipherSuite, i.e. cipher suites containing either '_ECDH_' or '_ECDHE_',
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The {@link TlsVersion} to get a single supported ECC cipher suite for.
	 * @return a single supported ECC cipher suite by the DUT for the specified TlsVersion or 'null' if no such
	 * cipher suites are supported for the provided {@link TlsVersion}.
	 */
	public TlsCipherSuite getSingleSupportedECCCipherSuite(TlsVersion tlsVersion) {
		return testRunPlanData.getSingleSupportedECCCipherSuite(tlsVersion);
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedECCCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedECCCipherSuites(tlsVersion);
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedEcdsaCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedEcdsaCipherSuites(tlsVersion);
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either 'RSA'
	 *  and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedRsaCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedRsaCipherSuites(tlsVersion);
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either 'DSA'
	 * and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedDsaCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedDsaCipherSuites(tlsVersion);
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedNonECCCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedNonECCCipherSuites(tlsVersion);
	}

	/**
	 * Checks the name of the cipher suite for support of elliptic curves.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports elliptic curve algorithms
	 */
	public boolean isECCCipherSuite(TlsCipherSuite cipherSuite) {
		return testRunPlanData.isECCCipherSuite(cipherSuite);
	}

	/**
	 * Checks the name of the cipher suite for support of ECDSA certificates
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports ECDSA
	 */
	public boolean isEcdsaCipherSuite(TlsCipherSuite cipherSuite) {
		return testRunPlanData.isEcdsaCipherSuite(cipherSuite);
	}

	/**
	 * Checks the name of the cipher suite for support of RSA certicates.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports RSA
	 */
	public boolean isRsaCipherSuite(TlsCipherSuite cipherSuite) {
		return testRunPlanData.isRsaCipherSuite(cipherSuite);
	}

	/**
	 * Checks the name of the cipher suite for support of DSA certicates.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports DSA
	 */
	public boolean isDsaCipherSuite(TlsCipherSuite cipherSuite) {
		return testRunPlanData.isDsaCipherSuite(cipherSuite);
	}

	/**
	 * Checks whether at least one of the cipher suites in the list supports elliptic curve.
	 *
	 * @param cipherSuites
	 * @return
	 */
	public boolean containsECCCipherSuite(List<TlsCipherSuite> cipherSuites) {
		return testRunPlanData.containsECCCipherSuite(cipherSuites);
	}

	/**
	 * Checks the name of the cipher suite for support of elliptic curves.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports elliptic curve algorithms
	 */
	public boolean isPFSCipherSuite(TlsCipherSuite cipherSuite) {
		return testRunPlanData.isPFSCipherSuite(cipherSuite);
	}

	/**
	 * Return the containsPFSCipherSuite value of the TestRunPlan.
	 * @return the containsPFSCipherSuite value of the TestRunPlan.
	 */
	public boolean containsPFSCipherSuite(List<TlsCipherSuite> cipherSuites) {
		return testRunPlanData.containsPFSCipherSuite(cipherSuites);
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT which provide perfect forward secrecy, i.e. the
	 * identifier contain '_ECDHE_' or '_DHE_', for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion the TlsVersion to get the list of supported PFS cipher suites for.
	 * @return a list of by the DUT supported PFS cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedPFSCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedPFSCipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_DHE_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion the TlsVersion to get the list of supported FFDHE cipher suites for.
	 * @return a list of by the DUT supported FFDHE cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedFFDHECipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedFFDHECipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_DHE_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion The TlsVersion to get the list of supported FFDHE cipher suites for.
	 * @return A list of by the DUT supported FFDHE cipher suites for the specified TlsVersion.
	 */
	public TlsCipherSuite getSingleSupportedFFDHECipherSuite(TlsVersion tlsVersion) {
		return testRunPlanData.getSingleSupportedFFDHECipherSuite(tlsVersion);
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_PSK_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion The TlsVersion to get the list of supported PSK cipher suites for.
	 * @return A list of by the DUT supported PSK cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedPSKCipherSuites(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedPSKCipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all cipher suites, that are not supported by the DUT.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion a {@link TlsVersion} as filter.
	 * @return a list of cipher suites which are not supported by the DUT
	 */
	public List<TlsCipherSuite> getNotSupportedCipherSuites(TlsVersion tlsVersion, TlsTestToolMode mode) {
		return testRunPlanData.getNotSupportedCipherSuites(tlsVersion, mode);
	}

	/**
	 * Returns the information, whether a PSK Hint is required by a client, according to Table 13 of the
	 * TR-03116-TS ICS document. Note: Only applicable for TLSv1.2.
	 *
	 * @return Information, whether a PSK Hint is required by a client, according to Table 13 of the TR-03116-TS
	 * ICS document.
	 */
	public Boolean isPSKHintRequired() {
		return testRunPlanData.isPSKHintRequired();
	}

	/**
	 * Returns the information, what value the PSK Identity Hint must contain, if it is applicable. According to Table
	 * 13 of the TR-03116-TS ICS document. Note: Only applicable for TLSv1.2.
	 *
	 * @return Information, what value the PSK Identity Hint must contain, if it is applicable. According to Table 13 of
	 * the TR-03116-TS ICS document.
	 */
	public String getPSKIdentityHint() {
		return testRunPlanData.getPSKIdentityHint();
	}

	/**
	 * The PSK Value to use when establishing a connection to the DUT. According to Table 12 of the TR-03116-TS ICS
	 * document.
	 *
	 * @return PSK Value to use when establishing a connection to the DUT.
	 */
	public byte[] getPSKValue() {
		return testRunPlanData.getPSKValue();
	}

	/**
	 * Generates a list containing the supported TLS extensions of the DUT for the specified TlsVersion according to
	 * Table 10 of the TR-03116-TS ICS document.
	 *
	 * @param tlsVersion The TlsVersion to get the supported TLS extensions for.
	 * @return A list containing the supported TLS extensions of the DUT for the specified TlsVersion.
	 */
	public List<TlsExtensionTypes> getSupportedExtensions(TlsVersion tlsVersion) {
		return testRunPlanData.getSupportedExtensions(tlsVersion);
	}

	/**
	 * Returns a data structure containing information on TR-03145 certification according to Table 11 of the
	 * TR-03116-TS ICS document.
	 *
	 * @return Data structure containing information on TR-03145 certification according to Table 11 of the TR-03116-TS
	 * ICS document.
	 */
	public TR03145CertificationInfo getTR03145CertificationInfo() {
		return testRunPlanData.getTR03145CertificationInfo();
	}

	/**
	 * Returns information whether the DUT makes use of early data according to Table 15 of the TR-03116-TS ICS
	 * document. Note: Only applicable for TLSv1.3.
	 *
	 * @return Whether the DUT makes use of early data according to Table 15 of the TR-03116-TS ICS document.
	 */
	public Boolean is0RTTSupported() {
		return testRunPlanData.is0RTTSupported();
	}

	/**
	 * Returns a list of Certificate Identifier objects according to Table 16 of the TR-03116-TS ICS document.
	 *
	 * @return A list of Certificate Identifier objects according to Table 16 of the TR-03116-TS ICS document.
	 */
	public List<CertificateIdentifier> getCertificateChain() {
		return testRunPlanData.getCertificateChain();
	}

	/**
	 * In case of a TLS server returns a list of (sub-)domain names, which the TLS server certificate is used for.
	 *
	 * @return A list of (sub-)domain names according to Table 17 of the TR-03116-TS ICS document.
	 */
	public List<String> getSubDomains() {
		return testRunPlanData.getSubDomains();
	}

	/**
	 * In case of a TLS server returns the address (i.e. the IP Address or the URL) under which the DUT can be found.
	 *
	 * @return the address (i.e. the IP Address or the URL) under which the DUT can be found.
	 */
	public String getDutAddress() {
		return testRunPlanData.getDutAddress();
	}

	/**
	 * Returns the ApplicationType of the DUT.
	 *
	 * @return tthe ApplicationType of the DUT.
	 */
	public String getDUTApplicationType() {
		return testRunPlanData.getDUTApplicationType();
	}

	/**
	 * In case of a TLS server returns the port under which the DUT can be found.
	 *
	 * @return the port under which the DUT can be found.
	 */
	public String getDutPort() {
		return testRunPlanData.getDutPort();
	}

	/**
	 * In case of a TLS server returns the address under which the RMI for the DUT can be found.
	 *
	 * @return the address under which the RMI of the DUT can be found.
	 */
	public String getDutRMIURL() {
		return testRunPlanData.getDutRMIURL();
	}

	/**
	 * In case of a TLS server returns the port under which the RMI for the DUT can be found.
	 *
	 * @return the port under which the RMI of the DUT can be found.
	 */
	public String getDutRMIPort() {
		return testRunPlanData.getDutRMIPort();
	}

	/**
	 * In case of a TLS client returns the executable by which the DUT can be executed.
	 *
	 * @return the executable by which the DUT can be executed.
	 */
	public String getDUTExecutable() {
		return testRunPlanData.getDUTExecutable();
	}

	/**
	 * In case of a TLS client returns the call arguments which shall be used to execute the DUT for a simple
	 * connection.
	 *
	 * @return the call arguments which shall be used to execute the DUT.
	 */
	public String getDUTCallArgumentsConnect() {
		return testRunPlanData.getDUTCallArgumentsConnect();
	}

	/**
	 * In case of a TLS client returns the call arguments which shall be used to execute the DUT for a session
	 * resumption.
	 *
	 * @return the call arguments which shall be used to execute the DUT.
	 */
	public String getDUTCallArgumentsResume() {
		return testRunPlanData.getDUTCallArgumentsResume();
	}

	/**
	 * Returns the port on which the eID-Client DUT listens.
	 * @return the port on which the eID-Client DUT listens.
	 */
	public Integer getDutEIDClientPort() {
		return testRunPlanData.getDutEIDClientPort();
	}

	/**
	 * Returns the maximum TLS session lifetime of the DUT according to Table 14 of the TR-03116-TS ICS document.
	 *
	 * @return the maximum TLS session lifetime of the DUT according to Table 14 of the TR-03116-TS ICS document.
	 */
	public Duration getMaximumTLSSessionTime() {
		return testRunPlanData.getMaximumTLSSessionTime();
	}

	/**
	 * Return the Generation Time of the TestRunPlan, which was written in the TestRunPlan XML file.
	 *
	 * @return The Generation Time of the TestRunPlan, which was written in the TestRunPlan XML file.
	 */
	public String getTRPCreationTime() {
		return testRunPlanData.getTRPCreationTime();
	}

	/**
	 * Return a single by the DUT unsupported TLS Signature Algorithm. Note: This always returns SHA1WithECDSA, as it
	 * should no longer be supported by any system.
	 *
	 * @return a single by the DUT unsupported TLS Signature Algorithm.
	 */
	public TlsSignatureAlgorithmWithHash getNotSupportedSignatureAlgorithm() {
		return testRunPlanData.getNotSupportedSignatureAlgorithm();
	}

	/**
	 * In case if the DUT is a TLS Server, returns the test client certificate file, used to test the mutual
	 * authentication to the server.
	 *
	 * @return the Client Certificate file, used to test the mutual authentication to the server.
	 */
	public File getClientCertificate() {
		return testRunPlanData.getClientCertificate();
	}

	/**
	 * In case if the DUT is a TLS Server, returns the private key of the test client certificate, used to test the
	 * mutual authentication to the server.
	 *
	 * @return the private key of the client, used to test the mutual authentication to the server.
	 */
	public File getClientPrivateKey() {
		return testRunPlanData.getClientPrivateKey();
	}

	/**
	 * Returns a Data Structure containing general information about the MICS file which was used to generate the
	 * TestRunPlan.
	 *
	 * @return Data Structure containing general information about the MICS file which was used to generate the TestRunPlan.
	 */
	public RunPlanMicsInfo getMicsInfo() {
		return testRunPlanData.getMicsInfo();
	}

	/**
	 * Returns the information, whether the DUT supports the use of SNI. This is a stub for now.
	 * @return true if the DUT supports use of SNI
	 */
	public boolean getTlsUseSni() {
		return testRunPlanData.getTlsUseSni();
	}

	/**
	 * Return the stored DUT Capabilities.
	 * @return the stored DUT Capabilities.
	 */
	public List<DUTCapabilities> getDUTCapabilities() {
		return testRunPlanData.getDutCapabilities();
	}

	/**
	 * Return the DUT BrowserSimulator URL of the Test Run Configuration.
	 * @return the DUT BrowserSimulator URL of the Test Run Configuration.
	 */
	public String getBrowserSimulatorURL() {
		return testRunPlanData.getBrowserSimulatorURL();
	}

	/**
	 * Return the DUT BrowserSimulator Port of the Test Run Configuration.
	 * @return the DUT BrowserSimulator Port of the Test Run Configuration.
	 */
	public String getBrowserSimulatorPort() {
		return testRunPlanData.getBrowserSimulatorPort();
	}

	public boolean isClientAuthCertAvailable() {
		if (clientAuthCertChainFile == null || clientAuthKeyFile == null) {
			return false;
		} else if (clientAuthKeyFile.isBlank() || clientAuthCertChainFile.isBlank()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Client Auth Cert Chain File Path
	 * @return Client Auth Cert Chain File Path
	 */
	public String getClientAuthCertChainFile() {
		return this.clientAuthCertChainFile;
	}

	/**
	 * Client Auth Key File Path
	 * @return Client Auth Key File Path
	 */
	public String getClientAuthKeyFile() {
		return this.clientAuthKeyFile;
	}

	public String getTaSKServerAddress() {
		return getGlobalConfigParameter(GlobalConfigParameterNames.RestApiHost).getValueAsString();
	}
}
