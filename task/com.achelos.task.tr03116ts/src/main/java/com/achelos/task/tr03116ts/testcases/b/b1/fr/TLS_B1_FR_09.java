package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;
import java.util.Collections;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_FR_09 - Session renegotiation.
 * <p>
 * This test verifies the correct behaviour of the DUT in case the client tries to renegotiate the parameters.
 */
public class TLS_B1_FR_09 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_09";
	private static final String TEST_CASE_DESCRIPTION = "Session renegotiation";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the correct behaviour of the DUT in case the client tries to renegotiate the parameters.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFAlertMessageCheck tfAlertMessageCheck;

	public TLS_B1_FR_09() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
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
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client sends a ClientHello message to initiate a renegotiation process.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT either ignores the request or answers with a ""no_renegotiation"" alert or another suitable alert
	 * description.
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

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);
		testTool.manipulateRenegotiate();
		testTool.start();

		tFTCPIPNewConnection.executeSteps("2", "", Collections.emptyList(),
				testTool);

		step(3, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		step(4, "The TLS client sends a ClientHello message to initiate a renegotiation process.", "");

		if(testTool.numberOfTimesMessageLogged(TestToolResource.ServerHello_valid)>1){
			logger.error("The DUT server accepted the renegoation.");
		} else {
			logger.info("The DUT ignored the request");
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
