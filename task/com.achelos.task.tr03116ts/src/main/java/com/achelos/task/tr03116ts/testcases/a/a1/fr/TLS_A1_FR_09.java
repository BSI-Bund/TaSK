package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_FR_09 - Compression not used.
 * <p>
 * This test verifies that compression is not offered and cannot be used in a connection.
 */
public class TLS_A1_FR_09 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_09";
	private static final String TEST_CASE_DESCRIPTION = "Compression not used";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that compression is not offered and cannot be used in a connection.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;


	public TLS_A1_FR_09() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
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
	 * <li>The TLS ClientHello offers the compression extension set to zero.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The server selects the compression method DEFLATE despite it was not offered.
	 * <li>No further extensions are supplied by the server.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT rejects the connection with a ""unsupported_extension"" alert or another suitable error description.
	 * <li>No TLS channel is established.
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

		/** one supported cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and cipher suite: "
				+ cipherSuite, null);

		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuite);
		testTool.manipulateServerHelloCompressionMethod(TestToolResource.Compression_DEFLATE);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		step(5, "Check if the TLS ClientHello offers the compression extension set to zero",
				"The TLS ClientHello offers the compression extension set to zero.");

		String compressionMethods = testTool.getValue(TestToolResource.ClientHello_compression_methods);

		if (TestToolResource.Compression_null.getInternalToolOutputMessage().equals(compressionMethods)) {
			logger.info("The TLS ClientHello offers the compression extension set to zero.");
		} else {
			logger.error("The TLS ClientHello does not offer the compression extension set to zero.");
		}

		// Check for unsupported_extension.
		tfAlertMessageCheck.executeSteps("6",
				"The DUT rejects the connection with a \"unsupported_extension\" alert or another suitable error "
						+ "description.",
				Arrays.asList("level=warning/fatal",
						"description=unsupported_extension or another suitable error description."),
				testTool, TlsAlertDescription.unsupported_extension);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfHandshakeNotSuccessfulCheck.executeSteps("8", "No TLS channel is established", null, testTool, tlsVersion);

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
