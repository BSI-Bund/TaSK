package com.achelos.task.tr03116ts.testcases.a.a1.ch;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.certificatehelper.ManipulateForceCertificateUsage;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Testcase TLS_A1_CH_06_T - Server cert based on wrong domain parameters
 * 
 * <p>
 * This test case checks the behavior of the DUT in case the server offers a TLS server certificate based on not
 * conforming domain parameters.
 * <p>
 * Depending on the client´s capabilities, the test MUST be repeated for DSA, RSA and ECDSA based certificates
 * [CERT_SHORT_KEY].
 * 
 */
public class TLS_A1_CH_06_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_CH_06_T";
	private static final String TEST_CASE_DESCRIPTION = "Server cert based on wrong domain parameters";
	private static final String TEST_CASE_PURPOSE
			= "This test case checks the behavior of the DUT in case the server offers a TLS server certificate "
					+ "based on not conforming domain parameters.";
	
	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;


	public TLS_A1_CH_06_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);
		
		tfLocalServerClose = new TFLocalServerClose(this);
		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
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
	protected final void preProcessing() throws Exception {
	}

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
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The server supplies a certificate chain [CERT_SHORT_KEY] with a key length not conform to the application
	 * requirements.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT rejects the connection with a ""bad_certificate"" alert or another suitable error description.
	 * <li>No TLS channel is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all supported tls versions
		List<TlsVersion> tlsVersions = configuration.getSupportedTLSVersions();
		if (null == tlsVersions || tlsVersions.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}

		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.name());
		}

		for (TlsVersion tlsVersion : tlsVersions) {

			var dsaCipherSuites = configuration.getSupportedDsaCipherSuites(tlsVersion);
			var rsaCipherSuites = configuration.getSupportedRsaCipherSuites(tlsVersion);
			var ecdsaCipherSuites = configuration.getSupportedEcdsaCipherSuites(tlsVersion);

			HashMap<String, List<TlsCipherSuite>> certificateTypeCipherSuites = new HashMap<>();
			certificateTypeCipherSuites.put("DSA", dsaCipherSuites);
			certificateTypeCipherSuites.put("RSA", rsaCipherSuites);
			certificateTypeCipherSuites.put("ECDSA", ecdsaCipherSuites);

			logger.info("This testcase is executed for each a DSA, RSA and ECDSA certifcate," 
			+ " if the client supports the certificate Type.");

			int iterationCount = 1;
			int maxIterationCount = 0;

			if (!dsaCipherSuites.isEmpty()) {
				maxIterationCount++;
			}
			if (!rsaCipherSuites.isEmpty()) {
				maxIterationCount++;
			}
			if (!ecdsaCipherSuites.isEmpty()) {
				maxIterationCount++;
			}

			/*Execute test for RSA, DSA and ECDSA certificate if client supports them*/
			for (HashMap.Entry<String, List<TlsCipherSuite>> certificateTypeCipherSuite : certificateTypeCipherSuites
					.entrySet()) {
				String certificateTyp = certificateTypeCipherSuite.getKey();
				List<TlsCipherSuite> cipherSuites = certificateTypeCipherSuite.getValue();

				logger.info("Test " + certificateTyp + " Certificate.");

				if (cipherSuites.isEmpty()) {
					logger.info("No " + certificateTyp
							+ " cipher suites found. Thus, the testcase is not executed for this certificate type.");
					continue;
				}

				logger.debug("Supported " + certificateTyp + " Cipher suites:");
				for (TlsCipherSuite cipherSuite : cipherSuites) {
					logger.debug(cipherSuite.name());
				}

				step(1, "Setting TLS version: " + tlsVersion.getName(), null);

				tfserverCertificate.executeSteps("2",
						"The server supplies a certificate chain [CERT_SHORT_KEY] with a key length not conform to the "
								+ "application requirements.",
						Arrays.asList(), testTool, tlsVersion, cipherSuites,
						TlsTestToolCertificateTypes.CERT_SHORT_KEY, new ManipulateForceCertificateUsage());
				
				tfServerHello.executeSteps("3",
						"The TLS server answers the DUT choosing a TLS version and a cipher suite that is "
								+ "contained in the ClientHello.",
						Arrays.asList(), testTool, tlsVersion, cipherSuites);

				tFDutClientNewConnection.executeSteps("4",
						"The TLS server receives a ClientHello handshake message from the DUT.",
						Arrays.asList(), testTool, new IterationCounter(iterationCount++, maxIterationCount),
						dutExecutor);

				tfAlertMessageCheck.executeSteps("5", "The DUT does not accept the certificate chain and sends a "
								+ "\"bad_certificate\" alert or another suitable error description.",
						Arrays.asList("level=warning/fatal"), testTool);

				tfApplicationCheck.executeSteps("5", "", Arrays.asList(), testTool, dutExecutor);

				tfHandshakeNotSuccessfulCheck.executeSteps("6", "No TLS channel is established", null, testTool, tlsVersion);

				tfLocalServerClose.executeSteps("7", "Server closed successfully", Arrays.asList(),
						testTool);

				dutExecutor.resetProperties();
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
		dutExecutor.cleanAndExit();
	}
}
