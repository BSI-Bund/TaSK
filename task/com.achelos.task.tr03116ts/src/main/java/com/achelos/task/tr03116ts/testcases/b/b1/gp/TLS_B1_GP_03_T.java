package com.achelos.task.tr03116ts.testcases.b.b1.gp;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.CertificateChecker;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;


/**
 * Test case TLS_B1_GP_03_T - signature_algorithms extension.
 * <p>
 * This positive test verifies that the DUT supports the signature algorithms extension. The test uses the signature
 * algorithm and hash function [SIG_ALGORITHM].
 * <p>
 * The test MUST be repeated for each signature algorithm [SIG_ALGORITHM] supported by the DUT according to the ICS.
 */
public class TLS_B1_GP_03_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_GP_03_T";
	private static final String TEST_CASE_DESCRIPTION = " signature_algorithms extension";
	private static final String TEST_CASE_PURPOSE
			= "This positive test verifies that the DUT supports the signature algorithms extension. "
					+ "The test uses the signature algorithm and hash function [SIG_ALGORITHM].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSVersionCheck tFTLSVersionCheck;
	private final TFTLSClientHello tfClientHello;

	public TLS_B1_GP_03_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);


		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTLSVersionCheck = new TFTLSVersionCheck(this);
		tfClientHello = new TFTLSClientHello(this);
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
		// tFDUTServer12.executeSteps("1", "", Arrays.asList(),
		// testTool);
	}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>The TLS ClientHello offers the signature_algorithms extension containing [SIG_ALGORITHM].
	 * <li>In case the chosen cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the
	 * appropriate extension according to the ICS.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The DUT provides a certificate chain with certificates signed using [SIG_ALGORITHM].
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** supported TLS versions */
		List<TlsVersion> tlsVersions = configuration.getSupportedTLSVersions();
		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.name());
		}
		if (tlsVersions.size() == 0) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}

		//Find out the number of iterations
		int iterationCount = 1;
		int totalNumberOfIterations = 0;
		for(TlsVersion tlsVersion : tlsVersions){
			totalNumberOfIterations  += configuration.getSupportedSignatureAlgorithms(tlsVersion).size();
		}

		// repeat test for each supported TLS version with signature algorithms
		for (TlsVersion tlsVersion : tlsVersions) {

			/** any supported algorithm cipher suite */
			TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
			if (cipherSuite == null) {
				logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
				continue;
			}
			logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

			/** supported signature algorithms */
			var sigAlgorithms = configuration.getSupportedSignatureAlgorithms(tlsVersion);
			if (sigAlgorithms.size() == 0) {
				logger.error("No supported Signature Algorithms found.");
				continue;
			}

			// repeat test for each signature algorithm
			for (TlsSignatureAlgorithmWithHash sigAlgorithm : sigAlgorithms) {

				tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
						+ ", cipher suite " + cipherSuite.name() + ", signature algorithm "
						+ sigAlgorithm.toString()
						+ " .",
						null, testTool, tlsVersion, cipherSuite, sigAlgorithm);

				testTool.start(iterationCount, totalNumberOfIterations);
				iterationCount++;

				tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(), testTool);

				tFTLSVersionCheck.executeSteps("3", "",
						Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool, tlsVersion,
						true);

				step(4, "Check if the TLS protocol is executed without errors and the channel is established.",
						"The TLS protocol is executed without errors and the channel is established.");
				testTool.assertMessageLogged(TestToolResource.Handshake_successful);

				final List<X509Certificate> certList = testTool.findServerCertificateList();

				if (certList.isEmpty()) {
					logger.error("At least one certificate is expected, but none was received.");
				} else {
					CertificateChecker.assertCertificateSignature(certList.get(0), sigAlgorithm, logger);
				}

				testTool.resetProperties();
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
