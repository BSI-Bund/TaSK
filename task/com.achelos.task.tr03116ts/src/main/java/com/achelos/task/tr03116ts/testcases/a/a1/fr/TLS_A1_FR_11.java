package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationHandshakeType;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_FR_11 - Session resumption with Session Ticket
 * <p>
 * Positive test verifying the session resumption through the Session Ticket.
 */
public class TLS_A1_FR_11 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_11";
	private static final String TEST_CASE_DESCRIPTION = "Session resumption with Session Ticket";
	private static final String TEST_CASE_PURPOSE
			= "Positive test verifying the session resumption through the Session Ticket.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFConnectionCloseCheck tFConnectionCloseCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFServerSessionClose tfServerSessionClose;



	public TLS_A1_FR_11() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfServerSessionClose = new TFServerSessionClose(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tFConnectionCloseCheck = new TFConnectionCloseCheck(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
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
	 * <li>If applicable, the TLS ClientHello indicates support for session resumption via Session Ticket.
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
	 * <li>The TLS server supplies a valid ticket in the NewSessionTicket handshake message.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 * <h2>TestStep 3</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>Close TLS connection.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>TRUE
	 * </ul>
	 * <h2>TestStep 4</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server of [URL] for the second time.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The TLS ClientHello correctly initiates session resumption via Session Ticket.
	 * </ul>
	 * <h2>TestStep 5</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server accepts session resumption.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>Session resumption is performed.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>Session resumption is executed without errors and the channel is established.
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

		/** one supported cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and cipher suite: "
				+ cipherSuite, null);

		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL);
		TlsTestToolConfigurationHandshakeType sessionResumptionWithSessionTicket
				= TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionTicket;
		testTool.setSessionHandshakeType(TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionTicket);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		step(5, "Check if the TLS ClientHello indicates support for session resumption via Session Ticket.",
				"The TLS ClientHello indicates support for session resumption via Session Ticket.");
		byte[] sessionTicket
				= testTool.assertExtensionTypeLogged(TlsTestToolMode.client, TlsExtensionTypes.SessionTicket_TLS);
		if (sessionTicket == null) {
			logger.error("Test case is not applicable because "
					+ "the TLS ClientHello does not indicate support for session resumption via Session Ticket.");
			return;
		}
		logger.info("The TLS ClientHello indicates support for session resumption via Session Ticket.");

		step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		boolean handShakeSuccessful = testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		if (!handShakeSuccessful) {
			logger.error("The test case is aborted because initial handshake is failed.");
			return;
		}

		testTool.assertMessageLogged(TestToolResource.Initial_handshake_finished_Wait_for_resumption_handshake);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfServerSessionClose.executeSteps("8", "Close TLS connection successfully", Arrays.asList(), testTool);

		testTool.saveInitialHandshakeLogs();

		tFDutClientNewConnection.executeSteps("9",
				"The tester causes the DUT to connect to the TLS server for the second time.", Arrays.asList(),
				testTool,
				sessionResumptionWithSessionTicket, dutExecutor);
		step(10, "Check if the TLS ClientHello correctly initiates session resumption via Session Ticket.",
				"The TLS ClientHello correctly initiates session resumption via Session Ticket.");
		sessionTicket = testTool.assertExtensionTypeLogged(TlsTestToolMode.client, TlsExtensionTypes.SessionTicket_TLS);
		if (sessionTicket == null || sessionTicket.length == 0) {
			logger.error("The TLS ClientHello does not indicate support for session resumption via Session Ticket.");
			return;
		}
		logger.info("The TLS ClientHello indicates support for session resumption via Session Ticket.");

		testTool.assertMessageLogged(TestToolResource.Server_handled_all_connections);

		step(11, "Check if the session resumption is executed without errors and the channel is established.",
				"Session resumption is executed without errors and the channel is established.");
		boolean serverHelloDoneLogged
				= testTool.assertMessageLogged(TestToolResource.ServerHelloDone_transmitted, BasicLogger.INFO);
		boolean handshakeSuccessful = testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		if (!serverHelloDoneLogged && handshakeSuccessful) {
			logger.info("The DUT has accepted the session resumption.");
		} else {
			logger.error("The DUT has refused the session resumption.");
		}

		tfApplicationCheck.executeSteps("12", "", Arrays.asList(), testTool, dutExecutor);

		tfLocalServerClose.executeSteps("13", "Server closed successfully", Arrays.asList(),
				testTool);

	}

	@Override
	protected void postProcessing() throws Exception {}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
		dutExecutor.cleanAndExit();
	}
}
