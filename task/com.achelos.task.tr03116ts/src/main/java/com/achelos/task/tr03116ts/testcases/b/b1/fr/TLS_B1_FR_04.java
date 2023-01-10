package com.achelos.task.tr03116ts.testcases.b.b1.fr;

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
import com.achelos.task.tr03116ts.testfragments.TFTCPIPCloseConnection;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;


/**
 * <p>
 * This test case checks the server certificate used by the DUT. The server certificate must be signed by a CA certified
 * according to [TR-03145].
 */
public class TLS_B1_FR_04 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_04";
	private static final String TEST_CASE_DESCRIPTION = "Certificate chain signatures";
	private static final String TEST_CASE_PURPOSE
			= "This test case checks the server certificate used by the DUT. The server certificate must be signed by a CA certified according to [TR-03145].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTCPIPCloseConnection tFTCPIPCloseConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFTLSVersionCheck tFTLSVersionCheck;

	public TLS_B1_FR_04() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTCPIPCloseConnection = new TFTCPIPCloseConnection(this);
		tfClientHello = new TFTLSClientHello(this);
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
	protected final void preProcessing() throws Exception {}

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
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT supplies a certificate during the TLS handshake. The certificate chain supplied by the DUT was issued
	 * by the CA that is stated in the ICS.
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

		/** one supported ECC algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.warning("No supported cipher suite found.");
			return;
		}
		logger.debug("Supported CipherSuite: " + cipherSuite.name());


		// configure client hello
		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);

		testTool.start();

		// connect
		tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(), testTool);

		tFTLSVersionCheck.executeSteps("3", "",
				Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool, tlsVersion, true);

		tFTCPIPCloseConnection.executeSteps("4", "", Arrays.asList(),
				testTool);

		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		step(5, "Verify that the certificate chain supplied by the DUT was issued by the CA that is stated in the ICS",
				"");

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

