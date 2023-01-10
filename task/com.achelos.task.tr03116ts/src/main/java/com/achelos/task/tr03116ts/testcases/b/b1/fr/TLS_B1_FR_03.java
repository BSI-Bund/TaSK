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


/**
 * <p>
 * This test case checks the server certificate used by the DUT. In particular, the domain name and the signature are
 * verified.
 */
public class TLS_B1_FR_03 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_03";
	private static final String TEST_CASE_DESCRIPTION = "Check Server Certificate";
	private static final String TEST_CASE_PURPOSE
			= "This test case checks the server certificate used by the DUT. In particular, the domain name and the signature are verified.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTCPIPCloseConnection tFTCPIPCloseConnection;

	public TLS_B1_FR_03() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfClientHello = new TFTLSClientHello(this);
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTCPIPCloseConnection = new TFTCPIPCloseConnection(this);
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
	 * <li>The DUT supplies a certificate during the TLS handshake. The signature of the certificate chain supplied by
	 * the DUT is correct, valid, not expired, not revoked and it is issued for the domain name that the client was
	 * connecting to.
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

		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite);
		testTool.start();

		tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(),
				testTool);

		tFTCPIPCloseConnection.executeSteps("3", "", Arrays.asList(),
				testTool);

		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		step(4, "The DUT supplies a certificate during the TLS handshake.", "The signature of the certificate"
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
