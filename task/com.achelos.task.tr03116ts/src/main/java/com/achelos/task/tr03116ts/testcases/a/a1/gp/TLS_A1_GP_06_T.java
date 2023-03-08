package com.achelos.task.tr03116ts.testcases.a.a1.gp;


import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.enums.TlsAlertLevel;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_GP_06_T - domain parameters with insufficient length
 * <p>
 * This test verifies the behaviour of the DUT in case the server tries to use ephemeral domain parameters with
 * insufficient length.
 * <p>
 * Depending on the client´s capabilities, the test MUST be repeated for ECDHE and DHE ephemeral domain parameters of
 * insufficient length from Table 20.
 */
public class TLS_A1_GP_06_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_GP_06_T";
	private static final String TEST_CASE_DESCRIPTION = "domain parameters with insufficient length";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT in case the server tries to use "
					+ "ephemeral domain parameters with insufficient length.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFTLSServerHello tftlsServerHello;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFServerCertificate tfserverCertificate;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_A1_GP_06_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfLocalServerClose = new TFLocalServerClose(this);
		tftlsServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
	}

	@Override
	protected final void prepareEnvironment() throws Exception {
		testTool = new TlsTestToolExecutor(getTestCaseId(), logger);
		tShark = new TSharkExecutor(getTestCaseId(), logger);
		tShark.start();
		dutExecutor = new DUTExecutor(getTestCaseId(), logger, configuration.getDutCallCommandGenerator());
	}

	/**
	 * <h2>Precondition</h2>
	 * <ul>
	 * <li>The test TLS server is waiting for incoming TLS connections on [URL].
	 * </ul>
	 */
	@Override
	protected final void preProcessing() throws Exception {}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server only offers a cipher suite based on FFDHE or ECDHE for key negotiation listed in the ICS of
	 * the DUT.
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The TLS server sends an ephemeral key based on domain parameters (elliptic curve in case of ECDHE or key
	 * length in case of FFDHE) of insufficient length.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS client does not accept the ServerHello and sends a ""handshake_failure"" alert or another suitable
	 * error description.
	 * <li>No TLS connection is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all supported tls version
		List<TlsVersion> tlsVersions = configuration.getSupportedTLSVersions();
		if (null == tlsVersions || tlsVersions.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}

		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.name());
		}

		for (TlsVersion tlsVersion : tlsVersions) {

			var eccCipherSuite = configuration.getSingleSupportedECCCipherSuite(tlsVersion);
			var insufficientLengthECDHEGroups
					= configuration.getNotSupportedEllipticCurves(tlsVersion);

			var dheCipherSuite = configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);
			var insufficientLengthDHEGroups
					= configuration.getInsufficientDHEKeyLengths(tlsVersion);

			var notSupportedFFDHEGroups = configuration.getNotSupportedDHEGroups(tlsVersion);

			if (null == eccCipherSuite && null == dheCipherSuite) {
				// If neither a ECDHE nor a DHE Cipher Suite are supported, PFS is not available.
				logger.error("No supported ECDHE or DHE cipher suite found. " +
						"If neither an ECDHE nor a DHE Cipher Suite are supported, PFS is not available.");
				continue;
			}

			int iterationCount = 1;

			int maxIterationCount = 0;
			if (eccCipherSuite != null && !insufficientLengthECDHEGroups.isEmpty()) {
				maxIterationCount += insufficientLengthECDHEGroups.size();
			}
			if (dheCipherSuite != null && !insufficientLengthDHEGroups.isEmpty()) {
				maxIterationCount += insufficientLengthDHEGroups.size();
			}
			if (dheCipherSuite != null && !notSupportedFFDHEGroups.isEmpty()) {
				maxIterationCount += notSupportedFFDHEGroups.size();
			}

			if (null != eccCipherSuite) {
				for (var insufficientECDHEGroup : insufficientLengthECDHEGroups) {
					logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
					step(1, "Setting TLS version: " + tlsVersion.getName() + " ECC CipherSuite: "
							+ eccCipherSuite.name() + " ECC Group: " + insufficientECDHEGroup.getName(), null);
					
					tfserverCertificate.executeSteps("2",
							"The TLS server supplies the certificate chain [CERT_DEFAULT].",
							Arrays.asList(), testTool, tlsVersion, eccCipherSuite,
							TlsTestToolCertificateTypes.CERT_DEFAULT);
					
					tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
							Arrays.asList(),
							testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, eccCipherSuite,
							insufficientECDHEGroup);
					tFDutClientNewConnection.executeSteps("4",
							"The TLS server receives a ClientHello handshake message from the DUT.",
							Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
							dutExecutor);
					iterationCount++;
					tfAlertMessageCheck.executeSteps("5", "The TLS client does not accept the ServerHello " +
									"and sends a \"handshake_failure\" alert or another suitable error description.",
							Arrays.asList("level=warning/fatal"), testTool);
					tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);
					tfHandshakeNotSuccessfulCheck.executeSteps("7", "No TLS channel is established", null, testTool, tlsVersion);
					tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
							testTool);
					dutExecutor.resetProperties();
					testTool.resetProperties();
				}
			}

			if (null != dheCipherSuite) {
				// Insufficient Length DH Groups.
				for (var insufficientDhGroup : insufficientLengthDHEGroups) {
					logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
					step(1, "Setting TLS version: " + tlsVersion.getName() + " DHE CipherSuite: "
							+ dheCipherSuite.name() + " DHE Group: " + insufficientDhGroup.name(), null);
					
					tfserverCertificate.executeSteps("2",
							"The TLS server supplies the certificate chain [CERT_DEFAULT].",
							Arrays.asList(), testTool, tlsVersion, dheCipherSuite,
							TlsTestToolCertificateTypes.CERT_DEFAULT);
					
					tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
							Arrays.asList(),
							testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, dheCipherSuite, insufficientDhGroup);
					
					tFDutClientNewConnection.executeSteps("4",
							"The TLS server receives a ClientHello handshake message from the DUT.",
							Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
							dutExecutor);
					iterationCount++;
					tfAlertMessageCheck.executeSteps("5", "The TLS client does not accept the ServerHello " +
									"and sends a \"handshake_failure\" alert or another suitable error description.",
							Arrays.asList("level=fatal"), testTool, TlsAlertLevel.fatal,
							null);

					tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);

					tfHandshakeNotSuccessfulCheck.executeSteps("7", "No TLS channel is established", null, testTool, tlsVersion);

					tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
							testTool);
					dutExecutor.resetProperties();
					testTool.resetProperties();
				}

				// Not Supported DHE Groups
				for (var notSupportedFFDHEGroup : notSupportedFFDHEGroups) {
					logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
					step(1, "Setting TLS version: " + tlsVersion.getName() + " DHE CipherSuite: "
							+ dheCipherSuite.name() + " DHE Group: " + notSupportedFFDHEGroup.getName(), null);

					tfserverCertificate.executeSteps("2",
							"The TLS server supplies the certificate chain [CERT_DEFAULT].",
							Arrays.asList(), testTool, tlsVersion, dheCipherSuite,
							TlsTestToolCertificateTypes.CERT_DEFAULT);

					tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
							Arrays.asList(),
							testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, dheCipherSuite, notSupportedFFDHEGroup);

					tFDutClientNewConnection.executeSteps("4",
							"The TLS server receives a ClientHello handshake message from the DUT.",
							Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
							dutExecutor);
					iterationCount++;
					tfAlertMessageCheck.executeSteps("5", "The TLS client does not accept the ServerHello " +
									"and sends a \"handshake_failure\" alert or another suitable error description.",
							Arrays.asList("level=fatal"), testTool, TlsAlertLevel.fatal,
							null);

					tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);

					tfHandshakeNotSuccessfulCheck.executeSteps("7", "No TLS channel is established", null, testTool, tlsVersion);

					tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
							testTool);
					dutExecutor.resetProperties();
					testTool.resetProperties();
				}
			}
		}
	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
		dutExecutor.cleanAndExit();
	}
}
