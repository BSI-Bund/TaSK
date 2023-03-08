package com.achelos.task.tr03116ts.testcases.b.b2.fr;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.CertificateChecker;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPCloseConnection;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Testcase TLS_B2_FR_03 - Check Server Certificate
 * <p>
 * This test case checks the server certificate used by the DUT. In particular, the domain name and the signature are
 * verified.
 */
public class TLS_B2_FR_03 extends AbstractTestCase {
	
	private static final String TEST_CASE_ID = "TLS_B2_FR_03";
	private static final String TEST_CASE_DESCRIPTION = "Check Server Certificate";
	private static final String TEST_CASE_PURPOSE = 
			"This test case checks the server certificate used by the DUT. In particular, the domain name and the signature are verified.";
	
	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFClientCertificate tfClientCertificate;
	
	public TLS_B2_FR_03() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);
		
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
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
	 * <ol>
	 * <li>DUT services are online without any known disturbances.
	 * <li>The DUT is accepting TLS connections.
	 * </ol>
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
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello. Its answer contains a CertificateRequest.
	 * <li>It requests client authentication.
	 * <li>The DUT supplies a certificate. The signature of the certificate chain supplied by the DUT is correct, valid,
	 * not expired, not revoked and it is issued for the domain name that the client was connecting to.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
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
		
		tfClientCertificate.executeSteps("1",
				"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(),
				testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);

		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);
		testTool.start();

		tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(),
				testTool);
		
		step(4, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
		testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
		testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);

		step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		step(6, "The DUT supplies a certificate during the TLS handshake.", "The signature of the certificate"
				+ " chain supplied by the DUT is correct, valid, not expired, not revoked and it is issued "
				+ "for the domain name that the client was connecting to.");

		final List<X509Certificate> certChain = testTool.findServerCertificateList();
		if (certChain.isEmpty()) {
			logger.error("At least one certificate is expected, but none was received.");
			return;
		}

		// check signatures
		CertificateChecker.verifyCertificateSignature(certChain, logger);

		// Verify the validity
		CertificateChecker.verifyCertificateValidity(certChain, logger);

		// check revoked
		CertificateChecker.performCertificateRevocationCheck(certChain, logger);

		// check domain name
		CertificateChecker.checkCertificateDnsName(certChain.get(0), configuration.getDutAddress(),
				logger);

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
