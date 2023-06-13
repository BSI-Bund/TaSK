package com.achelos.task.tr03116ts.testcases.b.b1.gp;

import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;


/**
 * Test case TLS_B1_GP_01_T - Supported TLS versions.
 * <p>
 * This positive test evaluates the ability of the DUT to establish a TLS connection with valid parameters. The test is
 * carried out for the TLS version [TLS_VERSION] and the cipher suite [CIPHERSUITE].
 * <p>
 * The test MUST be repeated for each TLS version [TLS_VERSION] and non-ECC algorithm [CIPHERSUITE] combination
 * supported by the DUT for incoming TLS connections.
 */
public class TLS_B1_GP_01_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_GP_01_T";
	private static final String TEST_CASE_DESCRIPTION = "Supported TLS versions";
	private static final String TEST_CASE_PURPOSE
			= "This positive test evaluates the ability of the DUT to establish a TLS connection with valid "
					+ "parameters. The test is carried out for the TLS version [TLS_VERSION] and the cipher suite [CIPHERSUITE].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSVersionCheck tFTLSVersionCheck;
	private final TFTLSClientHello tfClientHello;

	public TLS_B1_GP_01_T() {
		super();
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
	protected final void preProcessing() throws Exception {}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the TLS version [TLS_VERSION].
	 * <li>The TLS ClientHello offers only the cipher suite [CIPHERSUITE].
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/* all supported TLS versions */
		var tlsVersions = configuration.getSupportedTLSVersions();
		if (null == tlsVersions || tlsVersions.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.getName());
		}

		//Find out the number of iterations
		int iterationCount = 1;
		int totalNumberOfIterations = 0;
		for(TlsVersion tlsVersion : tlsVersions){
			totalNumberOfIterations  += configuration.getSupportedNonECCCipherSuites(tlsVersion).size();
		}
		// repeat test for each supported TLS version
		for (TlsVersion tlsVersion : tlsVersions) {

			/* supported non-ECC algorithm cipher suites */
			var cipherSuites = configuration.getSupportedNonECCCipherSuites(tlsVersion);

			if (null == cipherSuites || cipherSuites.isEmpty()) {
				logger.error("No supported non-ECC cipher suites found.");
				continue;
			}
			logger.debug("Supported non-ECC Cipher suites:");
			for (TlsCipherSuite cipherSuite : cipherSuites) {
				logger.debug(cipherSuite.getName());
			}

			// repeat test for all supported non-ecc cipher suite
			for (TlsCipherSuite cipherSuite : cipherSuites) {

				tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
						+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, TlsTestToolTlsLibrary.OpenSSL, cipherSuite, tlsVersion);
				testTool.start(iterationCount, totalNumberOfIterations);
				iterationCount++;

				tFTCPIPNewConnection.executeSteps("2", "", Arrays.asList(), testTool);

				tFTLSVersionCheck.executeSteps("3", "",
						Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool, tlsVersion,
						true);


				step(4, "Check if the TLS protocol is executed without errors and the channel is established.",
						"The TLS protocol is executed without errors and the channel is established.");
				testTool.assertMessageLogged(TestToolResource.Handshake_successful);

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
