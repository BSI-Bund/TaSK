package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHeartbeat;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtHeartbeat;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A1_FR_02 - Heartbeat extension
 * <p>
 * This test verifies the behaviour of the DUT if the server sends a heartbeat extension.
 */
public class TLS_A1_FR_02 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_02";
	private static final String TEST_CASE_DESCRIPTION = "Heartbeat extension";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT if the server sends a heartbeat extension.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;

	public TLS_A1_FR_02() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
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
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The Server sends a ""HeartbeatExtension"" extension containing the HeartbeatMode ""peer_allowed_to_send"".
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT ignores the HeartbeatExtension or answers with a ""peer_not_allowed_to_send"" mode.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** highest supported TLS version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error("No supported TLS versions found.");
			return;
		}
		logger.debug("TLS version: " + tlsVersion.name());

		/** one supported cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error("No supported cipher suite is found.");
			return;
		}
		logger.debug("Supported CipherSuite: " + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and Cipher suite: "
				+ cipherSuite.getName(), null);

		TlsExtHeartbeat heartbeatExtension = new TlsExtHeartbeat();
		heartbeatExtension.setPeerAllowedToSend(true);

		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3",
				"The TLS server answers the DUT choosing a TLS version and a cipher suite that is "
						+ "contained in the ClientHello.",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, heartbeatExtension);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		step(5, "The Server sends a \"HeartbeatExtension\" extension containing the HeartbeatMode "
				+ "\"peer_allowed_to_send\".",
				"No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.");

		final byte[] heartbeatExtensionData = testTool.findExtensionTypeLogged(TlsTestToolMode.client,
				TlsExtensionTypes.heartbeat);
		if (heartbeatExtensionData == null) {
			logger.info("The DUT ignores the HeartbeatExtension.");
		} else if (heartbeatExtensionData[0] != Integer
				.parseUnsignedInt(TlsHeartbeat.peer_allowed_to_send.toString())) {
			logger.info("The DUT does not support HeartbeatMode peer_allowed_to_send. Supported Hearbeat Mode = "
					+ heartbeatExtensionData[0]);
		} else {
			logger.info("The DUT answers with a \"peer_not_allowed_to_send\" mode");
		}
		step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		tfLocalServerClose.executeSteps("7", "Server closed successfully", Arrays.asList(),
				testTool);


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
