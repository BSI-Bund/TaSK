package com.achelos.task.tr03116ts.testcases.a.a2.ch;


import java.io.File;
import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.ocsp.OCSPRequestExecutor;
import com.achelos.task.commandlineexecution.applications.ocsp.OCSPServerExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.helper.CrlOcspCertificate;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.RequestClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFApplicationSpecificInspectionCheck;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFHandshakeNotSuccessfulCheck;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A2_CH_05 - Server certificate revoked by OCSPResponse.
 * 
 * <p>
 * This test verifies the behaviour of the DUT when receiving an OCSPResponse that reveals that the server certificate 
 * is revoked.
 * 
 */
public class TLS_A2_CH_05 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_CH_05";
	private static final String TEST_CASE_DESCRIPTION = "Server certificate revoked by OCSPResponse";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT when receiving an OCSPResponse that "
					+ "reveals that the server certificate is revoked.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private OCSPServerExecutor ocsp;
	private OCSPRequestExecutor ocspResponse;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_A2_CH_05() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);
		
		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
	}

	@Override
	protected final void prepareEnvironment() throws Exception {
		testTool = new TlsTestToolExecutor(getTestCaseId(), logger);
		tShark = new TSharkExecutor(getTestCaseId(), logger);
		tShark.start();
		dutExecutor = new DUTExecutor(getTestCaseId(), logger, configuration.getDutCallCommandGenerator());
		ocsp = new OCSPServerExecutor(getTestCaseId(), logger);
		ocspResponse = new OCSPRequestExecutor(getTestCaseId(), logger);
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
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The ClientHello message indicates support for OCSP stapling by including appropriate extension (e.g. ""status_request"" or ""status_request_v2"").
	 * </ul>
	 * <h2>TestStep 2</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the ClientHello.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_REVOKED].
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>The server sends an OCSP Response message revealing that the server certificate is revoked.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the certificate chain and sends a ""bad_certificate_status_response"" alert or another suitable error description.
	 * <li>No TLS connection is established.
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
				+ cipherSuite.getName(), null);
		
		TlsTestToolCertificateTypes certRevoked = TlsTestToolCertificateTypes.CERT_REVOKED;
		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_REVOKED].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, certRevoked, new CrlOcspCertificate());
		
		tfServerHello.executeSteps("3",
				"The TLS server answers the DUT choosing a TLS version and a cipher suite that is "
						+ "contained in the ClientHello.",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL, new RequestClientCertificate());
		

		// Start the OCSP Server.
		ocsp.start(cipherSuite);

		//OCSP Request
		ocspResponse.start(cipherSuite, certRevoked);
		File logFile = ocspResponse.getOutputFile();
		
		if (!logFile.exists()) {
			logger.error(" Cannot find the specified OCSP Response file: " + logFile.toString());
			return;
		}
		
		testTool.setOcspResponseFile(logFile.toString());
		
		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);
		
		step(5, "Check if the ClientHello message indicates support for OCSP stapling",
				"The ClientHello message indicates support for OCSP stapling by including appropriate extension "
						+ "(e.g. \"status_request\" or \"status_request_v2\").");
		final byte[] statusRequest
				= testTool.assertExtensionTypeLogged(TlsTestToolMode.client, TlsExtensionTypes.status_request);
		byte[] statusRequestV2 = null;
		if (statusRequest != null) {
			logger.info(
					"Actual Result: The ClientHello message indicates support for OCSP stapling by including "
							+ "\"status_request\" extension.");
		} else {
			statusRequestV2
					= testTool.assertExtensionTypeLogged(TlsTestToolMode.client, TlsExtensionTypes.status_request_v2);
			if (statusRequestV2 != null) {
				logger.info("Actual Result: The ClientHello message indicates support for OCSP stapling by including "
						+ "\"status_request_v2\" extension.");
			}
		}

		if (statusRequest == null && statusRequestV2 == null) {
			logger.error(
					"The DUT did not indicate support for OCSP stapling. Neither \"status_request\" "
							+ "nor \"status_request_v2\" extension was found in the ClientHello message.");
			// Stop the OCSP server when handshake is finished.
			ocsp.stop();
			return;
		}
		
		// Stop the OCSP server when handshake is finished.
		ocsp.stop();
		
		tfAlertMessageCheck.executeSteps("6", "The DUT does not accept the certificate chain and sends a "
				+ "\"bad_certificate_status_response\" alert or another suitable error description.",
				Arrays.asList("level=warning/fatal", "description=bad_certificate_status_response"), testTool, TlsAlertDescription.bad_certificate_status_response);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfHandshakeNotSuccessfulCheck.executeSteps("8", "No TLS channel is established", null, testTool, tlsVersion);

		tfLocalServerClose.executeSteps("9", "Server closed successfully", Arrays.asList(),
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
