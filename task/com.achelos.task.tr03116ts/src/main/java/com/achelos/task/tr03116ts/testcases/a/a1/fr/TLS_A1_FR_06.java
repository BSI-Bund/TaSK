package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtEncryptThenMac;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_FR_06 - Encrypt-then-MAC Extension
 * <p>
 * This test verifies that the Encrypt-then-MAC Extension is offered and can be used in a connection with a CBC-mode
 * cipher suite
 */
public class TLS_A1_FR_06 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_06";
	private static final String TEST_CASE_DESCRIPTION = "Encrypt-then-MAC Extension";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the Encrypt-then-MAC Extension is offered and can be used in a connection "
					+ "with a CBC-mode cipher suite";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;

	public TLS_A1_FR_06() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
	}

	@Override
	protected final void prepareEnvironment() throws Exception {
		testTool = new TlsTestToolExecutor(getTestCaseId(), logger);
		tShark = new TSharkExecutor(getTestCaseId(), logger);
		tShark.start();
		dutExecutor = new DUTExecutor(getTestCaseId(), logger, configuration.getDutCallCommandGenerator());
	}

	/**
	 * <h2>Precondition</h2>
	 * <ul>
	 * <li>The test TLS server is waiting for incoming TLS connections on [URL].
	 * </ul>
	 */
	@Override
	protected final void preProcessing() throws Exception {}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The TLS ClientHello offers at least one CBC-based cipher suite.
	 * <li>The TLS ClientHello offers the Encrypt-then-MAC extension.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a TLS version and a CBC-based cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The server selects the Encrypt-then-MAC extension.
	 * <li>No further extensions are supplied by the server.
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

		/** All supported cipher suites */
		var cipherSuites = configuration.getCBCBasedSupportedCipherSuites(tlsVersion);

		if (cipherSuites.size() == 0) {
			logger.error(MessageConstants.NO_SUPPORTED_CBC_CIPHER_SUITES);
			return;
		}

		logger.debug("LoggingMessages.SUPPORTED_CBC_CIPHER_SUITES");
		for (TlsCipherSuite cipherSuite : cipherSuites) {
			logger.debug(cipherSuite.name());
		}

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and cipher suites: "
				+ cipherSuites + " and Encrypt-then-MAC extension", null);

		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuites, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuites, new TlsExtEncryptThenMac());

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		step(5, "Check if the TLS ClientHello offers at least one CBC-based cipher suite.",
				"TLS ClientHello offers at least one CBC-based cipher suite.");
		List<TlsCipherSuite> clientHelloCipherSuites = TlsCipherSuite
				.parseCipherSuiteStringList(testTool.getValue(TestToolResource.ClientHello_cipher_suites));
		List<TlsCipherSuite> cbcCipherSuites = clientHelloCipherSuites.stream()
				.filter(cipherSuite -> cipherSuite.name().contains("_CBC_")).collect(Collectors.toList());
		if (cbcCipherSuites != null && !cbcCipherSuites.isEmpty()) {
			logger.info("The TLS ClientHello offers following CBC-based cipher suites: " + cbcCipherSuites.toString());
		} else {
			logger.error("The TLS ClientHello does not offer CBC-based cipher suites.");
		}

		step(6, "Check if the TLS ClientHello offers the Encrypt-then-MAC extension.",
				"ClientHello contains the encrypt-then-mac extension.");
		testTool.assertClientSupportsExtension(TlsExtensionTypes.encrypt_then_mac);

		step(7, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		tfApplicationCheck.executeSteps("8", "", Arrays.asList(), testTool, dutExecutor);

		tfLocalServerClose.executeSteps("9", "Server closed successfully", Arrays.asList(),
				testTool);

	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
		dutExecutor.cleanAndExit();
	}
}
