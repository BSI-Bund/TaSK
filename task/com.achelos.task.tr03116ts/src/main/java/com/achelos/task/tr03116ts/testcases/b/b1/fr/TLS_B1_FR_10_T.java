package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * <p>
 * This positive test verifies the behaviour of the DUT when a correct PSK is used. The test is carried out for the TLS
 * version [TLS_VERSION] and the PSK cipher suite [CIPHERSUITE].
 * <p>
 * The test MUST be repeated for all TLS versions [TLS_VERSION] and PSK cipher suites [CIPHERSUITE] listed in the ICS of
 * the DUT.
 */
public class TLS_B1_FR_10_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_10_T";
	private static final String TEST_CASE_DESCRIPTION = "correct use of PSK cipher suite";
	private static final String TEST_CASE_PURPOSE
			= "This positive test verifies the behaviour of the DUT when a correct PSK is used. "
					+ "The test is carried out for the TLS version [TLS_VERSION] and the PSK cipher suite "
					+ "[CIPHERSUITE].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;

	public TLS_B1_FR_10_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
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
	protected void preProcessing() throws Exception {

	}


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
	 * <li>The DUT accepts the the ClientHello. It may send a ""PSK identity hint""
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client uses a valid PSK according to the ICS.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	@Override
	protected void executeUsecase() throws Exception {

		// TBD: This test case is not applicable for the TLS Server Profile and will be covered in a later milestone

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_2;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION12_NOT_SUPPORTED);
			return;
		}

		// Repeat test for all supported TLS versions
		int iterationCount = 1;

		var cipherSuites = configuration.getSupportedPSKCipherSuites(tlsVersion);

		if (null == cipherSuites || cipherSuites.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_PSK_CIPHER_SUITE_FOR_TLS_VERSION + tlsVersion.getName());
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_PSK_CIPHER_SUITES);
		for (TlsCipherSuite cipherSuite : cipherSuites) {
			logger.debug(cipherSuite.name());
		}

		// Repeat test for all supported PSK cipher suites
		for (TlsCipherSuite cipherSuite : cipherSuites) {


			tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
					+ ", cipher suite " + cipherSuite.getName() + " .", null, testTool, tlsVersion, cipherSuite);


			step(3, "The TLS client uses a valid PSK according to the ICS.", null);

			testTool.start(iterationCount,  cipherSuites.size());
			iterationCount++;

			tFTCPIPNewConnection.executeSteps("4", "", Arrays.asList(), testTool);

			step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
					"The TLS protocol is executed without errors and the channel is established.");
			testTool.assertMessageLogged(TestToolResource.Handshake_successful);

			testTool.resetProperties();
		}

	}


	@Override
	protected void postProcessing() throws Exception {

	}


	@Override
	protected final void cleanAndExit() throws Exception {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}
