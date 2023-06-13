package com.achelos.task.tr03116ts.testcases.a.a2.ch;


import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commons.enums.*;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.ManipulateForceCertificateUsage;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls12;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.RequestClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFApplicationSpecificInspectionCheck;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFHandshakeNotSuccessfulCheck;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSHighestVersionSupportCheck;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A2_CH_08 - Server certificate uses unsupported signature algorithm.
 * 
 * <p>
 * This test verifies the behaviour of the DUT in case the server presents a certificate that uses an unsupported
 * signature algorithm.
 *
 */
public class TLS_A2_CH_08 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_CH_08";
	private static final String TEST_CASE_DESCRIPTION = "Server certificate uses unsupported signature algorithm";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT in case the server presents a certificate "
					+ "that uses an unsupported signature algorithm.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFTLSHighestVersionSupportCheck fFTLSHighestVersionSupportCheck;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_A2_CH_08() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		fFTLSHighestVersionSupportCheck = new TFTLSHighestVersionSupportCheck(this);
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
	 * <li>The TLS ClientHello offers the highest TLS version stated in the ICS.
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
	 * <li>The server supplies a certificate chain [CERT_DEFAULT] with certificates that are signed using an algorithm that is not supported according to the ICS.
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT rejects the connection with a ""bad_certificate"" alert or another suitable error description.
	 * <li>No TLS channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** highest supported TLS Version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		/** one supported CipherSuite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and cipher suite: "
				+ cipherSuite, null);

		TlsSignatureAlgorithmWithHash unsupportedSignatureAlgorithmWithHash
				= findUnsupportedSignatureAlgorithmWithHash(tlsVersion);

		if (null == unsupportedSignatureAlgorithmWithHash) {
			logger.warning("No unsupported SignatureAlgorithmWithHash available. ");
			return;
		}

		tfserverCertificate.executeSteps("2", "The server supplies a certificate chain [CERT_DEFAULT] with certificates"
						+ " that are signed using an algorithm that is not supported according to the ICS.",
				Arrays.asList(), testTool, tlsVersion, unsupportedSignatureAlgorithmWithHash,
				TlsTestToolCertificateTypes.CERT_DEFAULT, new ManipulateForceCertificateUsage());

		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuite, unsupportedSignatureAlgorithmWithHash, new RequestClientCertificate());

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		fFTLSHighestVersionSupportCheck.executeSteps("5",
				"The TLS ClientHello offers the highest TLS version stated in the ICS.", null,
				testTool);

		tfAlertMessageCheck.executeSteps("6", "The DUT rejects the connection with a \"bad_certificate\" alert or "
						+ "another suitable error description.",
				Arrays.asList("level=warning/fatal", "description=bad_certificate"), testTool, TlsAlertDescription.bad_certificate);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfHandshakeNotSuccessfulCheck.executeSteps("8", "No TLS channel is established", null, testTool, tlsVersion);

		tfLocalServerClose.executeSteps("9", "Server closed successfully", Arrays.asList(),
				testTool);

	}

	private TlsSignatureAlgorithmWithHash findUnsupportedSignatureAlgorithmWithHash(final TlsVersion tlsVersion) {
		var clientSupportedSigAlg = configuration.getSupportedSignatureAlgorithms(tlsVersion);

		if(tlsVersion == TlsVersion.TLS_V1_2) {
			for (TlsSignatureAlgorithmWithHash supportedCertificateType : TlsSignatureAlgorithmWithHashTls12
					.getSupportedCertificateTypesTls12()) {
				if (!containsSignatureAlgorthm(clientSupportedSigAlg, supportedCertificateType)) {
					return supportedCertificateType;
				}
			}
		} else {
			for (TlsSignatureAlgorithmWithHash supportedCertificateType : TlsSignatureScheme.getTls13SignatureSchemes()) {
				if (!containsSignatureAlgorthm(clientSupportedSigAlg, supportedCertificateType)) {
					return supportedCertificateType;
				}
			}
		}
		// If we are here, then the Client supports all common signature algorithms.
		// We proceed by choosing an uncommon signature algorithm.
		logger.info("Client supports all common SignatureAlgorithms available.");
		logger.info("Using uncommon signature algorithm: RSAWithSHA1");
		return new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha1);
	}

	public boolean containsSignatureAlgorthm(List<TlsSignatureAlgorithmWithHash> list, TlsSignatureAlgorithmWithHash value){
		for(TlsSignatureAlgorithmWithHash element: list){
			if(element.equals(value)){
				return true;
			}
		}
		return false;
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
