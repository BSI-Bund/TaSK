package com.achelos.task.commandlineexecution.applications.tlstesttool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfiguration;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationHandshakeType;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationOption;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.RunLogger;
import com.achelos.task.commons.certificatehelper.CertificateHelper;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsAlertLevel;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsDHGroup;
import com.achelos.task.commons.enums.TlsECGroup;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsTestToolLogLevel;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtServerName;
import com.achelos.task.commons.tlsextensions.TlsExtensionList;
import com.achelos.task.commons.tools.StringTools;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.logging.LogBean;


/**
 * TlsTestToolExecutor.
 */
public class TlsTestToolExecutor extends RunLogger {
	/**
	 * 127.0.0.1
	 */
	public static final String TLS_TEST_TOOL_LOCAL_HOST_AS_SERVER = "127.0.0.1";
	private static final int A_0XFF = 0xff;
	private final TlsTestToolConfiguration config;
	private final TestRunPlanConfiguration configuration;
	private TlsTestToolMode mode;

	/**
	 * Constructor for starting TLS Test Tool as TLS client.
	 *
	 * @param testCaseName test case name
	 * @param logger the logging connector
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public TlsTestToolExecutor(final String testCaseName, final LoggingConnector logger)
																							throws IOException,
																							URISyntaxException {
		super(testCaseName, Executor.TLSTESTTOOL, logger);
		config = new TlsTestToolConfiguration();
		configuration = TestRunPlanConfiguration.getInstance();
	}

	/**
	 * Method starts the TLS Test Tool with the given log level and waits for termination.
	 *
	 * @throws Exception
	 */
	private void startTestTool()
			throws Exception {
		if (!isNull()) {
			logError("TLS Test Tool already running.");
			return;
		}

		// Reset the flag to indicate that the entire protocol is not available.
		processLoggingOutputDone = false;

		final TlsTestToolLogLevel logLevel = configuration.getLogLevel();
		setLogLevel(logLevel);

		final String tlsSecretFile = configuration.getTlsSecretFile();
		addConfigOption(TlsTestToolConfigurationOption.TLSSECRETFILE, tlsSecretFile);

		// Set the variable waitBeforeClose from the global parameters if it has not yet
		// been set in the test case
		if (getWaitBeforeClose() == null) {
			final int waitBeforeCloseTimeout = configuration.getTlsWaitBeforeClose();
			setWaitBeforeClose(waitBeforeCloseTimeout);
		}
		final String tlsConfigFile = "tls_tool_config";
		String currentRunSuffix = null == getIterationCounter() ? "" : getIterationCounter().toFileNameSuffix();

		final File configFile
				= new File(Paths.get(configuration.getReportDirectory().getAbsolutePath(), getTestCaseName(),
						getTestCaseName() + "_" + tlsConfigFile + currentRunSuffix + ".conf").toString());
		var mkdirResult = configFile.getParentFile().mkdirs();
		if (!mkdirResult) {
			logDebug("Unable to create log file directory: " + configFile);
		}
		config.writeTo(configFile.toPath());
		logInfo("TLS Test Tool configuration: " + configFile.getAbsolutePath());
		tellLogger("TLS Test Tool configuration: ", configFile.getAbsolutePath());
		Path testToolPath;
		testToolPath = configuration.getTLSTestToolExecutable();

		startTestRun(testToolPath, configFile.getAbsolutePath());
	}


	private void startTestRun(final Path testToolPath, final String configurationFile)
			throws Exception {
		// Reset the flag to indicate that the entire protocol is not available.
		String executionFileOnly = "";
		String testToolWorkingDir = "";
		if (testToolPath.isAbsolute()) {
			Path fileName = testToolPath.getFileName();
			if (fileName != null) {
				executionFileOnly = fileName.toString();
			}
			Path parentPath = testToolPath.getParent();
			if (parentPath != null) {
				testToolWorkingDir = parentPath.toAbsolutePath().toString();
			}

		}
		final List<String> command = new ArrayList<>();
		command.add(Paths.get(testToolWorkingDir, executionFileOnly).toString());
		command.add("--configFile=" + configurationFile);

		start(command, null, new File(testToolWorkingDir));
		// We add a small delay for the process to finish
		final int finishProcessDelay = 3000;
		startSleepTimer(finishProcessDelay);
	}


	/**
	 * Method starts the TLS Test Tool using configured log level and waits for termination.
	 *
	 * @throws Exception
	 */
	public final void start()
			throws Exception {
		setIterationCounter(null);
		startTestTool();
	}


	/**
	 * Method starts the TLS Test Tool using configured log level and waits for termination. This overloaded function is
	 * for starting it multiple times during one test case.
	 *
	 * @param currentIteration Number of the current iteration
	 * @param totalNumberOfIterations Overall number of iterations
	 * @throws Exception
	 */
	public final void start(final int currentIteration, final int totalNumberOfIterations)
			throws Exception {
		setIterationCounter(new IterationCounter(currentIteration, totalNumberOfIterations));
		startTestTool();
	}
	
	public final void stop() {
		destroy();
		removeShutdownHook();
	}


	/**
	 * Sets a TLS Test Tool configuration option.
	 *
	 * @param name configuration option name
	 * @param value configuration value
	 */
	private void addConfigOption(final TlsTestToolConfigurationOption name, final String value) {
		logDebug("Setting: " + name + "=" + value);
		config.setOption(name, value);
	}


	/**
	 * Specify the {@link TlsTestToolLogLevel} for the TLS Test Tool.
	 *
	 * @param logLevel LogLevel holds the current log level for the TLS Test Tool Log levels are: high Much debug output
	 * (e.g., additional hex dumps, console output tshark logging). Medium amount of debug output (e.g.,
	 * additional output of sizes of received packages). low Little debug output (e.g., print actions that are
	 * performed). off No output.
	 */
	public final void setLogLevel(final TlsTestToolLogLevel logLevel) {
		addConfigOption(TlsTestToolConfigurationOption.LOGLEVEL, logLevel.getValue());
	}

	/**
	 * Specify the mode for the TLS Test Tool. Valid values are client and server.
	 *
	 * @param mode Mode holds the current mode client or server
	 */
	public final void setMode(final TlsTestToolMode mode) {
		this.mode = mode;
		addConfigOption(TlsTestToolConfigurationOption.MODE, mode.getValue());
	}


	/**
	 * Specify the TLS library to use for the TLS Test Tool.
	 *
	 * @param tlsLibrary The TLS library to be used by TLS Test Tool
	 */
	public final void setTlsLibrary(final TlsTestToolTlsLibrary tlsLibrary) {
		addConfigOption(TlsTestToolConfigurationOption.TLSLIBRARY, tlsLibrary.getValue());
	}

	/**
	 * Method checks if the Mode is set to Server beforehand and sets the desired Diffie-Hellman Group.
	 *
	 * @param dhGroup The Diffie-Hellman group to be used.
	 */
	public final void setDHGroup(final TlsDHGroup dhGroup) {
		checkServerMode();
		addConfigOption(TlsTestToolConfigurationOption.TLSSERVERDHPARAMS, dhGroup.getValue());
	}

	/**
	 * Method checks if the Mode is set to Server beforehand and sets the desired Diffie-Hellman Group.
	 *
	 * @param dhGroup The Diffie-Hellman group to be used.
	 */
	public final void setDHGroup(final TlsNamedCurves dhGroup) {
		checkServerMode();
		addConfigOption(TlsTestToolConfigurationOption.TLSSERVERDHPARAMS, dhGroup.getName());
	}

	/**
	 * Method checks if the Mode is set to Server beforehand and sets the desired elliptic curve group.
	 *
	 * @param ecGroup The elliptic curve group to be used.
	 */
	public final void setECGroup(final TlsECGroup ecGroup) {
		checkServerMode();
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATEELLIPTICCURVEGROUP, ecGroup.getValue());
	}

	/**
	 * Method checks if the Mode is set to Server beforehand and sets the desired elliptic curve group.
	 *
	 * @param ecGroup The elliptic curve group to be used.
	 */
	public final void setECGroup(final TlsNamedCurves ecGroup) {
		checkServerMode();
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATEELLIPTICCURVEGROUP, ecGroup.getName());
	}


	/**
	 * Sets all TLS Supported Groups to be sent in ClientHello supported_groups extension.
	 *
	 * @param groups the list of DHE/DH named curves the TLS client is willing to support.
	 */
	public final void setTlsSupportedGroups(final List<TlsNamedCurves> groups) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < groups.size(); i++) {
			if (0 != i) {
				sb.append(',');
			}
			sb.append(groups.get(i).name());
		}
		addConfigOption(TlsTestToolConfigurationOption.TLSSUPPORTEDGROUPS, sb.toString());
	}


	/**
	 * Sets all TLS supported signature algorithms to be sent in ClientHello supported_algorithms extension.
	 *
	 * @param signatureAlgorithms the list of signature algorithms the TLS client is willing to support.
	 */
	public final void setTlsSignatureAlgorithms(final List<TlsSignatureAlgorithmWithHash> signatureAlgorithms) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < signatureAlgorithms.size(); i++) {
			if (0 != i) {
				sb.append(',');
			}
			sb.append("(" +
					signatureAlgorithms.get(i).getSignatureAlgorithm().getSignatureAlgorithmLevelValue() +
					"," + signatureAlgorithms.get(i).getHashAlgorithm().getHashAlgorithmLevelValue() + ")");
		}
		addConfigOption(TlsTestToolConfigurationOption.SIGNATUREALGORITHMS, sb.toString());
	}


	/**
	 * tlsVersion=([major],[minor]) (with decimal integers major equal to 3 and minor from [1,3]) If mode=client, send
	 * the specified version in ClientHello.client_version. If mode=server, accept only the specified version and send
	 * it in the specified version in ServerHello.server_version. Use (3,1) for TLS v1.0, (3,2) for TLS v1.1, (3,2) for
	 * TLS v1.2. If not specified, all three TLS versions are accepted by a server and the highest version is sent by a
	 * client.
	 *
	 * @param tlsVersion The TLS version to set.
	 */
	public final void setTlsVersion(final TlsVersion tlsVersion) {
		addConfigOption(TlsTestToolConfigurationOption.TLSVERSION, tlsVersion.getTlsVersionValue());

		// TLS 1.3 requires the OpenSSL library
		if (tlsVersion == TlsVersion.TLS_V1_3) {
			setTlsLibrary(TlsTestToolTlsLibrary.OpenSSL);
		}
	}


	/**
	 * Sets the value of parameter {@code tlsUseSni} to true.<br>
	 * Activates the use of SNI (Server Name Indication).
	 * <p>
	 * If false, the handshake is not performed with SNI. <br>
	 * If true, the handshake is performed with SNI.
	 */
	public final void setTlsUseSni() {
		addConfigOption(TlsTestToolConfigurationOption.TLSUSESNI, "true");
	}


	/**
	 * Get the boolean information as string if the SNI extension is used or not.
	 *
	 * @return Whether SNI extension is used or not. That means "true" or "false".
	 */
	public final String getTlsUseSni() {
		return config.getOption(TlsTestToolConfigurationOption.TLSUSESNI);
	}


	/**
	 * tlsVerifyPeer=[verify] (with Boolean value verify either true or false, default false) If false, a peer
	 * certificate is not verified. If true, a valid peer certificate is required. If no valid peer certificate is
	 * presented, the TLS handshake is aborted.
	 *
	 */
	public final void tlsVerifyPeer() {
		addConfigOption(TlsTestToolConfigurationOption.TLSVERIFYPEER, "true");
	}


	/**
	 * Disable the usage of Encrypt-then-MAC.
	 *
	 */
	public final void tlsEncryptThenMacDisable() {
		addConfigOption(TlsTestToolConfigurationOption.TLSENCRYPTTHENMAC, "false");
	}


	/**
	 * Enable the usage of Encrypt-then-MAC.
	 *
	 */
	public final void tlsEncryptThenMacEnable() {
		addConfigOption(TlsTestToolConfigurationOption.TLSENCRYPTTHENMAC, "true");
	}


	/**
	 * caCertificateFile=[path] (with path pointing to a PEM- or DER-encoded file) File containing an X.509 CA
	 * certificate that will be used to verify peer certificates.
	 *
	 * @param fileLocation Path to the certificate file to be loaded
	 */
	public final void setCaCertificateFile(final Path fileLocation) {
		addConfigOption(TlsTestToolConfigurationOption.CACERTIFICATEFILE, fileLocation.toString());
	}

	/**
	 * Selected and sets the certificate and private key files to TLS test tool configuration based on selected
	 * cipher suite.
	 * 
	 * @param tlsVersion the selected TLS version
	 * @param cipherSuite selected cipher suite
	 * @param certificateType The TlsTestToolCertificateType in regard to chapter 4.2 of the TR-03116-TS of the certificate to use for the TLSTestTool.
	 * @return the name of the selected certificate file
	 * @throws FileNotFoundException
	 */
	public final String setCertificateAndPrivateKey(final TlsVersion tlsVersion,
													final TlsCipherSuite cipherSuite,
													final TlsTestToolCertificateTypes certificateType)
			throws FileNotFoundException {
		return setCertificateAndPrivateKey(tlsVersion, cipherSuite, null, certificateType);
	}

	/**
	 * Selected and sets the certificate and private key files to TLS test tool configuration based on selected
	 * cipher suite.
	 *
	 * @param tlsVersion the selected TLS version
	 * @param cipherSuite selected cipher suite
	 * @return the name of the selected certificate file
	 * @throws FileNotFoundException
	 */
	public final String setCertificateAndPrivateKey(final TlsVersion tlsVersion, final TlsCipherSuite cipherSuite)
			throws FileNotFoundException {
		return setCertificateAndPrivateKey(tlsVersion, cipherSuite, null, TlsTestToolCertificateTypes.CERT_DEFAULT);
	}

	/**
	 * Sets a certificate and private key to TLS test tool configuration that matches the selected signature with
	 * hash algorithm which is supported by the specified TLS version.
	 *
	 * @param tlsVersion the TLS version to get supported signature algorithm with hash.
	 * @param signatureAlgorithmWithHash the selected signature with hash algorithm.
	 * @return the name of the selected certificate file.
	 * @throws FileNotFoundException if certificate or private key file was not found.
	 */
	public final String setCertificateAndPrivateKey(final TlsVersion tlsVersion,
			final TlsSignatureAlgorithmWithHash signatureAlgorithmWithHash) throws FileNotFoundException {
		return setCertificateAndPrivateKey(tlsVersion, null, signatureAlgorithmWithHash, TlsTestToolCertificateTypes.CERT_DEFAULT);
	}

	/**
	 * Sets a certificate and private key to TLS test tool configuration that matches the selected signature with
	 * hash algorithm which is supported by the specified TLS version.
	 *
	 * @param tlsVersion the TLS version to get supported signature algorithm with hash.
	 * @param signatureAlgorithmWithHash the selected signature with hash algorithm.
	 * @param certificateType The TlsTestToolCertificateType in regard to chapter 4.2 of the TR-03116-TS of the certificate to use for the TLSTestTool.
	 * @return the name of the selected certificate file.
	 * @throws FileNotFoundException if certificate or private key file was not found.
	 */
	public final String setCertificateAndPrivateKey(final TlsVersion tlsVersion,
													final TlsSignatureAlgorithmWithHash signatureAlgorithmWithHash,
													final TlsTestToolCertificateTypes certificateType) throws FileNotFoundException {
		return setCertificateAndPrivateKey(tlsVersion, null, signatureAlgorithmWithHash, certificateType);
	}

	/**
	 * Selects and sets a certificate and private key to TLS test tool configuration that matches either the selected
	 * cipher suite (ecdsa, rsa, dsa) or the selected signature with hash algorithm. Normally, only 1 Parameter (cipher
	 * suite or signatureAlgorithmWithHash) should be != null, the other parameter should be null. If ECDSA certificate
	 * is necessary, we choose the corresponding curve from the Supported Groups configuration.
	 *
	 * @param tlsVersion the TLS version to get the supported signature algorithm with hash or cipher suite.
	 * @param cipherSuite the selected cipher suite.
	 * @param signatureAlgorithmWithHash the selected signature algorithm with hash.
	 * @param certificateType The TlsTestToolCertificateType in regard to chapter 4.2 of the TR-03116-TS of the certificate to use for the TLSTestTool.
	 * @return the name of the selected certificate file.
	 * @throws FileNotFoundException if certificate or private key file was not found.
	 */
	private String setCertificateAndPrivateKey(final TlsVersion tlsVersion,
											   final TlsCipherSuite cipherSuite,
											   final TlsSignatureAlgorithmWithHash signatureAlgorithmWithHash,
											   final TlsTestToolCertificateTypes certificateType) throws FileNotFoundException {

		// Scenario 1: ciphersuite!= null
		// given selected ciphersuite -> DSA,ECDSA,RSA
		// -> select supported hash algorithm -> from the MICS file
		// ECDSA -> select supported curve from the MICS file

		// Scenario 2: signatureAlgorithmWithHash!= null
		// given selected signatureWithHashAlgorithm
		// ECDSA -> select supported curve from the MICS file

		// Step 1 configure default values
		List<TlsSignatureAlgorithmWithHash> signatureWithHashAlgorithms
				= configuration.getSupportedSignatureAlgorithms(tlsVersion);
		if (signatureWithHashAlgorithms.isEmpty()) {
			logError("No signature with hash algorithms are found in the MICS file.");
			return "";
		}
		TlsSignatureAlgorithm defaultSignatureAlgorithm = signatureWithHashAlgorithms.get(0).getSignatureAlgorithm();

		TlsHashAlgorithm defaultHashAlgorithm = signatureWithHashAlgorithms.get(0).getHashAlgorithm();


		// TLS 1.2
		String tlsVersionString = "";
		switch (tlsVersion) {
			case SSL_V3_0:
				tlsVersionString = "";
				break;
			case TLS_V1_0:
			case TLS_V1_1:
			case TLS_V1_2:
				tlsVersionString = "tls12";
				break;
			case TLS_V1_3:
				tlsVersionString = "tls13";
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + tlsVersion);
		}

		TlsSignatureAlgorithm signatureAlgorithm = defaultSignatureAlgorithm;
		TlsHashAlgorithm hashAlgorithm = defaultHashAlgorithm;

		if (cipherSuite != null) {
			/* Find out which signature algorithm is used in selected cipher suite */
			if (TlsCipherSuite.filterByName("RSA").contains(cipherSuite)) {
				signatureAlgorithm = TlsSignatureAlgorithm.rsa;
			} else if (TlsCipherSuite.filterByName("ECDSA").contains(cipherSuite)) {
				signatureAlgorithm = TlsSignatureAlgorithm.ecdsa;
			} else if (TlsCipherSuite.filterByName("DSS").contains(cipherSuite)) {
				signatureAlgorithm = TlsSignatureAlgorithm.dsa;
			}
		} else if (signatureAlgorithmWithHash != null) {
			if (!signatureAlgorithmWithHash.isSignatureScheme()) {
				signatureAlgorithm = signatureAlgorithmWithHash.getSignatureAlgorithm();
				hashAlgorithm = signatureAlgorithmWithHash.getHashAlgorithm();
			} else {
				/* TLS 1.3 */
			}
		}

		var prefixName = String.format("test_server_%s_%s_%s", tlsVersionString,
				signatureAlgorithm.getSignatureAlgorithmValueDescription(),
				hashAlgorithm.getHashAlgorithmDescription());
		if (signatureAlgorithm == TlsSignatureAlgorithm.ecdsa) {
			List<TlsNamedCurves> defaultCurves
					= configuration.filterSupportedGroupsToEllipticCurveGroups(tlsVersion);
			TlsNamedCurves defaultCurve;
			if (defaultCurves.isEmpty()) {
				logInfo("No Elliptic Curve support in MICS. Using NIST P-256 EC Certificate.");
				defaultCurve = TlsNamedCurves.secp256r1;
			} else {
				defaultCurve = defaultCurves.get(0);
			}
			if(certificateType ==TlsTestToolCertificateTypes.CERT_SHORT_KEY){
				defaultCurve = TlsNamedCurves.secp192r1;
			}
			prefixName += "_" + defaultCurve.getName();
		}
		var certificatesBasePath = configuration.getTlsTestToolCertificatesPath();

		String certificateTypeSubfolder = certificateType.getRelativePathName();

		var certificateFile = String.format("%s/%s/%s/%s/%s_certificate.pem", certificatesBasePath,
				tlsVersionString,
				signatureAlgorithm.getSignatureAlgorithmValueDescription(),
				certificateTypeSubfolder,
				prefixName);
		var privateKeyFile = String.format("%s/%s/%s/%s/%s_private_key.pem",
				certificatesBasePath,
				tlsVersionString,
				signatureAlgorithm.getSignatureAlgorithmValueDescription(),
				certificateTypeSubfolder,
				prefixName);

		return setCertificateAndPrivateKey(certificateFile, privateKeyFile);
	}

	/**
	 * Set the certificate and private key files to TLS test tool configuration.
	 * 
	 * @param certificatePath the certificate file path
	 * @param privateKeyPath the private key file path
	 * @return the certificate file name which is added to TLS test tool configuration
	 * @throws FileNotFoundException If either the certificatePath or the privateKeyPath is not valid.
	 */
	public final String setCertificateAndPrivateKey(final String certificatePath, final String privateKeyPath)
			throws FileNotFoundException {
		if (certificatePath == null || privateKeyPath == null) {
			throw new FileNotFoundException(
					" Cannot find the specified certificate file and private key file.");
		}
		File certFile = new File(certificatePath);
		File privateKeyFile = new File(privateKeyPath);

		if (!certFile.exists()) {
			throw new FileNotFoundException(
					" Cannot find the specified certificate file: " + certFile.toString());
		}
		if (!privateKeyFile.exists()) {
			throw new FileNotFoundException(
					" Cannot find the specified private key file: " + certFile.toString());
		}
		addConfigOption(TlsTestToolConfigurationOption.PRIVATEKEYFILE, privateKeyPath);
		addConfigOption(TlsTestToolConfigurationOption.CERTIFICATEFILE, certificatePath);

		return certFile.getName();
	}


	/**
	 * Enable the verification of peer certificates. A CA certificate has to be contained in the global parameters.
	 *
	 * @see #setCaCertificateFile(Path)
	 * @see #tlsVerifyPeer()
	 */
	public final void enablePeerVerification() {
		tlsVerifyPeer();
	}


	/**
	 * Method takes the values for DUT IP-address and port from global parameters and specifies host and port for the
	 * TLS Test Tool when used as a client.
	 */
	public final void setClientHostAndPort() {
		checkClientMode();
		final String host = configuration.getDutAddress();
		final String port = configuration.getDutPort();
		logInfo("Setup DUT Server address and port to: " + host + ":" + port);
		setHostAndPort(host, port);
	}


	/**
	 * Method takes the value for port from global parameters and specifies host=127.0.0.1 and taken port for the TLS
	 * Test Tool when used as a server.
	 */
	public final void setServerHostAndPort() {
		checkServerMode();
		final String port = Integer.toString(configuration.getTlsTestToolPort());
		logInfo("Setup TLS Test Tool address and port to: " + TLS_TEST_TOOL_LOCAL_HOST_AS_SERVER + ":" + port);
		setHostAndPort(TLS_TEST_TOOL_LOCAL_HOST_AS_SERVER, port);
	}

	private final void checkServerMode() {
		checkMode(TlsTestToolMode.server);
	}

	private final void checkClientMode() {
		checkMode(TlsTestToolMode.client);
	}

	private void checkMode(final TlsTestToolMode mode) {
		if (this.mode == null) {
			throw new IllegalArgumentException("TLS Test Tool mode is not specified. Please set TLS Test Tool mode.");
		}
		if (this.mode != mode) {
			throw new IllegalArgumentException("TLS Test Tool mode is invalid.");
		}
	}


	/**
	 * Specify host and port for the TLS Test Tool.
	 *
	 * @param host host holds the host name
	 * @param port port holds the port number
	 */
	private final void setHostAndPort(final String host, final String port) {
		setHost(host);
		setPort(port);
	}

	/**
	 * Specify host for the TLS Test Tool.
	 *
	 * @param host host holds the host name.
	 */
	private void setHost(final String host) {
		addConfigOption(TlsTestToolConfigurationOption.HOST, host);
	}

	/**
	 * Specify port for the TLS Test Tool.
	 *
	 * @param port port holds the port number.
	 */
	private void setPort(final String port) {
		addConfigOption(TlsTestToolConfigurationOption.PORT, port);
	}


	/**
	 * Method resets the properties used within test cased execution. The optional parameter is to be used to avoid that
	 * log messages are going to be deleted. Instead of a log reset log messages will be collected in log history and
	 * can be used later on.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public final void resetProperties() throws IOException {
		logDebug("Reset current TLS Test Tool configuration.");
		processLoggingOutput();
		// Check if there is a need for storing log messages for later usage.
		// Simply support only one element in the optional parameter
		if (null != getIterationCounter() && processLoggingOutputDone) {
			writeLogsToFile((ArrayList<LogBean>) getLogBeanList().clone());
		}
		resetLog();
		config.clear();
	}

	/**
	 * Saves initial handshake logs to the log file and clears them from logBeanList. Please note that, at this point
	 * TLS Test Tool process is still running and not exited.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public final void saveInitialHandshakeLogs() throws IOException {
		writeLogsToFile((ArrayList<LogBean>) getLogBeanList().clone());
		clearLogBeanList();
		clearLogList();
	}


	/**
	 * Returns the configuration.
	 *
	 * @return configuration
	 */
	@Override
	public final TestRunPlanConfiguration getConfiguration() {
		return TestRunPlanConfiguration.getInstance();
	}


	/**
	 * Set a single cipher suite to property file.
	 *
	 * @param cipherSuite the cipher suite
	 */
	public final void setCipherSuite(final TlsCipherSuite cipherSuite) {
		setCipherSuite(Collections.singletonList(cipherSuite));
	}


	/**
	 * Set cipher suite(s) using their IDs e.g. (0x00, 0x01), (0xC0,0xAD), ... to the list of supported
	 * cipher suites.
	 *
	 * @param cipherSuite the cipher suite to work with
	 */
	public final void setCipherSuite(final String cipherSuite) {
		addConfigOption(TlsTestToolConfigurationOption.TLSCIPHERSUITES, cipherSuite);
	}


	/**
	 * Sets all cipher suites received within a list to the property file.
	 *
	 * @param cipherSuites the list of cipher suites to work with
	 */
	public final void setCipherSuite(final List<TlsCipherSuite> cipherSuites) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cipherSuites.size(); i++) {
			if (0 != i) {
				sb.append(',');
			}
			sb.append(cipherSuites.get(i).getValuePair());
		}
		setCipherSuite(sb.toString());
	}


	/**
	 * Set the session cache for a session resumption handshake
	 *
	 * @param sessionCache
	 */
	public final void setSessionCache(final String sessionCache) {
		addConfigOption(TlsTestToolConfigurationOption.SESSIONCACHE, sessionCache);
	}


	/**
	 * Set the handshake type
	 *
	 * @param handshakeType
	 */
	public final void setSessionHandshakeType(final TlsTestToolConfigurationHandshakeType handshakeType) {
		addConfigOption(TlsTestToolConfigurationOption.HANDSHAKETYPE, handshakeType.toString());
	}


	/**
	 * Set the early data
	 *
	 * @param earlyData
	 */
	public final void setEarlyData(final String earlyData) {
		addConfigOption(TlsTestToolConfigurationOption.EARLYDATA,
				earlyData);
	}


	/**
	 * Set the session lifetime (maximum time the peer is allowed to keep the session alive)
	 *
	 * @param sessionLifetime
	 */
	public final void setSessionLifetime(final long sessionLifetime) {
		addConfigOption(TlsTestToolConfigurationOption.SESSIONLIFETIME,
				Long.toString(sessionLifetime));
	}

	/**
	 * This pattern verifies, that a non-empty string is compliant to the HexString parameter type.
	 */
	private final Pattern hexString = Pattern.compile("^[0-9a-f][0-9a-f]([ ][0-9a-f][0-9a-f])*$");

	/**
	 * Checks, and if necessary, converts the given input into a format, which complies to the HexString format of the
	 * TLS Test Tool.
	 *
	 * @param input the input
	 * @return HexString.
	 */
	private String asHexString(final String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		if (hexString.matcher(input).find()) {
			return input;
		}
		final String mayBeBetter = input.toLowerCase().replace(" ", "");
		final StringBuffer looper = new StringBuffer();
		short blockSize = 0;
		for (final char someChar : mayBeBetter.toCharArray()) {
			if (blockSize == 2) {
				looper.append(' ');
				blockSize = 0;
			}
			looper.append(someChar);
			blockSize++;
		}
		if (hexString.matcher(looper).find()) {
			return looper.toString();
		} else {
			throw new IllegalArgumentException("Value '" + input + "' can not be used as a HexString");
		}
	}


	/**
	 * Utility method to generate a common log for a contains check of a list.
	 *
	 * @param list containing all possible values
	 * @param value to be found
	 */
	public final void assertContains(final List<?> list, final Object value) {
		if (value == null) {
			logError("Value was not found, value is null.");
			return;
		}

		if (list.contains(value)) {
			logInfo("Value '" + value + "' was found.");
		} else {
			logInfo("List content:");
			for (final Object object : list) {
				logInfo("\t" + object);
			}
			logInfo("-------------");
			logError("Value '" + value + "' was not found.");
		}
	}


	/**
	 * manipulateClientHelloExtensions=[bytes] (with bytes given as HexString) If mode=client, overwrite the field
	 * extensions in a ClientHello message with the byte array given in bytes. Ignored, if mode=server.
	 *
	 * @param hexString hexString for example "00 0A 00 0B"
	 */
	private void manipulateClientHelloExtensions(final String hexString) {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATECLIENTHELLOEXTENSIONS, hexString);
	}


	/**
	 * Overloaded function for manipulating ClientHello extensions. Add SNI extension if configured in the global
	 * parameters because this overwrites the present extensions.
	 *
	 * @param extensionList TLS extension list
	 */
	public final void manipulateClientHelloExtensions(final TlsExtensionList extensionList) {
		manipulateClientHelloExtensions(extensionList, true);
	}


	/**
	 * Overloaded function for manipulating ClientHello extensions. Add SNI extension if configured in the global
	 * parameters because this overwrites the present extensions.
	 *
	 * @param extensionList TLS extension list
	 * @param addSniExtensionIfConfigured Indicates whether the SNI extension is will be set if configured.
	 */
	public final void manipulateClientHelloExtensions(final TlsExtensionList extensionList,
			final boolean addSniExtensionIfConfigured) {

		if (addSniExtensionIfConfigured) {
			// Add SNI extension if configured in the global parameters

			final boolean tlsUseSni = configuration.getTlsUseSni();
			if (tlsUseSni) {
				extensionList.add(new TlsExtServerName(configuration.getDutAddress()));
				logInfo("Add SNI (server name indication) extension to client hello extensions!");
			}
		}
		manipulateClientHelloExtensions(extensionList.toHexString());
	}

	/**
	 * Overloaded function for manipulating ClientHello extensions. Add SNI extension if configured in the global
	 * parameters because this overwrites the present extensions.
	 *
	 * @param extensionList TLS extension list
	 */
	public final void manipulateServerHelloExtensions(final TlsExtensionList extensionList) {

		manipulateServerHelloExtensions(extensionList.toHexString());
	}

	/**
	 * manipulateClientHelloExtensions=[bytes] (with bytes given as HexString) If mode=client, overwrite the field
	 * extensions in a ClientHello message with the byte array given in bytes. Ignored, if mode=server.
	 *
	 * @param hexString hexString for example "00 0A 00 0B"
	 */
	private void manipulateServerHelloExtensions(final String hexString) {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATESERVERHELLOEXTENSIONS, hexString);
	}


	/**
	 * manipulateHelloVersion=([major],[minor]) (with hexadecimal integers major and minor preceded with 0x) The two
	 * bytes given in major and minor define a possibly invalid protocol version. If mode= client, replace the field
	 * client_version in a ClientHello message with the given protocol version. If mode=server, replace the field
	 * server_version in a ServerHello message with the given protocol version.
	 *
	 * @param major Protocol version major
	 * @param minor protocol version minor
	 */
	public final void manipulateHelloVersion(final int major, final int minor) {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATEHELLOVERSION,
				String.format("(0x%02x,0x%02x)", major, minor));
	}


	/**
	 * manipulateSendHeartbeatRequest=[ignored] (with an arbitrary, possibly empty value ignored) Send a
	 * HeartbeatRequest message before starting a TLS handshake.
	 *
	 * @param when specifies, when to send the heartbeat request (before or after the handshake).
	 * @param length payload_length.
	 * @param data payload to be sent
	 */
	public final void manipulateSendHeartbeatRequest(final String when, final String length, final String data) {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATESENDHEARTBEATREQUEST,
				when + "," + length + "," + asHexString(data));
	}


	/**
	 * manipulateRenegotiate=[ignored] (with an arbitrary, possibly empty value ignored) Perform a TLS renegotiation
	 * after a successful TLS handshake.If mode=client, this means sending a ClientHello message. If mode=server, this
	 * means sending a HelloRequest message.
	 */
	public final void manipulateRenegotiate() {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATERENEGOTIATE, "");
	}


	/**
	 * Overwrite the field compression_methods in a ClientHello message with the byte array given in bytes.
	 *
	 * @param compression the compression method
	 */
	public final void manipulateClientHelloCompressionMethods(final String compression) {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATECLIENTHELLOCOMPRESSIONMETHODS,
				asHexString(compression));
	}


	/**
	 * Utility method to build compression lists.
	 *
	 * @param compressions Specified resources must prefix with Compression_*
	 * @throws IllegalArgumentException
	 */
	public final void manipulateClientHelloCompressionMethods(final TestToolResource... compressions) {
		final StringBuilder compression = new StringBuilder();

		for (final TestToolResource comp : compressions) {
			if (!comp.name().startsWith("Compression_")) {
				throw new IllegalArgumentException("Only compression constants are supported.");
			}
			compression.append(' ');
			compression.append(comp.getInternalToolOutputMessage());
		}
		manipulateClientHelloCompressionMethods(compression.toString().trim());
	}

	/**
	 * Utility method to build compression.
	 *
	 * @param compression Specified resources must prefix with Compression_*
	 * @throws IllegalArgumentException
	 */
	public final void manipulateServerHelloCompressionMethod(final TestToolResource compression) {

		if (!compression.name().startsWith("Compression_")) {
			throw new IllegalArgumentException("Only compression constants are supported.");
		}
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATESERVERHELLOCOMPRESSIONMETHOD,
				asHexString(compression.getInternalToolOutputMessage()));
	}

	/**
	 * Utility method to force certificate usage of TlsTestTool server
	 */
	public final void manipulateForceCertificateUsage() {
		addConfigOption(TlsTestToolConfigurationOption.MANIPULATEFORCECERTIFICATEUSAGE, "");
	}


	/**
	 * Append the master_secret in the NSS Key Log Format to a plain text file. This file can be used by Wireshark to
	 * decrypt TLS packages.<br>
	 * <br>
	 * This is usually only used for debug purposes.
	 *
	 * @param path to the file
	 */
	public final void setTlsSecretFile(final Path path) {
		addConfigOption(TlsTestToolConfigurationOption.TLSSECRETFILE, path.toString());
	}


	/**
	 * Set the time to specify how many seconds to wait for the peer before closing the connection.
	 *
	 * @param timeout The waiting time in seconds
	 */
	public final void setWaitBeforeClose(final int timeout) {
		addConfigOption(TlsTestToolConfigurationOption.WAITBEFORECLOSE, Integer.toString(timeout));
	}


	/**
	 * Get the in seconds to wait for the peer before closing the connection.
	 *
	 * @return The waiting time in seconds
	 */
	public final String getWaitBeforeClose() {
		return config.getOption(TlsTestToolConfigurationOption.WAITBEFORECLOSE);
	}


	/**
	 * Set a single Signature Scheme to property file.
	 *
	 * @param signatureScheme The Signature Scheme
	 */
	public final void setSignatureScheme(final TlsSignatureScheme signatureScheme) {
		setSignatureSchemes(Collections.singletonList(signatureScheme));
	}


	/**
	 * Set one or more Signature Scheme(s) using their IDs e.g. (0x08,0x04), (0x04,0x03), ... to the list of supported
	 * Signature Schemes.
	 *
	 * @param signatureScheme Signature Schemes to work with
	 */
	public final void setSignatureScheme(final String signatureScheme) {
		addConfigOption(TlsTestToolConfigurationOption.TLSSIGNATURESCHEMES, signatureScheme);
	}


	/**
	 * Sets all cipher suites received within a list to the property file.
	 *
	 * @param signatureSchemes the list of cipher suites to work with
	 */
	public final void setSignatureSchemes(final List<TlsSignatureScheme> signatureSchemes) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < signatureSchemes.size(); i++) {
			if (0 != i) {
				sb.append(',');
			}
			sb.append(signatureSchemes.get(i).getValuePair());
		}
		setSignatureScheme(sb.toString());
	}


	/**
	 * Set the default settings for running as TLS client.
	 * <ul>
	 * <li>sets mode = client
	 * <li>sets host and port from configuration
	 * </ul>
	 *
	 * @throws Exception
	 */
	public final void defaultClientConfiguration() throws Exception {
		setMode(TlsTestToolMode.client);
		setLogLevel(TlsTestToolLogLevel.high);
	}


	/**
	 * Check, if the field ServerHello.cipher_suites contains the given cipher suites.
	 *
	 * @param expectedCipherSuite The cipher suites to search in the field
	 * @throws IOException
	 */
	public final void assertServerHelloCipherSuitesContains(final TlsCipherSuite expectedCipherSuite)
			throws IOException {
		final String serverHelloCipherSuite = getValue(TestToolResource.ServerHello_cipher_suite);
		if (null == serverHelloCipherSuite) {
			logError("Cannot check " + TestToolResource.ServerHello_cipher_suite.getInternalToolOutputMessage()
					+ ".");
		} else {
			if (serverHelloCipherSuite.contains(expectedCipherSuite.getValueHexString())) {
				logInfo("Cipher suite " + expectedCipherSuite + " found in "
						+ TestToolResource.ServerHello_cipher_suite.getInternalToolOutputMessage() + ".");
			} else {
				logError("Cipher suite " + expectedCipherSuite + " not found in "
						+ TestToolResource.ServerHello_cipher_suite.getInternalToolOutputMessage() + ".");
			}
		}
	}


	/**
	 * Method searches a given message constant within gathered logging and returns the proper finding or the closest
	 * message which was found. Logs an error if the message was not found.
	 *
	 * @param message The message to search for as constant
	 * @return true, if the message was found
	 * @throws IOException
	 */
	public final boolean assertMessageLogged(final TestToolResource message) throws IOException {
		return assertMessageLogged(message.getInternalToolOutputMessage(), BasicLogger.ERROR);
	}


	/**
	 * Method searches a given message constant within gathered logging and returns the proper finding or the closest
	 * message which was found.
	 *
	 * @param message The message to search for as constant
	 * @param logLevel The log level in case that the message was not found taken from {@link LoggingConnector}
	 * @return true, if the message was found
	 * @throws IOException
	 */
	public final boolean assertMessageLogged(final TestToolResource message, final long logLevel)
			throws IOException {
		return assertMessageLogged(message.getInternalToolOutputMessage(), logLevel);
	}


	/**
	 * Method searches a given message identifier by using text constants within gathered logging and returns the proper
	 * finding or the closest message which was found.
	 *
	 * @param level to assert, <code>null</code> will only log assert level
	 * @param description the description
	 * @param logLevel The log level in case that the alert was not found taken from {@link LoggingConnector}
	 * @return true, if the alert was found
	 * @throws IOException
	 */
	public final boolean assertAlertLogged(final TlsAlertLevel level, final TlsAlertDescription description,
			final long logLevel) throws IOException {
		boolean alertLogged = assertMessageLogged(TestToolResource.Alert_message_received, logLevel);
		if (null != level) {
			logInfo("Expected: Alert.level=" + level + ".");
			final byte[] bytesLevel = getHexStringValue(TestToolResource.Alert_level, logLevel);
			if (null == bytesLevel || 1 != bytesLevel.length) {
				alertLogged = false;
				log(logLevel, "Actual: Alert.level not found.");
			} else if (level.toNumber() == bytesLevel[0]) {
				logInfo("Actual: Alert.level=" + level + ".");
				if (null == description) {
					final byte[] bytesDesc = getHexStringValue(TestToolResource.Alert_description, logLevel);
					if (null == bytesDesc || 1 != bytesDesc.length) {
						log(logLevel, "Actual: Alert.description not found.");
					} else {
						logInfo("Actual: Alert.description=" + TlsAlertDescription.converteValue(bytesDesc[0]) + ".");
					}
				}

			} else {
				alertLogged = false;
				boolean found = false;
				for (final TlsAlertLevel potentialLevel : TlsAlertLevel.values()) {
					if (potentialLevel.toNumber() == bytesLevel[0]) {
						alertLogged = false;
						log(logLevel, "Actual: Alert.level=" + potentialLevel + ".");
						found = true;
						break;
					}
				}
				if (!found) {
					log(logLevel, "Actual: Alert.level=" + bytesLevel[0] + ".");
				}
			}
		}
		if (null != description) {
			logInfo("Expected: Alert.description=" + description + ".");
			final byte[] bytesLevel = getHexStringValue(TestToolResource.Alert_description, logLevel);
			if (null == bytesLevel || 1 != bytesLevel.length) {
				alertLogged = false;
				log(logLevel, "Actual: Alert.description not found.");
			} else if (description.toNumber() == bytesLevel[0]) {
				logInfo("Actual: Alert.description=" + description + ".");
			} else {
				alertLogged = false;
				boolean found = false;
				for (final TlsAlertDescription potentialLevel : TlsAlertDescription.values()) {
					if (potentialLevel.toNumber() == bytesLevel[0]) {
						log(logLevel, "Actual: Alert.description=" + potentialLevel + ".");
						found = true;
						break;
					}
				}
				if (!found) {
					alertLogged = false;
					log(logLevel, "Actual: Alert.description=" + bytesLevel[0] + ".");
				}
			}
		}
		return alertLogged;
	}


	/**
	 * Check, if a fatal Alert message has been received. If it is not received, an error will be logged.
	 *
	 * @param logLevel The log level in case that the alert was not found taken from {@link LoggingConnector}
	 * @return true, if a fatal alert was received
	 * @throws IOException
	 */
	public final boolean assertFatalAlertReceived(final long logLevel) throws IOException {
		return assertAlertLogged(TlsAlertLevel.fatal, null, logLevel);
	}


	/**
	 * Method searches a given message identifier by using text constants within gathered logging and returns the proper
	 * finding or the closest message which was found.
	 *
	 * @param level to assert, <code>null</code> will only log assert level
	 * @param description the description
	 * @return returns the proper finding or the closest message which was found.
	 * @throws IOException
	 */
	public final boolean assertAlertLogged(final TlsAlertLevel level, final TlsAlertDescription description)
			throws IOException {
		return assertAlertLogged(level, description, BasicLogger.ERROR);
	}


	/**
	 * Check, if a fatal Alert message has been received. If it is not received, an error will be logged.
	 *
	 * @return true, if a fatal alert was received
	 * @throws IOException
	 */
	public final boolean assertFatalAlertReceived() throws IOException {
		return assertFatalAlertReceived(BasicLogger.ERROR);
	}


	/**
	 * @return the extension types sent by server or client and checks if the given extension type is found. The
	 * extracted result is returned or null in case that no log message matches the desired extension type.
	 * @param tlsTestToolMode The TLS role of the TLS Test Tool
	 * @param expectedExtensionType The extension type to search for
	 * @throws IOException if an error occurs while reading the log information
	 */
	public final byte[] assertExtensionTypeLogged(final TlsTestToolMode tlsTestToolMode,
			final TlsExtensionTypes expectedExtensionType) throws IOException {
		String searchString;
		if (TlsTestToolMode.client == tlsTestToolMode) {
			searchString = TestToolResource.ClientHello_extensions.getInternalToolOutputMessage();
		} else if (TlsTestToolMode.server == tlsTestToolMode) {
			searchString = TestToolResource.ServerHello_extensions.getInternalToolOutputMessage();
		} else {
			throw new IllegalArgumentException("TLS Test Tool mode is invalid");
		}
		logDebug("Analyzing the value of " + searchString + ".");
		final LogBean logBean = findMessage(searchString);
		return extractExtensionData(searchString, logBean, expectedExtensionType);
	}


	/**
	 * @return the extension types as a string (Hex) value sent by server or client.
	 * @param tlsTestToolMode The TLS role of the TLS Test Tool
	 * @throws IOException if an error occurs reading the log information
	 */
	public final String getExtensions(final TlsTestToolMode tlsTestToolMode) throws IOException {
		String searchString;
		if (TlsTestToolMode.client == tlsTestToolMode) {
			searchString = TestToolResource.ClientHello_extensions.getInternalToolOutputMessage();
		} else if (TlsTestToolMode.server == tlsTestToolMode) {
			searchString = TestToolResource.ServerHello_extensions.getInternalToolOutputMessage();
		} else {
			throw new IllegalArgumentException("TLS Test Tool mode is invalid");
		}
		logDebug("Analyzing the value of " + searchString + ".");
		final LogBean logBean = findMessage(searchString);
		return extractExtensionData(logBean);
	}


	private String extractExtensionData(final LogBean logBean) {
		if (null != logBean) {
			final String[] messageParts = logBean.getMessage().split("=", 2);
			if (messageParts.length == 2) {
				String extensionsInHEX = messageParts[1].replace(" ", "");
				if (4 > extensionsInHEX.length()) {
					logError("Extensions value too short");
					return "";
				}
				return extensionsInHEX;
			}
		}
		return "";
	}


	/**
	 * Get the value of a key-value-pair in the log.
	 *
	 * @param key The Key value to search for
	 * @param logLevel The log level in case that nothing was found taken from {@link LoggingConnector}
	 * @return value as String, else null.
	 * @throws IOException
	 */
	public final List<String> getValues(final String key, final long logLevel)
			throws IOException {
		final List<String> foundMatches = new ArrayList<>();

		logInfo("Searching for '" + key + "'");
		final List<LogBean> logEntries = findMessages(key);

		if (logEntries != null) {
			for (final LogBean logEntry : logEntries) {
				final String[] valuePair = logEntry.getMessage().split("=");

				if (valuePair.length == 2) {
					logInfo("found value: " + valuePair[1]);
					foundMatches.add(valuePair[1]);
				} else {
					log(logLevel,
							"Couldn't convert key: " + key + ". The expected syntax differs from the found one .");
				}
			}
		}
		if (foundMatches.isEmpty()) {
			logInfo("Couldn't find key: " + key);
			return null;
		}
		return foundMatches;
	}


	/**
	 * Get values of a key-value-pair out of the logged information.
	 *
	 * @param constant taken from {@link TestToolResource} to search for
	 * @param logLevel the log level in case that nothing was found taken from {@link LoggingConnector}
	 * @return values as the list of strings, else null.
	 * @throws IOException
	 */
	public final List<String> getValues(final TestToolResource constant, final long logLevel)
			throws IOException {
		return getValues(constant.getInternalToolOutputMessage(), logLevel);
	}


	/**
	 * Method searches the extension types sent by server or client. The extracted result is returned or null in case
	 * that no log message matches the desired extension type.
	 *
	 * @param tlsTestToolMode The TLS role of the TLS Test Tool
	 * @param expectedExtensionType The extension type to search for
	 * @return the extension data
	 * @throws IOException if an error occurs reading the log information
	 */
	public final byte[] findExtensionTypeLogged(final TlsTestToolMode tlsTestToolMode,
			final TlsExtensionTypes expectedExtensionType) throws IOException {
		String searchString;
		if (TlsTestToolMode.client == tlsTestToolMode) {
			searchString = TestToolResource.ClientHello_extensions.getInternalToolOutputMessage();
		} else if (TlsTestToolMode.server == tlsTestToolMode) {
			searchString = TestToolResource.ServerHello_extensions.getInternalToolOutputMessage();
		} else {
			throw new IllegalArgumentException("TLS Test Tool role is invalid");
		}
		return findExtensionTypeLogged(searchString, expectedExtensionType);
	}


	/**
	 * Method searches the extension types sent by server or client.
	 *
	 * @return The extracted result is returned or null in case that no log message matches the desired extension type.
	 * @param searchString the log message keyword to search for
	 * @param expectedExtensionType The extension type to search for
	 * @throws IOException
	 */
	public final byte[] findExtensionTypeLogged(final String searchString,
			final TlsExtensionTypes expectedExtensionType) throws IOException {
		logDebug("Analyzing the value of " + searchString + ".");
		final LogBean logBean = findMessage(searchString);
		if (null != logBean) {
			final String[] messageParts = logBean.getMessage().split("=", 2);
			if (messageParts.length == 2) {
				final byte[] extensions = StringTools.toByteArray(messageParts[1].replace(" ", ""));
				if (4 > extensions.length) {
					return null;
				}
				for (int i = 0; i < extensions.length;) {
					final TlsExtensionTypes actualExtensionType = TlsExtensionTypes.valueOf(extensions[i],
							extensions[i + 1]);
					final int length = (extensions[i + 2] & A_0XFF) << 8 | extensions[i + 3] & A_0XFF;
					if (null != actualExtensionType && expectedExtensionType == actualExtensionType) {
						logDebug("Extension " + expectedExtensionType.getExtensionDescriptionValue() + " with length "
								+ length + " found.");
						return Arrays.copyOfRange(extensions, i + 4, i + 4 + length);
					}
					i += 4 + length;
				}
			}
		}
		return null;
	}


	/**
	 * Check in the log output, if the TLS server does not support the given TLS extension. Report the result in the
	 * test case log.
	 *
	 * @param expectedExtension TLS extension to search for
	 * @throws IOException
	 */
	public final void assertServerLacksExtension(final TlsExtensionTypes expectedExtension) throws IOException {
		assertMessageLogged(TestToolResource.ServerHello_valid);
		if (null == assertExtensionTypeLogged(TlsTestToolMode.server, expectedExtension)) {
			logInfo("The extension " + expectedExtension + " is not supported by the TLS server.");
		} else {
			logError("The extension " + expectedExtension + " is supported by the TLS server.");
		}
	}

	/**
	 * Check in the log output, if the TLS client does not support the given TLS extension. Report the result in the
	 * test case log.
	 *
	 * @param expectedExtension TLS extension to search for
	 * @throws IOException
	 */
	public final void assertClientLacksExtension(final TlsExtensionTypes expectedExtension) throws IOException {
		if (null == assertExtensionTypeLogged(TlsTestToolMode.client, expectedExtension)) {
			logInfo("The extension " + expectedExtension + " is not supported by the DUT.");
		} else {
			logError("The extension " + expectedExtension + " is supported by the DUT.");
		}
	}


	/**
	 * Get the value of a key-value-pair in the log and returns the found value or null in case that nothing was found.
	 * In case that an element was not found simple an 'info' is generated in order to don't have any impact on the
	 * test case execution result.
	 *
	 * @param constant The constant value to search for
	 * @return found value or null.
	 * @throws IOException
	 */
	protected final String getOptionalValue(final TestToolResource constant) throws IOException {
		return getOptionalValue(constant.getInternalToolOutputMessage());
	}


	/**
	 * Search for the size of the peer's certificate list and return it as an integer.
	 *
	 * @return Size of the certificate list, if successful. -1 on error.
	 * @throws IOException
	 */
	private int getCertificateListSize() throws IOException {
		final String listSizeStr = getValue(TestToolResource.Certificate_list_size);
		if (null == listSizeStr) {
			// getValue has already logged an error message
			return -1;
		}
		try {
			return Integer.parseInt(listSizeStr);
		} catch (final NumberFormatException e) {
			logError("Value of " + TestToolResource.Certificate_list_size.getInternalToolOutputMessage()
					+ " cannot be parsed as integer", e);
		}
		return -1;
	}


	/**
	 * Search the log for a given key and return its hexadecimal string value converted to an array of bytes.
	 *
	 * @param key Key to search the log for
	 * @return Array of bytes as represented by the hexadecimal string, if successful. {@code null}, if the key could
	 * not be found, or the value could not be parsed.
	 * @throws IOException
	 * @see #getValue(TestToolResource)
	 */
	protected final byte[] getHexStringValue(final TestToolResource key) throws IOException {
		return getHexStringValue(key, BasicLogger.ERROR);
	}


	/**
	 * Search the log for a given key and return its hexadecimal string value converted to an array of bytes.
	 *
	 * @param key Key to search the log for
	 * @param logLevel The log level taken from {@link LoggingConnector}
	 * @return Array of bytes as represented by the hexadecimal string, if successful. {@code null}, if the key could
	 * not be found, or the value could not be parsed.
	 * @throws IOException
	 * @see #getValue(TestToolResource)
	 */
	protected final byte[] getHexStringValue(final TestToolResource key, final long logLevel) throws IOException {
		final String strValue = getValue(key, logLevel);
		if ((null == strValue) || !strValue.matches("^(\\p{XDigit}{2} ?)*$")) {
			return null;
		}
		return StringTools.toByteArray(strValue.replace(" ", ""));
	}


	/**
	 * Get the value of a key-value-pair in the log and returns the found value or null in case that nothing was found.
	 *
	 * @param constant The constant value to search for
	 * @return found value or null.
	 * @throws IOException
	 */
	public final String getValue(final TestToolResource constant) throws IOException {
		return getValue(constant, BasicLogger.ERROR);
	}


	/**
	 * Get the value of a key-value-pair in the log and returns the found value or null in case that nothing was found.
	 *
	 * @param constant The constant value to search for
	 * @param logLevel The log level in case that nothing was found taken from {@link LoggingConnector}
	 * @return found value or null.
	 * @throws IOException
	 */
	protected final String getValue(final TestToolResource constant, final long logLevel) throws IOException {
		return getValue(constant.getInternalToolOutputMessage(), logLevel);
	}

	/**
	 * Gets the <b>"sessionCache"</b> message from the log messages.
	 *  
	 * @param logLevel the log level.
	 * @return a list of found messages or {@code null} if the log message was not found. 
	 * 
	 */
	public final List<String> getSessionCacheFromLog(final long logLevel) throws IOException {
		final String sessionCacheKey = "sessionCache";
		final List<String> foundMatches = new ArrayList<>();

		logInfo("Searching for '" + sessionCacheKey + "'.");
		final List<LogBean> logEntries = findMessages(sessionCacheKey);

		if (logEntries != null) {
			for (final LogBean logEntry : logEntries) {
				int index = logEntry.getMessage().indexOf("=");
				if (index == -1) {
					log(logLevel, "Couldn't convert key: " + sessionCacheKey
							+ ". The expected syntax differs from the found one.");
				}
				foundMatches.add(logEntry.getMessage().substring(index + 1));
			}
		}
		if (foundMatches.isEmpty()) {
			logInfo("Couldn't find key: " + sessionCacheKey);
			return null;
		}
		return foundMatches;
	}


	/**
	 * Search for the delivered Server certificates.
	 *
	 * @return list of certificates, no certificates leads to empty list.
	 * @throws IOException
	 */
	public final List<X509Certificate> findServerCertificateList() throws IOException {
		final int size = getCertificateListSize();
		if (-1 == size) {
			return Collections.emptyList();
		}
		final List<X509Certificate> certList = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			final String certHex = getValue("Certificate.certificate_list[" + i + "]");
			if (null != certHex) {
				certList.add(CertificateHelper.parseData(certHex));
			}
		}
		return certList;
	}


	/**
	 * Check in the log output, if the TLS server supports the given TLS extension. Report the result in the test case
	 * log.
	 *
	 * @param expectedExtension TLS extension to search for
	 * @throws IOException
	 */
	public final void assertServerSupportsExtension(final TlsExtensionTypes expectedExtension) throws IOException {
		assertMessageLogged(TestToolResource.ServerHello_valid);
		if (null == assertExtensionTypeLogged(TlsTestToolMode.server, expectedExtension)) {
			logError("The extension " + expectedExtension + " is not supported by the TLS server.");
		} else {
			logInfo("The extension " + expectedExtension + " is supported by the TLS server.");
		}
	}

	/**
	 * Check in the log output, if the TLS client supports the given TLS extension. Report the result in the test case
	 * log.
	 *
	 * @param expectedExtension TLS extension to search for
	 * @throws IOException
	 */
	public final void assertClientSupportsExtension(final TlsExtensionTypes expectedExtension)
			throws IOException {
		if (null == assertExtensionTypeLogged(TlsTestToolMode.client, expectedExtension)) {
			logError("The extension " + expectedExtension + " is not supported by the TLS client.");
		} else {
			logInfo("The extension " + expectedExtension + " is supported by the TLS client.");
		}
	}


	/**
	 * Check in the log output, if the TLS server supports the given TLS extension. Report the result in the test case
	 * log.
	 *
	 * @param expectedExtension TLS extension to search for
	 * @throws IOException
	 */
	public boolean assertServerSupportsEncryptedExtensions(final TlsExtensionTypes expectedExtension)
			throws IOException {
		boolean encExtMsgLogged = assertMessageLogged(
				TestToolResource.EncryptedExtension_valid.getInternalToolOutputMessage());
		if (!encExtMsgLogged) {
			logError("The EncryptedExtension message was not received.");
			return false;
		}
		final LogBean logBean = findMessage(
				TestToolResource.EncryptedExtension_extensions.getInternalToolOutputMessage());
		if (getValue(TestToolResource.EncryptedExtension_extensions.getInternalToolOutputMessage()) == null) {
			return false;
		}
		return null != extractExtensionData(
				TestToolResource.EncryptedExtension_extensions.getInternalToolOutputMessage(), logBean,
				expectedExtension);
	}


	/**
	 * Search a given message identifier within log output and log the result as information.
	 *
	 * @param message The message to search for
	 * @throws IOException
	 */
	protected final void infoMessageLogged(final TestToolResource message) throws IOException {
		infoMessageLogged(message.getInternalToolOutputMessage());
	}


	/**
	 * Utility method, which forwards to {@link #findMessage(String)} returns either the found element or null.
	 *
	 * @param message input to search for within list of logging data
	 * @return found element or null.
	 * @throws IOException
	 */
	public final LogBean findMessage(final TestToolResource message)
			throws IOException {
		return findMessage(message, true);
	}


	/**
	 * Utility method, which forwards to {@link #findMessage(String)} returns either the found element or null.
	 *
	 * @param message input to search for within list of logging data
	 * @param handleNoLogAsError This method allows the user to decide if a missing log leads to an error or not e.g.
	 * due to an expected TLS Test Tool abortion.
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final LogBean findMessage(final TestToolResource message, final boolean handleNoLogAsError)
			throws IOException {
		return findMessage(message.getInternalToolOutputMessage(), handleNoLogAsError);
	}


	private byte[] extractExtensionData(final String searchString, final LogBean logBean,
			final TlsExtensionTypes expectedExtensionType) {
		if (null == logBean) {
			logError("Log message for " + searchString + " not found.");
		} else {
			final String[] messageParts = logBean.getMessage().split("=", 2);
			if (messageParts.length == 2) {
				final byte[] extensions = StringTools.toByteArray(messageParts[1].replace(" ", ""));
				if (4 > extensions.length) {
					logError("Extensions value is too short.");
					return null;
				}
				for (int i = 0; i < extensions.length;) {
					final TlsExtensionTypes actualExtensionType = TlsExtensionTypes.valueOf(extensions[i],
							extensions[i + 1]);
					final int length = (extensions[i + 2] & A_0XFF) << 8 | extensions[i + 3] & A_0XFF;
					if (null != actualExtensionType && expectedExtensionType == actualExtensionType) {
						logDebug("Extension " + expectedExtensionType.getExtensionDescriptionValue() + " with length "
								+ length + " found.");
						return Arrays.copyOfRange(extensions, i + 4, i + 4 + length);
					}
					i += 4 + length;
				}
			}
		}
		return null;

	}

	/**
	 * Returns {@code true} if the process is not NULL and still running.
	 *
	 * @return {@code true} if the process is not NULL and currently running, false otherwise.
	 */
	@Override
	public final boolean isRunning() {
		return super.isRunning();
	}

	@Override
	public final void logEndOfIteration(final Writer writer) throws IOException {
		if (null != getIterationCounter()) {
			logInfo("End iteration " + getIterationCounter().getCurrentIteration() + " of "
					+ getIterationCounter().getTotalNumberOfIterations() + ".");
		}

	}

	/**
	 * Gets the TLS extensions received in the client hello message.
	 *  
	 * @return the list of TLS extensions received in the client hello message. 
	 * @throws IOException may throw an exception, If an error occurs while reading the extensions
	 */
	public final ArrayList<TlsExtensionTypes> getClientHelloExtensions() throws IOException {
		String extensionTypesHexString = getExtensions(TlsTestToolMode.client);
		return parseExtensions(extensionTypesHexString);
	}


	private ArrayList<TlsExtensionTypes> parseExtensions(final String extensionTypesHexString) {
		ArrayList<TlsExtensionTypes> receivedExtensions = new ArrayList<>();
		if (null != extensionTypesHexString) {
			final byte[] extensions = StringTools.toByteArray(extensionTypesHexString);
			if (extensions != null && extensions.length >= 4) {
				StringBuilder sb = new StringBuilder();
				logDebug("The DUT offers following ClientHello.extension(s): ");
				for (int i = 0; i < extensions.length;) {
					final TlsExtensionTypes extensionType = TlsExtensionTypes.valueOf(extensions[i],
							extensions[i + 1]);
					final int length = (extensions[i + 2] & A_0XFF) << 8 | extensions[i + 3] & A_0XFF;
					if (null != extensionType) {
						receivedExtensions.add(extensionType);
						String comma = i == 0 ? "" : ", ";
						sb.append(comma + extensionType.getExtensionDescriptionValue() + " with length " + length);
					}
					i += 4 + length;
				}
				logDebug(sb.toString());
			}
		}
		return receivedExtensions;
	}

	/**
	 * Checks the provided domain parameters list against received domain parameters list in the client hello message.
	 * 
	 * An error is logged if, <br>1. The received domain parameters list does not contain any of the expected domain
	 * parameters. <br>2. The received domain parameters contains additional domain parameters that do not exist in the
	 * provided domain parameters.
	 * 
	 * @param namedGroups the expected domain parameters.
	 * @throws IOException if an error occurs while reading the log information.
	 */
	public final void checkDomainParameters(final List<TlsNamedCurves> namedGroups) throws IOException {
		final byte[] data = assertExtensionTypeLogged(TlsTestToolMode.client,
				TlsExtensionTypes.supported_groups);
		if (data.length <= 2) {
			logError("The NamedGroup extension has not the correct format.");
			return;
		}
		var dataWithoutExtensionLength = Arrays.copyOfRange(data, 2, data.length);
		var receivedSupportedGroups = TlsNamedCurves.parseSupportedGroupsFromByteToList(dataWithoutExtensionLength);
		logDebug("Received following domain parameters: " + receivedSupportedGroups);
		// Find difference in signature algorithms with respect to supportedSignatureAndHashAlgorithms.
		List<TlsNamedCurves> difference
				= getDifference(namedGroups, receivedSupportedGroups);
		logInfo("Expected domain parameters: " + namedGroups);
		logInfo("Actual domain parameters: " + receivedSupportedGroups);
		if (!difference.isEmpty()) {
			logError("The NamedGroup extension does not offer following domain parameters: " + difference);
		} else {
			difference = getDifference(receivedSupportedGroups, namedGroups);
			if (!difference.isEmpty()) {
				logError(
						"The NamedGroup extension additionally contains following domain parameters: "
								+ difference);
			} else if (namedGroups.equals(receivedSupportedGroups)) {
				logInfo("The NamedGroup extension contains the domain parameters "
						+ "stated in the ICS in specified order.");
			} else {
				logError("The NamedGroup extension does not contain the domain parameters "
						+ "stated in the ICS in specified order.");
			}
		}
	}

	/**
	 * Method finds the difference in first list with respect to second list and returns new list containing elements
	 * that do not exist in the second list.
	 *
	 * @param list1 the first list.
	 * @param list2 the second list.
	 * @return new list containing elements that are not presents in the second list.
	 */
	private List<TlsNamedCurves> getDifference(final List<TlsNamedCurves> list1,
			final List<TlsNamedCurves> list2) {
		List<TlsNamedCurves> differences = new ArrayList<>(list1);
		differences.removeAll(list2);
		return differences;
	}

	/**
	 * Gets all the supported TLS extensions from the MICS file for specified TLS version and checks them against
	 * received extensions in the client hello message. An error is logged if, an expected extension is not received or
	 * TLS client hello additionally offers the extensions that are not expected (not specified in the MICS file against
	 * the provided TLS version.
	 * 
	 * @param tlsVersion the TLS version to get supported extensions as specified in the MICS file.
	 * @throws IOException may throw an exception, If an error occurs while reading the TLS client hello extensions
	 */
	public final void checkSupportedExtensions(final TlsVersion tlsVersion) throws IOException {

		List<TlsExtensionTypes> supportedExtensions
				= getConfiguration().getSupportedExtensions(tlsVersion);
		ArrayList<TlsExtensionTypes> receivedClientHelloExtensions = getClientHelloExtensions();
		ArrayList<TlsExtensionTypes> noteReceivedExtensions = new ArrayList<>();
		for (int i = 0; i < supportedExtensions.size(); i++) {
			if (!receivedClientHelloExtensions.contains(supportedExtensions.get(i))) {
				noteReceivedExtensions.add(supportedExtensions.get(i));
			}
		}


		ArrayList<TlsExtensionTypes> additionallyReceivedExtensions = new ArrayList<>();
		for (int i = 0; i < receivedClientHelloExtensions.size(); i++) {
			if (!supportedExtensions.contains(receivedClientHelloExtensions.get(i))) {
				additionallyReceivedExtensions.add(receivedClientHelloExtensions.get(i));
			}
		}


		if (!noteReceivedExtensions.isEmpty()) {
			logError("TLS ClientHello does not offer following extensions: "
					+ noteReceivedExtensions.toString());
		} else if (!additionallyReceivedExtensions.isEmpty()) {
			logError("TLS ClientHello additionally offers following extensions: "
					+ additionallyReceivedExtensions.toString());
		} else {

			logInfo("TLS ClientHello offers only the extensions stated in the ICS that match "
					+ "the TLS version.");
		}

		logInfo("Expected extensions: " + supportedExtensions);
		logInfo("Actual extensions: " + receivedClientHelloExtensions);

	}

	/**
	 * Method tries to keep the test tool running for specified time in seconds.
	 * 
	 * @param secondsOverMaximumSessionLifetime a small buffer in seconds to add to wait for session timeout.
	 * @param maximumTLSSessionTime maximum TLS session time in seconds.
	 * @throws InterruptedException if the TLS test tool process was interrupted.
	 */
	public void waitForSessionTimeout(int secondsOverMaximumSessionLifetime, long maximumTLSSessionTime)
			throws InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.invokeAll(Arrays.asList(new WaitForSessionTimeout()),
				maximumTLSSessionTime + secondsOverMaximumSessionLifetime,
				TimeUnit.SECONDS);
		executor.shutdown();
		try {
		    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
		    	executor.shutdownNow();
		    } 
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
	}
	
	private class WaitForSessionTimeout implements Callable<String> {
		@Override
		public String call() throws Exception {
			try {
				while (isRunning()) {
					final int sleepTimeMilliSeconds = 100;
					Thread.sleep(sleepTimeMilliSeconds);
				}
			} catch (InterruptedException e) {
				// do nothing.
			}
			return null;
		}
	}
	
	/**
	 * ocspResponseFile=[path] (with path pointing to an ocspResponseFile). If mode=server, then the TLS Test Tool sends
	 * a CertificateStatus in the TLS handshake containing the OCSP response from the ocspResponseFile (OCSP stapling).
	 * Ignored, if mode=client.
	 *
	 * @param fileLocation Path to the certificate file to be loaded
	 * @throws FileNotFoundException if OCSP response file was not found. 
	 */
	public final void setOcspResponseFile(final String fileLocation) throws FileNotFoundException {
		
		File responseFile = new File(fileLocation);

		if (!responseFile.exists()) {
			throw new FileNotFoundException(
					" Cannot find the specified OCSP Response file: " + fileLocation);
		}
		
		addConfigOption(TlsTestToolConfigurationOption.OCSPRESPONSEFILE, fileLocation);
	}
	
	/**
	 * Sets the PSK (Pre-Shared Key) to the TLS Test Tool. Later, in the TLS handshake both parties reuse the PSK to
	 * establish the TLS Session. Note: The configured psk is only used if a corresponding PSK-ciphersuite is set (see
	 * https://www.rfc-editor.org/rfc/rfc4279).
	 *
	 * @param psk 
	 */
	public final void setPSK(final byte[] psk) {
		addConfigOption(TlsTestToolConfigurationOption.PSK, asHexString(StringTools.toHexString(psk)));
	}

	/**
	 *
	 * Sets the PSKIdentitiyHint (Pre-Shared Key) to the TLS Test Tool. Later, in the TLS handshake both parties reuse the PSK to
	 * establish the TLS Session. Note: The configured psk is only used if a corresponding PSK-ciphersuite is set (see
	 * https://www.rfc-editor.org/rfc/rfc4279).
	 *
	 * @param pskIdentityHint
	 */
	public final void setPSKIdentityHint(final String pskIdentityHint) {
		addConfigOption(TlsTestToolConfigurationOption.PSKIDENTITIYHINT, pskIdentityHint);
	}

	/**
	 * Extracts gmt_unix_time from a server or client hello random.
	 *
	 * @param random to parse
	 * @return the read timestamp (<b>COUNT IN SECONDS since 1970)</b>)
	 */
	public final long getGmtunixtimeInHelloRandom(final String random) {
		final String[] hexParts = random.split(" ");
		final int sixteen = 16;
		return Long.parseUnsignedLong(hexParts[0] + hexParts[1] + hexParts[2] + hexParts[3], sixteen);
	}

}
