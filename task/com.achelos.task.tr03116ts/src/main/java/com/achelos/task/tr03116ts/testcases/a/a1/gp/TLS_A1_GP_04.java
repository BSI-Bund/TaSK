package com.achelos.task.tr03116ts.testcases.a.a1.gp;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Test case TLS_A1_GP_04 - Unsupported cipher suite
 * <p>
 * This test checks the correct behaviour of the DUT in case the server can only use an unsupported cipher suite
 * according to the ICS.
 */
public class TLS_A1_GP_04 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_GP_04";
	private static final String TEST_CASE_DESCRIPTION = "Unsupported cipher suite";
	private static final String TEST_CASE_PURPOSE
			= "This test checks the correct behaviour of the DUT in case the server can only use an unsupported "
					+ "cipher suite according to the ICS.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;


	public TLS_A1_GP_04() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
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
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server is restricted to only use cipher suites that are not listed in the ClientHello and not
	 * supported according to the ICS. Therefore the Server rejects the connection.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server is configured to use the certificate chain [CERT_DEFAULT].
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>No TLS connection is established.
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

		/** Unsupported cipher suite */
		var unsupportedClientCipherSuites = configuration.getNotSupportedCipherSuites(tlsVersion);
		if (unsupportedClientCipherSuites == null || unsupportedClientCipherSuites.isEmpty()) {
			logger.error("No supported cipher suite is found.");
			return;
		}

		/** select cipherSuite that is not supported in ICS */
		logger.debug("Selected unsupported CipherSuites: ");
		for (TlsCipherSuite unsupportedClientCipherSuite : unsupportedClientCipherSuites) {
			logger.debug(unsupportedClientCipherSuite.name());
		}

		tfserverCertificate.executeSteps("1",
				"The TLS server is configured to use the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, unsupportedClientCipherSuites,
				TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("2", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, unsupportedClientCipherSuites);

		tFDutClientNewConnection.executeSteps("3",
				"The TLS server receives a ClientHello handshake message from the DUT.",
				Arrays.asList(), testTool, dutExecutor);

		step(4, "Check if the Server rejects the connection.", "No TLS connection is established.");

		testTool.assertMessageLogged(TestToolResource.Handshake_failed);

		tfLocalServerClose.executeSteps("5", "Server closed successfully", Arrays.asList(),
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
