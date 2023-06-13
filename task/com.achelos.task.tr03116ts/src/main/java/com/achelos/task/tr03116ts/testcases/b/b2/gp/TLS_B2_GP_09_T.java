package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.CertificateChecker;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtSignatureAlgorithmsCertTls13;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;


/**
 * Test case TLS_B2_GP_09_T - Support for signature_algorithms_cert extension.
 * <p>
 * This positive test verifies that the DUT supports the ""signature_algorithms_cert"" extension. 
 * The test uses the signature algorithm and hash function [SIG_ALGORITHM_CERT]
 * <p>
 * The test MUST be repeated for each signature algorithm [SIG_ALGORITHM_CERT] 
 * supported by the DUT according to the ICS.
 */
public class TLS_B2_GP_09_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_09_T";
	private static final String TEST_CASE_DESCRIPTION = "Support for signature_algorithms_cert extension";
	private static final String TEST_CASE_PURPOSE
			= "This positive test verifies that the DUT supports the 'signature_algorithms_cert' extension. "
					+ "The test uses the signature algorithm and hash function [SIG_ALGORITHM_CERT] "
					+ "The test MUST be repeated for each signature algorithm [SIG_ALGORITHM_CERT] "
					+ "supported by the DUT according to the ICS.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;
	private final TFTLSVersionCheck tFTLSVersionCheck;


	public TLS_B2_GP_09_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
		tfClientCertificate = new TFClientCertificate(this);
		tFTLSVersionCheck = new TFTLSVersionCheck(this);
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
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the chosen cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate extension according to the ICS.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * <li>The request is supplied with the ""signature_algorithms_cert"" extension.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The certificate chain [CERT_DEFAULT_CLIENT] is signed using [SIG_ALGORITHM_CERT].
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_3;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION12_NOT_SUPPORTED);
			return;
		}

		/** supported signature algorithms */
		var sigAlgorithms = configuration.getSupportedSignatureAlgorithmsForCertificates();

		if (null == sigAlgorithms || sigAlgorithms.isEmpty()) {
			logger.error("No supported signature schemes found.");
			return;
		}
		logger.debug("Supported signature schemes");
		for (TlsSignatureAlgorithmWithHashTls13 sigAlg : sigAlgorithms) {
			logger.debug(sigAlg.toString());
		}

		step(1, "Setting TLS version: " + tlsVersion.getName() , null);

			/** any supported algorithm cipher suite */
			TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
			if (cipherSuite == null) {
				logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
				return;
			}
			logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

			int totalNumberOfIterations = sigAlgorithms.size();
			int iterationCount = 1;
			// repeat test for each signature algorithm
			for (TlsSignatureAlgorithmWithHashTls13 sigAlgorithm : sigAlgorithms) {
				
				tfClientCertificate.executeSteps("2",
						"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(),
						testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT);

				var sigAlgCertExtension = new TlsExtSignatureAlgorithmsCertTls13();
				sigAlgCertExtension.addSupportedSignatureAlgorithmCert(sigAlgorithm);

				tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
						+ ", cipher suite " + cipherSuite.name() + ", signature algorithm "
						+ sigAlgorithm.toString()
						+ " .",
						null, testTool, tlsVersion, cipherSuite, sigAlgCertExtension, TlsTestToolTlsLibrary.OpenSSL);

				testTool.start(iterationCount++, totalNumberOfIterations);

				tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(), testTool);

				step(3, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
				testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
				testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);
				
				tFTLSVersionCheck.executeSteps("4", "",
						Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool, tlsVersion,
						true);

				step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
						"The TLS protocol is executed without errors and the channel is established.");
				testTool.assertMessageLogged(TestToolResource.Handshake_successful);
				
				final List<X509Certificate> certList = testTool.findClientCertificateList();

				if (certList.isEmpty()) {
					logger.error("At least one certificate is expected, but none was received.");
				} else {
					CertificateChecker.assertCertificateSignature(certList.get(0), sigAlgorithm, logger);
				}

				testTool.resetProperties();
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
