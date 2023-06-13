package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_B2_GP_05_T - Reject unsupported TLS version.
 * <p>
 * This test verifies that no downgrade to a TLS version that is not supported according to the ICS is possible.
 * <p>
 * The test MUST be repeated for each TLS version unsupported by the DUT according to the ICS (cf. Table 20).
 */
public class TLS_B2_GP_05_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_05_T";
	private static final String TEST_CASE_DESCRIPTION = "Reject unsupported TLS version";
	private static final String TEST_CASE_PURPOSE = "This test verifies that no downgrade to a TLS version that "
			+ "is not supported according to the ICS is possible.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_B2_GP_05_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tfClientHello = new TFTLSClientHello(this);
		tfClientCertificate = new TFClientCertificate(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
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
	 * <li>The tester chooses a SSL/TLS version that is not supported according to the ICS.
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
	 * <li>The TLS ClientHello offers a TLS version that is not supported according to the ICS.
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the chosen cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the
	 * appropriate extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT rejects the ClientHello with a ""protocol_version"" alert or another suitable error description.
	 * <li>No TLS connection is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		List<TlsVersion> tlsVersions = configuration.getNotSupportedTLSVersions();
		if (null == tlsVersions || tlsVersions.isEmpty()) {
			logger.error("No unsupported TLS versions found.");
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.getName());
		}

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite
				= configuration.getSingleSupportedCipherSuite(TlsVersion.TLS_V1_2);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		step(1, "The tester connects to the DUT.", "");

		// repeat test for each un supported TLS version
		int iterationCount = 1;
		for (TlsVersion tlsVersion : tlsVersions) {
			
			tfClientCertificate.executeSteps("2",
					"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(),
					testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT);

			tfClientHello.executeSteps("3", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
					+ ", cipher suite " + cipherSuite.getName() + " .", null, testTool, tlsVersion, cipherSuite);
			testTool.start(iterationCount, tlsVersions.size());
			iterationCount++;

			tFTCPIPNewConnection.executeSteps("4", "", Arrays.asList(), testTool);

			if(testTool.assertMessageLogged(TestToolResource.Handshake_successful, BasicLogger.INFO)){
				logger.error("The handshake was successful");
			}

			tfAlertMessageCheck.executeSteps("5",
					"The DUT rejects the ClientHello with a \"protocol_version\" alert or "
							+ "	another suitable error description",
					Arrays.asList("level=warning/fatal", "description=protocol_version"), testTool, TlsAlertDescription.protocol_version);
			tfHandshakeNotSuccessfulCheck.executeSteps("6", "No TLS channel is established", null, testTool, tlsVersion);

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
