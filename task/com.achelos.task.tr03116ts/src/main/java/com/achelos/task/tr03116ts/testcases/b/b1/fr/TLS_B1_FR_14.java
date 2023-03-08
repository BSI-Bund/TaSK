package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFHandshakeNotSuccessfulCheck;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * <p>
 * This test verifies the behaviour of the DUT when an incorrect PSK is used.
 */
public class TLS_B1_FR_14 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_14";
	private static final String TEST_CASE_DESCRIPTION = "incorrect PSK";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT when an incorrect PSK is used.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFAlertMessageCheck tFAlertMessageCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_B1_FR_14() {
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
	protected void preProcessing() throws Exception {

	}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the TLS version [TLS_VERSION].
	 * <li>The TLS ClientHello offers only the cipher suite [CIPHERSUITE].
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the the ClientHello. It may send a ""PSK identity hint"".
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client uses an invalid PSK.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the connection and sends a ""handshake failure"" alert or another suitable error
	 * description.
	 * <li>No TLS channel is established.
	 * </ul>
	 */
	@SuppressWarnings("unused")
	@Override
	protected void executeUsecase() throws Exception {

		// TBD: This test case is not applicable for the TLS Server Profile and will be covered in a later milestone

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_2;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION12_NOT_SUPPORTED);
			return;
		}

		/** any supported algorithm CipherSuite */
		List<TlsCipherSuite> cipherSuites = configuration.getSupportedPSKCipherSuites(tlsVersion);
		if (cipherSuites == null || cipherSuites.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_PSK_CIPHER_SUITE_FOR_TLS_VERSION + tlsVersion.getName());
			return;
		}

		TlsCipherSuite cipherSuite = cipherSuites.get(0);
		logger.debug(MessageConstants.SUPPORTED_PSK_CIPHER_SUITE + cipherSuite.getName());

		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.getName() + " .", null, testTool, tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL);
		byte [] hardcodedPSK= new byte[] {0x00};
		testTool.setPSK(hardcodedPSK);
		step(2, "The TLS client uses an invalid PSK.", null);

		testTool.start();

		tFTCPIPNewConnection.executeSteps("3", "", Collections.emptyList(),
				testTool);

		// check for handshake_failure
		tFAlertMessageCheck.executeSteps("4",
				"The DUT does not accept the connection and sends a \"handshake failure\" alert or another suitable "
						+ "error description.",
				Arrays.asList("level=warning/fatal", "description=handshake_failure"), testTool);
		tfHandshakeNotSuccessfulCheck.executeSteps("5", "No TLS channel is established", null, testTool, tlsVersion);

	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected final void cleanAndExit() throws Exception {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}
