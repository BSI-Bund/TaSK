package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationHandshakeType;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_FR_17 - No 0-RTT connection.
 * <p>
 * This test case checks that the early data is not used by the DUT.
 */
public class TLS_B1_FR_17 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_17";
	private static final String TEST_CASE_DESCRIPTION = "No 0-RTT connection";
	private static final String TEST_CASE_PURPOSE = "This test case checks that the early data is not used by the DUT.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;

	public TLS_B1_FR_17() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfClientHello = new TFTLSClientHello(this);
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
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
	 * <li>The TLS ClientHello offers the TLS version [TLS_VERSION].
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>If applicable, the TLS ClientHello indicates support for session resumption via Session Ticket.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT supplies a valid ticket in the NewSessionTicket handshake message.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester closes the connection to the DUT and stores the Session Ticket.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>TRUE
	 * </ul>
	 * <h2>TestStep 3</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>Less than the maximum allowed amount of time later, the tester reconnects to the DUT and performs 0-RTT
	 * connection establishment with early data.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT ignores the early data and forces regular 1-RTT handshake.
	 * <li>Alternatively the DUT may respond with HelloRetryRequest handshake message.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		/** this test case uses OpenSSL library and works only for TLS 1.3 */

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());
		/** highest supported TLS version */

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_3;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION13_NOT_SUPPORTED);
			return;
		}

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.getName() + " .", null, testTool, tlsVersion,
				cipherSuite, TlsTestToolTlsLibrary.OpenSSL);

		testTool.start(1, 2);
		
		tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(), testTool);
		
		step(3, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		boolean handshakeSuccessful = testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		
		// response contains session ticket
		boolean ticketLogged = testTool.assertMessageLogged(TestToolResource.NewSessionTicket_ticket);

		if (!ticketLogged) {
			logger.error("The DUT did not supply a valid ticket in the NewSessionTicket handshake message.");
			return;
		}

		if (!handshakeSuccessful) {
			logger.error("The handshake was not successful");
			return;
		}

		List<String> sessionCache = testTool.getSessionCacheFromLog(0);
		if (sessionCache == null || sessionCache.size() == 0) {
			logger.error("SessionCache was not logged");
			return;
		}

		/* Step 4: Close Connection */
		step(4, "\tThe tester closes the connection to the DUT and stores the Session\n" +
				"\t * Ticket.", "");
		logger.info("Initial handshake was successfully executed and the session ticket was stored");

		/* Step 5: Perform a 0-RTT connection */
		step(5, "Less than the maximum allowed amount of time later, the tester reconnects "
				+ "to the DUT and performs 0-RTT connection establishment with early data",
				"The DUT ignores the early data and forces regular 1-RTT handshake.");

		testTool.resetProperties();

		tfClientHello.executeSteps("6", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion,
				cipherSuite, TlsTestToolConfigurationHandshakeType.ZERO_RTT);

		testTool.setSessionCache(sessionCache.get(0));
		testTool.setEarlyData("0123456789");

		testTool.start(2, 2);

		boolean serverHelloDoneLogged
				= testTool.assertMessageLogged(TestToolResource.ServerHelloDone_valid, BasicLogger.INFO);
		handshakeSuccessful = testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		if (!serverHelloDoneLogged && handshakeSuccessful) {
			logger.info("The server TOE has accepted the session resumption.");
		} else {
			logger.info("The server TOE has refused the session resumption.");
		}
		boolean earlyDataAccepted = testTool.assertServerSupportsEncryptedExtensions(TlsExtensionTypes.early_data);
		if (earlyDataAccepted) {
			logger.error("The server TOE has accepted the early data in its EncryptedExtensions.");
		} else {
			logger.info("The server TOE has not accepted the early data in its EncryptedExtensions.");
		}
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
