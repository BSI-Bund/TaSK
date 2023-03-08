package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_B2_GP_02 - Reject unsupported Cipher Suites.
 * <p>
 * This test verifies that the connection is not established if the client offers only cipher suites that are not listed
 * in the ICS.
 */
public class TLS_B2_GP_02 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_02";
	private static final String TEST_CASE_DESCRIPTION = "Reject unsupported Cipher Suites";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the connection is not established if the client offers "
					+ "only cipher suites that are not listed in the ICS.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFAlertMessageCheck tFAlertMessageCheck;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;


	public TLS_B2_GP_02() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFAlertMessageCheck = new TFAlertMessageCheck(this);
		tfClientHello = new TFTLSClientHello(this);
		tfClientCertificate = new TFClientCertificate(this);
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
	 * <li>The TLS ClientHello offers only cipher suites that are not supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
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

		/** highest supported TLS version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		/** Not supported cipher suites */
		var notSupportedCipherSuites = configuration.getNotSupportedCipherSuites(tlsVersion, TlsTestToolMode.client);

		if (null == notSupportedCipherSuites || notSupportedCipherSuites.isEmpty()) {
			logger.error(MessageConstants.NO_UNSUPPORTED_CIPHER_SUITES);
			return;
		}
		logger.debug(MessageConstants.NOT_SUPPORTED_CIPHER_SUITES);
		for (TlsCipherSuite cipherSuite : notSupportedCipherSuites) {
			logger.debug(cipherSuite.getName());
		}


		tfClientCertificate.executeSteps("1",
				"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(), testTool,
				tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);

		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ " .", null, testTool, tlsVersion, notSupportedCipherSuites);
		testTool.start();

		// connect
		tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(), testTool);

		// check for handshake_failure
		tFAlertMessageCheck.executeSteps("4", "The DUT does not accept the ClientHello and sends a \"handshake "
				+ "failure\" alert or another suitable error description.",
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