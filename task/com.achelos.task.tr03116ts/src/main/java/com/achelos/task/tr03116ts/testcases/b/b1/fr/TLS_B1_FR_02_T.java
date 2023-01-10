package com.achelos.task.tr03116ts.testcases.b.b1.fr;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtTruncatedHmac;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPCloseConnection;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * TLS_B1_FR_02_T - No Truncated HMAC extension.
 * <p>
 * This test case checks that the Truncated HMAC extension is not selected by the DUT.
 * <p>
 * The test MUST be repeated for each combination of TLS version [TLS_VERSION] and CBC- based cipher suites
 * [CIPHERSUITE] supported by the DUT for incoming TLS connections.
 */
public class TLS_B1_FR_02_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_FR_02_T";
	private static final String TEST_CASE_DESCRIPTION = "No Truncated HMAC extension";
	private static final String TEST_CASE_PURPOSE
			= "This test case checks that the Truncated HMAC extension is not selected by the DUT.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTCPIPCloseConnection tFTCPIPCloseConnection;
	private final TFTLSClientHello tfClientHello;


	public TLS_B1_FR_02_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTCPIPCloseConnection = new TFTCPIPCloseConnection(this);
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
	 * <li>DUT services are online without any known disturbances.</li>
	 * <li>The DUT is accepting TLS connections.</li>
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
	 * <li>The TLS ClientHello offers the truncated_hmac extension.
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
	 * <li>The DUT did not select the truncated_hmac extension.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		List<TlsVersion> tlsVersions = configuration.getSupportedTLSVersions();
		logger.debug("Supported TLS versions:");
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.name());
		}
		if (tlsVersions.size() == 0) {
			logger.error("No supported TLS versions found.");
			return;
		}

		int iterationCount = 1;
		for (TlsVersion tlsVersion : tlsVersions) {

			var cipherSuites = configuration.getCBCBasedSupportedCipherSuites(tlsVersion);
			logger.debug("Supported CBC Cipher suites:");
			for (TlsCipherSuite cipherSuite : cipherSuites) {
				logger.debug(cipherSuite.name());
			}

			if (cipherSuites.size() == 0) {
				logger.error("No supported CBC Cipher suites found.");
				continue;
			}

			for (TlsCipherSuite cipherSuite : cipherSuites) {

				tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
						+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion, cipherSuite,
						new TlsExtTruncatedHmac());
				testTool.start(iterationCount, tlsVersions.size() * cipherSuites.size());
				iterationCount++;

				/* open connection */
				tFTCPIPNewConnection.executeSteps("2", "",
						Arrays.asList(), testTool);

				step(3, "The DUT does not select the truncated_hmac extension.",
						"ServerHello does not contain the truncated_mac extension.");

				testTool.assertMessageLogged(TestToolResource.Handshake_successful);
				testTool.assertServerLacksExtension(TlsExtensionTypes.truncated_hmac);

				/* close connection */
				tFTCPIPCloseConnection.executeSteps("4", "", Arrays.asList(), testTool);
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
