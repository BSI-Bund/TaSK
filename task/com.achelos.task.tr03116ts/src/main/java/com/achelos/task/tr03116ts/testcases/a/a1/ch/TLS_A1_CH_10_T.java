package com.achelos.task.tr03116ts.testcases.a.a1.ch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Testcase TLS_A1_CH_10_T - signature_algorithms_cert extension
 * 
 * <p>
 * This test verifies that the offered "signature_algorithms_cert" extension matches the declaration in the ICS.
 * Furthermore, a TLS connection is possible. The test uses the signature algorithm and hash function
 * [SIG_ALGORITHM_CERT].
 * <p>
 * The test MUST be repeated for supported signature algorithms [SIG_ALGORITHM_CERT].
 *
 */
public class TLS_A1_CH_10_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_CH_10_T";
	private static final String TEST_CASE_DESCRIPTION = "signature_algorithms_cert extension";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the offered \"signature_algorithms_cert\" extension matches the "
					+ "declaration in the ICS. Furthermore, a TLS connection is possible. The test uses the "
					+ "signature algorithm and hash function [SIG_ALGORITHM_CERT].";
	
	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tftlsServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;

	public TLS_A1_CH_10_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);
		
		tftlsServerHello = new TFTLSServerHello(this);
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
	 * <li>The TLS ClientHello offers the ""signature_algorithms_cert"" extension containing the values stated in the
	 * ICS.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a cipher suite supported by the ICS.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>A certificate chain [CERT_DEFAULT] with certificates that are signed using [SIG_ALGORITHM_CERT] is supplied.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
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

		int iterationCount = 1;

		var sigAlgorithms = configuration.getSupportedSignatureAlgorithmsForCertificates();

		if (null == sigAlgorithms || sigAlgorithms.isEmpty()) {
			logger.error("No supported signature schemes found.");
			return;
		}
		logger.debug("Supported signature schemes");
		for (TlsSignatureScheme sigAlg : sigAlgorithms) {
			logger.debug(sigAlg.toString());
		}

		for (TlsSignatureScheme sigAlg : sigAlgorithms) {
			logger.info("Start iteration " + iterationCount + " of " + sigAlgorithms.size() + ".");
			step(1, "Setting TLS version: " + tlsVersion.getName() + " and signature algorithm: "
					+ sigAlg.toString(), null);

			tfserverCertificate.executeSteps("2", "A certificate chain [CERT_DEFAULT] with certificates that are signed"
					+ " using [SIG_ALGORITHM_CERT] is supplied.",
					Arrays.asList(), testTool, tlsVersion, sigAlg, TlsTestToolCertificateTypes.CERT_DEFAULT);
			
			tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
					Arrays.asList(),
					testTool, tlsVersion, TlsTestToolTlsLibrary.OpenSSL, sigAlg);
			tFDutClientNewConnection.executeSteps("4",
					"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(),
					testTool, new IterationCounter(iterationCount, sigAlgorithms.size()),
					dutExecutor);
			iterationCount++;


			step(5, "Check if The TLS ClientHello offers the \"signature_algorithms_cert\" extension "
					+ "containing the values stated in the ICS.",
					"The TLS ClientHello offers the \"signature_algorithms_cert\" extension "
							+ "containing the values stated in the ICS.");

			ArrayList<TlsSignatureScheme> tlsSignatureSchemes = null;

			final byte[] data = testTool.assertExtensionTypeLogged(TlsTestToolMode.client,
					TlsExtensionTypes.signature_algorithms_cert);
			if (data == null) {
				logger.error("The TLS ClientHello does not offer the \"signature_algorithms_cert\" extension.");
//				continue;
			}
			
			try {
				tlsSignatureSchemes
						= TlsSignatureScheme.parsetlsSignatureSchemeWithHashByteList(data);
			} catch (Exception e) {
				// Unknown_signature algorithm.
				logger.error("Found unknown signature algorithm" + e);
//				continue;
			}

			// Find difference in sigAlgorithms with respect to supportedSignatureAndHashAlgorithms.
			List<TlsSignatureScheme> difference
					= getDifference(sigAlgorithms, tlsSignatureSchemes);

			logger.info("Expected signature algorithms: " + sigAlgorithms);
			logger.info("Actual signature algorithms: " + tlsSignatureSchemes);
			if (!difference.isEmpty()) {
				logger.error(
						"The TLS ClientHello does not offer following signature algorithm(s): "
								+ difference);
			} else {
				difference = getDifference(tlsSignatureSchemes, sigAlgorithms);
				if (!difference.isEmpty()) {
					logger.error(
							"The TLS ClientHello additionally offers following signature algorithm(s): "
									+ difference);
				} else {
					logger.info("The TLS ClientHello offers the signature_algorithms_cert"
							+ " containing the values stated in the ICS.");
				}
			}

			
			step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
					"The TLS protocol is executed without errors and the channel is established.");
			testTool.assertMessageLogged(TestToolResource.Handshake_successful);

			tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

			tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
					testTool);

			dutExecutor.resetProperties();
			testTool.resetProperties();
		}

	}
	
	/**
	 * Method finds the difference in list1 with respect to list2 and returns new list containing elements that do not
	 * exist in the list2.
	 *
	 * @param sigAlgorithms list1.
	 * @param list2 list2.
	 * @return new list containing elements that are not presents in the list2.
	 */
	private List<TlsSignatureScheme> getDifference(final List<TlsSignatureScheme> sigAlgorithms,
			final List<TlsSignatureScheme> list2) {
		List<TlsSignatureScheme> differences = new ArrayList<>(sigAlgorithms);
		differences.removeAll(list2);
		return differences;
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
