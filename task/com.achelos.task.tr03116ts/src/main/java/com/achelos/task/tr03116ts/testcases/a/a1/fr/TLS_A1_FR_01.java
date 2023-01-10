package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.crl.CRLExecutor;
import com.achelos.task.commandlineexecution.applications.crl.messagetextresources.CRLResource;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.ocsp.OCSPServerExecutor;
import com.achelos.task.commandlineexecution.applications.ocsp.messagetextresources.OCSPResource;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.helper.CrlOcspCertificate;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A1_FR_01 - No OCSP Response
 * <p>
 * This test verifies the behaviour of the DUT when receiving no OCSPResponse.
 */
public class TLS_A1_FR_01 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_01";
	private static final String TEST_CASE_DESCRIPTION = "No OCSP Response";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT when receiving no OCSPResponse.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private OCSPServerExecutor ocsp;
	private CRLExecutor crl;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;

	public TLS_A1_FR_01() {
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
		ocsp = new OCSPServerExecutor(getTestCaseId(), logger);
		crl = new CRLExecutor(getTestCaseId(), logger);
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
	 * <li>The ClientHello message indicates support for OCSP stapling by including appropriate extension (e.g.
	 * ""status_request"" or ""status_request_v2"").
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
	 * <li>The server does not send a OCSPResponse message.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT queries either an OCSP to receive the certificate status or downloads an appropriate CRL.
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
		
		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT,
				new CrlOcspCertificate());
		
		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL);

		crl.start(cipherSuite);
		ocsp.start(cipherSuite);

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
			// Stop the servers when handshake is finished.
			ocsp.stop();
			crl.stop();
			return;
		}
		step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		// Stop the servers when handshake is finished.
		ocsp.stop();
		crl.stop();

		boolean ocspRequestReceived
				= ocsp.assertMessageLogged(OCSPResource.OCSP_REQUEST_DATA.getMessage(), BasicLogger.INFO);
		boolean crlRequestReceived
				= crl.assertMessageLogged(CRLResource.GET_ROOT_CA_CRL.getMessage(), BasicLogger.INFO);

		if (!ocspRequestReceived && !crlRequestReceived) {
			logger.error(
					"The DUT did not query either an OCSP to receive the certificate status or downloads an "
							+ "appropriate CRL.");
		} else if (ocspRequestReceived) {
			logger.info(
					"The DUT queried OCSP to receive the certificate status.");
		} else {
			logger.info(
					"The DUT downloaded an appropriate CRL.");
		}

		tfLocalServerClose.executeSteps("7", "Server closed successfully", Arrays.asList(),
				testTool);

	}

	@Override
	protected void postProcessing() throws Exception {}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
		dutExecutor.cleanAndExit();
		ocsp.cleanAndExit();
		crl.cleanAndExit();
	}
}
