package com.achelos.task.tr03116ts.testcases.a.a2.fr;


import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.CertificateChecker;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tools.StringTools;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.tr03116ts.testfragments.RequestClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFApplicationSpecificInspectionCheck;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A2_FR_04 - CA of the client certificate.
 * 
 * <p>
 * This test verifies the CA of the client certificate that is sent by the DUT upon request.
 *
 */
public class TLS_A2_FR_04 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_FR_04";
	private static final String TEST_CASE_DESCRIPTION = "CA of the client certificate";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the CA of the client certificate that is sent by the DUT upon request.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;


	public TLS_A2_FR_04() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
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
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
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
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>The server supplies the certificate chain [CERT_DEFAULT].
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT supplies a valid client certificate that was issued by the CA stated in the ICS.
	 * <li>The TLS protocol is executed without errors and the channel is established.
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
		
		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3",
				"The TLS server answers the DUT choosing a TLS version and a cipher suite that is"
						+ " contained in the ClientHello.",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, new RequestClientCertificate(), TlsTestToolTlsLibrary.OpenSSL);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		step(5, "Verify that the certificate chain supplied by the DUT was issued by the CA that is stated in the ICS",
				null);
		final List<X509Certificate> certList = testTool.findClientCertificateList();

		if (certList.size() < 1) {
			logger.error("At least one intermediate certificate is expected, but none was received.");
		} else {
			var certificationInfo = configuration.getTR03145CertificationInfo();
			if (certificationInfo == null) {
				logger.error("The certification information in Table 11 is missing.");
			} else {
				CertificateChecker.assertCertifiedCA(certList.get(0),
						certificationInfo.getSubject(),
						certificationInfo.getValidityNotAfter(),
						certificationInfo.getValidityNotBefore(),
						certificationInfo.getSubjectKeyIdentifier(),
						certificationInfo.getBSICertificateNumber(),
						logger);
			}
		}
		
		step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
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
