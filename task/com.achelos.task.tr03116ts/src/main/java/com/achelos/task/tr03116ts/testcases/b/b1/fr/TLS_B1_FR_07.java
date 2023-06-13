package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtHeartbeat;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_FR_07 - Heartbeat extension not supported.
 * <p>
 * This test verifies the correct behaviour of the DUT if the client wants to use heartbeats.
 */
public class TLS_B1_FR_07 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_07";
	private static final String TEST_CASE_DESCRIPTION = "Heartbeat extension not supported";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the correct behaviour of the DUT if the client wants to use heartbeats.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;

	public TLS_B1_FR_07() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
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
	protected final void preProcessing() throws Exception {

	}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the highest TLS version supported according to the ICS.
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello contains a ""HeartbeatExtension"" with the HeartbeatMode ""peer_allowed_to_send"".
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT ignores the HeartbeatExtension or answers with a ""peer_not_allowed_to_send"" mode.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** highest supported TLS version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		TlsExtHeartbeat heartbeatExtension = new TlsExtHeartbeat();
		heartbeatExtension.setPeerAllowedToSend(true);

		step(1, "The tester connects to the DUT.", "");
		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite,
				heartbeatExtension);

		testTool.start();

		step(3, "The TLS ClientHello contains a \"HeartbeatExtension\" with the HeartbeatMode \"peer_allowed_to_send\"",
				"Receive ServerHello message from TOE. ServerHello.extensions does not contain the heartbeat extension.");

		tFTCPIPNewConnection.executeSteps("4", "", Arrays.asList(),
				testTool);

		step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		
		step(6, "Check if the DUT ignores the HeartbeatExtension or answers with a \"peer_not_allowed_to_send\" mode.",
				"The DUT ignores the HeartbeatExtension or answers with a \"peer_not_allowed_to_send\" mode.");
		testTool.assertServerLacksExtension(TlsExtensionTypes.heartbeat);

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
