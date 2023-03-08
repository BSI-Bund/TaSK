package com.achelos.task.tr03116ts.testcases.b.b2.fr;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtEncryptThenMac;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B2_FR_01_T - Encrypt-then-MAC extension.
 * <p>
 * These test cases verify that the Encrypt then MAC extension is used if the clients offers it. The test is carried out
 * for the TLS version [TLS_VERSION] and the CBC-based cipher suite [CIPHERSUITE].
 * <p>
 * The test MUST be repeated for each combination of TLS version [TLS_VERSION], PFS algorithm [CIPHERSUITE] using ECDHE
 * and elliptic curve domain parameters [GROUP] supported by the DUT for incoming TLS connections.
 */
public class TLS_B2_FR_01_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_FR_01_T";
	private static final String TEST_CASE_DESCRIPTION = "Encrypt-then-MAC extension";
	private static final String TEST_CASE_PURPOSE
			= "These test cases verify that the Encrypt then MAC extension is used if the clients offers it. "
					+ "The test is carried out for the TLS version [TLS_VERSION] and the CBC-based cipher suite "
					+ "[CIPHERSUITE].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_FR_01_T() {
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
	 * <li>The TLS ClientHello offers the TLS version [TLS_VERSION].
	 * <li>The TLS ClientHello offers only the cipher suite [CIPHERSUITE].
	 * <li>The TLS ClientHello offers the encrypt_then_mac extension.
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
	 * <li>The DUT selected the encrypt_then_mac extension.
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

		// all unsupported tls version
		TlsVersion tlsVersion = TlsVersion.TLS_V1_2;

		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		if (!configuration.getSupportedTLSVersions().contains(tlsVersion)) {
			logger.error(MessageConstants.TLS_VERSION12_NOT_SUPPORTED);
			return;
		}

		int iterationCount = 1;

		var cipherSuites = configuration.getCBCBasedSupportedCipherSuites(tlsVersion);
		logger.debug("LoggingMessages.SUPPORTED_CBC_CIPHER_SUITES");
		for (TlsCipherSuite cipherSuite : cipherSuites) {
			logger.debug(cipherSuite.name());
		}

		if (cipherSuites.size() == 0) {
			logger.error(MessageConstants.NO_SUPPORTED_CBC_CIPHER_SUITES);
			return;
		}

		for (TlsCipherSuite cipherSuite : cipherSuites) {

			tfClientCertificate.executeSteps("1",
					"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(),
					testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);


			tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
					+ ", cipher suite " + cipherSuite.name() + " .", null, testTool,
					tlsVersion, cipherSuite, new TlsExtEncryptThenMac());
			testTool.start(iterationCount, cipherSuites.size());
			iterationCount++;

			/* open connection */
			tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(), testTool);

			step(4, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
			testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
			testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);

			step(5, "Check if the DUT selected the encrypt_then_mac extension..",
					"The DUT selected the encrypt_then_mac extension.");
			testTool.assertServerSupportsExtension(TlsExtensionTypes.encrypt_then_mac);

			step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
					"The TLS protocol is executed without errors and the channel is established.");
			testTool.assertMessageLogged(TestToolResource.Handshake_successful);

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
