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
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;


/**
 * Testcase TLS_B2_FR_04 - Certificate chain signatures
 * <p>
 * This test case checks the server certificate used by the DUT. The server certificate must be signed by a CA certified
 * according to [TR-03145].
 */
public class TLS_B2_FR_04 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_FR_04";
	private static final String TEST_CASE_DESCRIPTION = "Certificate chain signatures";
	private static final String TEST_CASE_PURPOSE
			= "This test case checks the server certificate used by the DUT. The server certificate must be signed by a"
					+ " CA certified according to [TR-03145].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFTLSVersionCheck tFTLSVersionCheck;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_FR_04() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
		tFTLSVersionCheck = new TFTLSVersionCheck(this);
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
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * <li>The DUT supplies a certificate. The certificate chain supplied by the DUT was issued by the CA that is stated
	 * in the ICS.
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

		/** highest supported TLS Version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		/** one supported ECC algorithm CipherSuite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.name());


		tfClientCertificate.executeSteps("1",
				"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(), testTool,
				tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);


		// configure client hello
		tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);

		testTool.start();

		// connect
		tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(), testTool);

		step(4, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
		testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
		testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);

		tFTLSVersionCheck.executeSteps("5", "",
				Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool, tlsVersion, true);

		step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		step(7, "Verify that the certificate chain supplied by the DUT was issued by the CA that is stated in the ICS",
				null);
		final List<X509Certificate> certList = testTool.findServerCertificateList();

		if (certList.size() < 2) {
			logger.error("At least one intermediate certificate is expected, but none was received.");
		} else {
			var certificationInfo = configuration.getTR03145CertificationInfo();
			if (certificationInfo == null) {
				logger.error("The certification information in Table 11 is missing.");
			} else {
				CertificateChecker.assertCertifiedCA(certList.get(1),
						certificationInfo.getSubject(),
						certificationInfo.getValidityNotAfter(),
						certificationInfo.getValidityNotBefore(),
						certificationInfo.getSubjectKeyIdentifier(),
						certificationInfo.getBSICertificateNumber(),
						logger);
			}
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

