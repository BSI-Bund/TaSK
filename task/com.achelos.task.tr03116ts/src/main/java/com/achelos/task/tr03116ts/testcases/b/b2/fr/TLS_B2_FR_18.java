package com.achelos.task.tr03116ts.testcases.b.b2.fr;

import java.util.Arrays;
import java.util.Collections;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_B2_FR_18 - Client authentication fails.
 * <p>
 * This test checks the behaviour of the DUT in case TLS client authentication fails. 
 * The client does not send a client certificate during the handshake.
 */
public class TLS_B2_FR_18 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_FR_18";
	private static final String TEST_CASE_DESCRIPTION = "Client authentication fails";
	private static final String TEST_CASE_PURPOSE 
			= "This test checks the behaviour of the DUT in case TLS client authentication fails. "
					+ "The client does not send a client certificate during the handshake.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tFClientHello;
	private final TFAlertMessageCheck tFAlertMessageCheck;
	private final TFHandshakeNotSuccessfulCheck tFHandshakeNotSuccessfulCheck;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_FR_18() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFClientHello = new TFTLSClientHello(this);
		tFAlertMessageCheck = new TFAlertMessageCheck(this);
		tFHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
		tfClientCertificate = new TFClientCertificate(this);
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
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the highest TLS version supported according to the ICS.
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * <li>The DUT supplies a certificate.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client supplies an empty client certificate message.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the certificate and sends a ""bad_certificate"" alert or another suitable error description.
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

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		tfClientCertificate.executeSteps("2",
				"The TLS client supplies the valid certificate chain [CERT_EMPTY_CLIENT].", Arrays.asList(),
				testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);

		tFClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);
		testTool.start();

		tFTCPIPNewConnection.executeSteps("2", "", Collections.emptyList(),
				testTool);

		step(3, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
		testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);

		// check for handshake_failure
		tFAlertMessageCheck.executeSteps("5", "The DUT does not accept the certificate and sends a \"bad_certificate\" "
				+ "alert or another suitable error description",
				Arrays.asList("level=warning/fatal", "description=bad_certificate"), testTool,
				TlsAlertDescription.bad_certificate);

		tFHandshakeNotSuccessfulCheck.executeSteps("6", "No TLS channel is established", null, testTool, tlsVersion);

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
