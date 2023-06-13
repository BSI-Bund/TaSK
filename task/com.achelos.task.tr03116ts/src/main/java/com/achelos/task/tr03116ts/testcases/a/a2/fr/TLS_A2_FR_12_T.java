package com.achelos.task.tr03116ts.testcases.a.a2.fr;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.dutexecution.DUTExecutor;
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
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;
import com.achelos.task.utilities.logging.IterationCounter;
import com.achelos.task.utilities.logging.LogBean;


/**
 * Test case TLS_A2_FR_12_T - Session resumption declined.
 * <p>
 * This test verifies the behaviour of the DUT if the server chooses not to resume the session.
 * <p>
 * The test MUST be repeated for every session resumption mechanism supported by the DUT (i.e. Session ID, Session
 * Ticket or both).
 */
public class TLS_A2_FR_12_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_FR_12_T";
	private static final String TEST_CASE_DESCRIPTION = "Session resumption declined";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT if the server chooses not to resume the session.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFConnectionCloseCheck tFConnectionCloseCheck;
	private final TFServerSessionClose tfServerSessionClose;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;

	public TLS_A2_FR_12_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tFConnectionCloseCheck = new TFConnectionCloseCheck(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
		tfServerSessionClose = new TFServerSessionClose(this);
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
	 * <li>The TLS ClientHello indicates support for session resumption.
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
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>The TLS server supports session resumption and prepares necessary data (e.g. generates a new Session ID or NewSessionTicket handshake message).
	 * <li>The data required for the session resumption is supplied to the DUT.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The client supplies a valid client certificate and a correct CertificateVerify message.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 * <h2>TestStep 3</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>Close TLS connection.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The application data may be exchanged before closing the channel.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>TRUE
	 * </ul>
	 * <h2>TestStep 4</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server of [URL] for the second time.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The TLS ClientHello initiates session resumption via correct Session ID or Session Ticket extension.
	 * </ul>
	 * <h2>TestStep 5</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The Server does not resume the session and forces a fresh TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The connection establishment is either aborted by the TLS client, closed immediately after creation, or not used to send any further data.
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
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.name());

		List<TlsTestToolConfigurationHandshakeType> resumptionType =
				TlsTestToolConfigurationHandshakeType.getHandshakeTypeFromDUTCapabilities(configuration.getDUTCapabilities());
		if(resumptionType.isEmpty()){
			logger.warning("This test case is not applicable since does not support any resumption mechanism according to the ICS");
			return;
		}

		int iterationNumber = 1;
		int totalIterations = resumptionType.size()*2; //for each resumptionType, we need to perform 2 handshake
		for (int i = 0; i < resumptionType.size(); i++) {
			logger.info("Start iteration " + iterationNumber + " of " + totalIterations + ".");

			step(1, "Setting TLS version: " + tlsVersion.getName() + " and cipher suite: "
					+ cipherSuite, null);

			tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
					Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
			
			tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(),
					testTool,
					tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL, new RequestClientCertificate());
			TlsTestToolConfigurationHandshakeType handshakeType = resumptionType.get(i);
			testTool.setSessionHandshakeType(resumptionType.get(i));
			tFDutClientNewConnection.executeSteps("4",
					"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
					dutExecutor, new IterationCounter(iterationNumber++, totalIterations));

			step(5, "Check if the TLS ClientHello indicates support for session resumption.",
					"The TLS ClientHello indicates support for session resumption.");

			if (handshakeType.equals(TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionID)) {
				String sessionID = testTool.getValue(TestToolResource.ClientHello_session_id);
				if (sessionID == null) {
					logger.info(
							"The TLS ClientHello indicates support for session resumption via empty "
									+ "Session ID extension.");
				} else if (sessionID.length() > 0) {
					logger.warning(
							"The TLS ClientHello sends non empty Session ID with length: " + sessionID.length());
				}
			} else {
				byte[] sessionTicket
						= testTool.assertExtensionTypeLogged(TlsTestToolMode.client,
								TlsExtensionTypes.SessionTicket_TLS);
				if (sessionTicket == null) {
					logger.error(
							"The TLS ClientHello does not indicate support for session resumption via Session Ticket.");

				}
			}
			
			step(6, "The TLS server supports session resumption and prepares necessary data (e.g. "
					+ "generates a new Session ID or NewSessionTicket handshake message).",
					"The data required for the session resumption is supplied to the DUT.");
			String serverHelloSessionID = null;
			if (handshakeType.equals(TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionID)) {
				serverHelloSessionID = testTool.getValue(TestToolResource.ServerHello_session_id);
				logger.info(
						"The TLS server generated following new Session ID and supplies it to the DUT: "
								+ serverHelloSessionID);
			} else {
				String newSessionTicket = testTool.getValue(TestToolResource.NewSessionTicket_ticket);
				logger.info(
						"The TLS server generated following new Session ticket and supplies it to the DUT: "
								+ newSessionTicket);
			}
			

			step(7, "Check if the client supplies a valid client certificate and a correct CertificateVerify message.",
					"The client supplies a valid client certificate and a correct CertificateVerify message.");
			testTool.assertMessageLogged(TestToolResource.Certificate_received_valid.getInternalToolOutputMessage());
			testTool.assertMessageLogged(TestToolResource.CertificateVerify_valid.getInternalToolOutputMessage());
			
			step(8, "Check if the TLS protocol is executed without errors and the channel is established.",
					"The TLS protocol is executed without errors and the channel is established.");
			boolean handshakeSuccessfulFound = testTool.assertMessageLogged(TestToolResource.Handshake_successful);

			tfApplicationCheck.executeSteps("9", "", Arrays.asList(), testTool, dutExecutor);

			tfServerSessionClose.executeSteps("10", "Close TLS connection successfully", Arrays.asList(), testTool);
			testTool.assertMessageLogged(TestToolResource.Initial_handshake_finished_Wait_for_resumption_handshake);


			testTool.stop();

			if (testTool.isRunning()) {
				// Wait 5 seconds for TLS Test Tool to stop.
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Callable<Object> task = new Callable<Object>() {

					@Override
					public Object call() {
						while (testTool.isRunning()) {
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException e) {
								return null;
							}
							if (Thread.interrupted()) {
								return null;
							}
						}
						return null;

					}
				};

				Collection<Callable<Object>> collection = new ArrayList<Callable<Object>>();
				collection.add(task);
				executor.invokeAny(collection, 5, TimeUnit.SECONDS);
				
				executor.shutdown();
				try {
				    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				    	executor.shutdownNow();
				    } 
				} catch (InterruptedException e) {
					executor.shutdownNow();
				}
			}
			
			if (!handshakeSuccessfulFound) {
				logger.error("The test case is aborted because initial handshake is failed.");
				return;
			}

			dutExecutor.resetProperties();
			testTool.resetProperties();

			logger.info("Start iteration " + iterationNumber + " of " + totalIterations + ".");
			step(11, "Setting TLS version: " + tlsVersion.getName() + " and cipher suite: "
					+ cipherSuite, null);
			tfserverCertificate.executeSteps("12", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
					Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
			
			tfServerHello.executeSteps("13", "Server started and waits for new client connection", Arrays.asList(),
					testTool,
					tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL, new RequestClientCertificate());

			tFDutClientNewConnection.executeSteps("14",
					"The tester causes the DUT to connect to the TLS server for the second time.", Arrays.asList(),
					testTool,
					dutExecutor,
					resumptionType.get(i),
					new IterationCounter(iterationNumber++, totalIterations));
			
			step(15, "Check if the TLS ClientHello initiates session resumption via correct Session ID.",
					"TLS ClientHello initiates session resumption via correct Session ID or Session Ticket extension.");
			if (handshakeType.equals(TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionID)) {
				String sessionID = testTool.getValue(TestToolResource.ClientHello_session_id);
				if (serverHelloSessionID.equalsIgnoreCase(sessionID)) {
					logger.info("The TLS ClientHello initiates session resumption via Session ID extension with the "
							+ "correct value.");
				} else {
					logger.error("The TLS ClientHello does not initiate session resumption via Session ID extension "
							+ "with the correct value.");
				}
			} else {
				byte[] sessionTicket = testTool.assertExtensionTypeLogged(TlsTestToolMode.client,
						TlsExtensionTypes.SessionTicket_TLS);
				if (sessionTicket == null || sessionTicket.length == 0) {
					logger.error(
							"The TLS ClientHello does not indicate support for session resumption via Session Ticket.");
					return;
				}
				logger.info("The TLS ClientHello indicates support for session resumption via Session Ticket.");
			}

			step(16, "Check if the connection establishment is either aborted by the DUT, "
					+ "closed immediately after creation, or not used to send any further data.",
					"The connection establishment is either aborted by the DUT, "
							+ "closed immediately after creation, or not used to send any further data.");
			/*we are currently checking if the handshake was aborted*/
			LogBean handshakeSuccessful = testTool.findMessage(TestToolResource.Handshake_successful);
			if (handshakeSuccessful != null) {
				logger.error("The connection establishment is not aborted by the DUT.");
			} else {
				logger.info("The connection establishment is aborted by the DUT.");
			}

			testTool.assertMessageLogged(TestToolResource.Server_handled_all_connections);

			tfApplicationCheck.executeSteps("17", "", Arrays.asList(), testTool, dutExecutor);

			tfLocalServerClose.executeSteps("18", "Server closed successfully", Arrays.asList(),
					testTool);
			dutExecutor.resetProperties();
			testTool.resetProperties();
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
