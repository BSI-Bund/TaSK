package com.achelos.task.tr03116ts.testcases.b.b1.gp;

import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFHandshakeNotSuccessfulCheck;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_GP_08 - Unsupported signature algorithm.
 * <p>
 * This test verifies that the connection is not established if the client indicates only signature algorithms during
 * the handshake that do not meet the requirements of the application.
 */
public class TLS_B1_GP_08 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_GP_08";
	private static final String TEST_CASE_DESCRIPTION = "Unsupported signature algorithm";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the connection is not established if the client indicates "
					+ "only signature algorithms during the handshake that do not meet the requirements "
					+ "of the application.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFAlertMessageCheck tFAlertMessageCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_B1_GP_08() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
		tFAlertMessageCheck = new TFAlertMessageCheck(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
	}

	@Override
	protected final void prepareEnvironment() throws Exception {
		testTool = new TlsTestToolExecutor(getTestCaseId(), logger);
		tShark = new TSharkExecutor(getTestCaseId(), logger);
		tShark.start();
	}

	/**
	 * <h2>Precondition</h2>
	 * <ul>
	 * <li>DUT services are online without any known disturbances.
	 * <li>The DUT is accepting TLS connections.
	 * </ul>
	 */
	@Override
	protected final void preProcessing() throws Exception {}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the highest TLS version supported according to the ICS.
	 * <li>The TLS ClientHello offers only the cipher suite [CIPHERSUITE].
	 * <li>The TLS ClientHello sends the signature_algorithms extension indicating only signature algorithms that do not
	 * conform to the application.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the ClientHello and sends a ""handshake failure"" alert or another suitable error
	 * description.
	 * <li>No TLS channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_2;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION12_NOT_SUPPORTED);
			return;
		}

		/** one supported cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		/** not supported signature algorithms */
		var unsupportedSignatureAlgorithm = configuration.getNotSupportedSignatureAlgorithm();
		if (unsupportedSignatureAlgorithm == null) {
			logger.error("No unsupported Signature Algorithm found.");
			return;
		}
		if (null != unsupportedSignatureAlgorithm.getSignatureAlgorithm() &&
				null != unsupportedSignatureAlgorithm.getHashAlgorithm()) {
			logger.debug("Not Supported Signature Algorithms: " +
					unsupportedSignatureAlgorithm.getSignatureAlgorithm().getSignatureAlgorithmValueDescription()
					+ "With" +
					unsupportedSignatureAlgorithm.getHashAlgorithm().getHashAlgorithmDescription());
		}

		step(1, "TLS ClientHello sends the signature_algorithms extension indicating "
				+ " only signature algorithms that do not conform to the application.", "");

		// configure client hello
		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.getName() + " signature algorithm "
				+ unsupportedSignatureAlgorithm.getSignatureAlgorithm().getSignatureAlgorithmValueDescription() + "With"
				+ unsupportedSignatureAlgorithm.getHashAlgorithm().getHashAlgorithmDescription() + " .",
				null, testTool, tlsVersion, cipherSuite, unsupportedSignatureAlgorithm);

		testTool.start();

		// connect
		tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(),
				testTool);

		// check for handshake_failure
		tFAlertMessageCheck.executeSteps("4", "The DUT rejects the ClientHello and sends a \"handshake failure\" alert "
				+ "or another suitable error description",
				Arrays.asList("level=warning/fatal", "description=handshake_failure"), testTool);

		tfHandshakeNotSuccessfulCheck.executeSteps("5", "No TLS channel is established", null, testTool, tlsVersion);

	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}
