package com.achelos.task.tr03116ts.testcases.a.a1.ch;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.crl.CRLExecutor;
import com.achelos.task.commandlineexecution.applications.crl.messagetextresources.CRLResource;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.helper.CrlOcspCertificate;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_CH_09 - Server certificate revoked by CRL.
 * 
 * <p>
 * This test verifies the behaviour of the DUT when retrieving a CRL revealing that the server certificate is revoked.
 *
 */
public class TLS_A1_CH_09 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_CH_09";
	private static final String TEST_CASE_DESCRIPTION = "Server certificate revoked by CRL";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT when retrieving a CRL "
					+ "revealing that the server certificate is revoked.";
	
	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private CRLExecutor crl;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_A1_CH_09() {
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
		crl = new CRLExecutor(getTestCaseId(), logger);
	}

	/**
	 * <h2>Precondition</h2>
	 * <ul>
	 * <li>The test TLS server is waiting for incoming TLS connections on [URL].
	 * </ul>
	 */
	@Override
	protected final void preProcessing() throws Exception {
	}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_REVOKED].
	 * <li>The server maintains a CRL distribution point with the CRL revealing that the server certificate is revoked.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT retrieves the CRL.
	 * <li>The DUT aborts the connection and sends a ""bad_certificate_status_response"" alert or another suitable error
	 * description.
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
		
		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_REVOKED].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, new CrlOcspCertificate(),
				TlsTestToolCertificateTypes.CERT_REVOKED);

		tfServerHello.executeSteps("3",
				"The TLS server answers the DUT choosing a TLS version and a cipher suite that is "
						+ "contained in the ClientHello.",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolTlsLibrary.OpenSSL);

		crl.start(cipherSuite);
		
		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);
		
		// Stop the server when handshake is finished.
		crl.stop();
		
		boolean crlRequestReceived
				= crl.assertMessageLogged(CRLResource.GET_ROOT_CA_CRL.getMessage(), BasicLogger.INFO);

		if (!crlRequestReceived) {
			logger.error(
					"The DUT did not downloads an appropriate CRL.");
		} else {
			logger.info(
					"The DUT downloaded an appropriate CRL.");
		}
		
		tfAlertMessageCheck.executeSteps("5", "The DUT does not accept the certificate chain and sends a "
				+ "\"bad_certificate_status_response\" alert or another suitable error description.",
				Arrays.asList("level=warning/fatal", "description=bad_certificate_status_response"), testTool, TlsAlertDescription.bad_certificate_status_response);

		//CRL does return OK instead of revoked

		tfApplicationCheck.executeSteps("5", "", Arrays.asList(), testTool, dutExecutor);

		tfHandshakeNotSuccessfulCheck.executeSteps("6", "No TLS channel is established", null, testTool, tlsVersion);

		tfLocalServerClose.executeSteps("7", "Server closed successfully", Arrays.asList(),
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
		crl.cleanAndExit();
	}
}
