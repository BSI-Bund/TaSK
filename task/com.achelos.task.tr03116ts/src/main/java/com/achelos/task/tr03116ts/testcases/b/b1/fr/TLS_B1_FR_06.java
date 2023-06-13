package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtStatusRequest;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_FR_06 - Support for OCSP stapling.
 * <p>
 * This test case verifies that the DUT supports OCSP stapling.
 */
public class TLS_B1_FR_06 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_06";
	private static final String TEST_CASE_DESCRIPTION = "Support for OCSP stapling";
	private static final String TEST_CASE_PURPOSE = "This test case verifies that the DUT supports OCSP stapling.";


	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;


	public TLS_B1_FR_06() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

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
	protected void preProcessing() throws Exception {}


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
	 * <li>The ClientHello message indicates support for OCSP stapling by including appropriate extension (e.g.
	 * ""status_request"" or ""status_request_v2"").
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT supplies a certificate accompanied by a valid OCSPResponse message.
	 * </ul>
	 */
	@Override
	protected void executeUsecase() throws Exception {

		/** this test case uses OpenSSL library */

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

		step(1, "The ClientHello message contains the status_request (OCSP stapling) extension.", "");

		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion,
				cipherSuite, new TlsExtStatusRequest(), TlsTestToolTlsLibrary.OpenSSL);
		testTool.start();

		step(3, "The DUT supplies a certificate accompanied by a valid  OCSPResponse message.", "");

		testTool.assertMessageLogged(TestToolResource.OCSP_response_received);
		testTool.assertMessageLogged(TestToolResource.OCSP_response_successful);

		step(4, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

	}


	@Override
	protected void postProcessing() throws Exception {

	}


	@Override
	protected void cleanAndExit() throws Exception {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}
