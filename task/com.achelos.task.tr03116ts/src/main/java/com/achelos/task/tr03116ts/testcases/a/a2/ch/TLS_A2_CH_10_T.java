package com.achelos.task.tr03116ts.testcases.a.a2.ch;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.RequestClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFApplicationSpecificInspectionCheck;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSHighestVersionSupportCheck;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;
import com.achelos.task.utilities.logging.IterationCounter;


/**
 * Test case TLS_A2_CH_10_T - signature_algorithms_cert extension.
 * 
 * <p>
 * This test verifies that the offered "signature_algorithms_cert" extension matches the declaration in the ICS.
 * Furthermore, a TLS connection is possible. The test uses the signature algorithm and hash function
 * [SIG_ALGORITHM_CERT].
 * <p>
 * The test MUST be repeated for supported signature algorithms [SIG_ALGORITHM_CERT].
 * 
 */
public class TLS_A2_CH_10_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_CH_10_T";
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
	private final TFTLSHighestVersionSupportCheck fFTLSHighestVersionSupportCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;

	public TLS_A2_CH_10_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tftlsServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		fFTLSHighestVersionSupportCheck = new TFTLSHighestVersionSupportCheck(this);
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
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The TLS ClientHello offers the ""signature_algorithms_cert"" extension containing the values stated in the ICS.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 *
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a cipher suite supported by the ICS.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>A certificate chain [CERT_DEFAULT] with certificates that are signed using [SIG_ALGORITHM_CERT] is supplied.
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The client supplies a valid client certificate and a correct CertificateVerify message.
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

		var sigAlgorithms = configuration.getSupportedSignatureAlgorithmsForCertificates();

		if (null == sigAlgorithms || sigAlgorithms.isEmpty()) {
			logger.error("No supported signature schemes found.");
			return;
		}
		logger.debug("Supported signature schemes");
		for (TlsSignatureAlgorithmWithHashTls13 sigAlg : sigAlgorithms) {
			logger.debug(sigAlg.toString());
		}

		step(1, "Setting TLS version: " + tlsVersion.getName() , null);

		tfserverCertificate.executeSteps("2", "A certificate chain [CERT_DEFAULT] with certificates that are signed"
				+ " using [SIG_ALGORITHM_CERT] is supplied.",
				Arrays.asList(), testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);

		tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
				Arrays.asList(),
				testTool, tlsVersion, TlsTestToolTlsLibrary.OpenSSL, new RequestClientCertificate());
		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(),
				testTool,
				dutExecutor);

		fFTLSHighestVersionSupportCheck.executeSteps("5",
				"The TLS ClientHello offers the highest TLS version stated in the ICS.", null,
				testTool);


		step(5, "Check if The TLS ClientHello offers the \"signature_algorithms_cert\" extension "
				+ "containing the values stated in the ICS.",
				"The TLS ClientHello offers the \"signature_algorithms_cert\" extension "
						+ "containing the values stated in the ICS.");

		List <TlsSignatureAlgorithmWithHashTls13> tlsSignatureSchemes = null;

		final byte[] data = testTool.assertExtensionTypeLogged(TlsTestToolMode.client,
				TlsExtensionTypes.signature_algorithms_cert);
		if (data == null) {
			logger.error("The TLS ClientHello does not offer the \"signature_algorithms_cert\" extension.");
			return;
		}

		try {
			tlsSignatureSchemes
					= (List<TlsSignatureAlgorithmWithHashTls13>)(List<?>)TlsSignatureAlgorithmWithHash.parseSignatureAlgorithmWithHashByteList(data, TlsVersion.TLS_V1_3);
		} catch (Exception e) {
			// Unknown_signature algorithm.
			logger.error("Found unknown signature algorithm" + e);
//				continue;
		}

		// Find difference in sigAlgorithms with respect to supportedSignatureAndHashAlgorithms.
		var difference = getDifference(sigAlgorithms, tlsSignatureSchemes);

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
		tfLocalServerClose.executeSteps("6", "Server closed successfully", Arrays.asList(),
				testTool);

		step(7, "Check if the client supplies a valid client certificate and a correct CertificateVerify message.",
				"The client supplies a valid client certificate and a correct CertificateVerify message.");
		testTool.assertMessageLogged(TestToolResource.Certificate_received_valid.getInternalToolOutputMessage());
		testTool.assertMessageLogged(TestToolResource.CertificateVerify_valid.getInternalToolOutputMessage());

		/* OpenSSL currently does not support the signature_algorithms_cert extension */

		/*step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

		tfApplicationCheck.executeSteps("7", "", Arrays.asList(), testTool, dutExecutor);

		tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
				testTool);*/

	}

	/**
	 * Method finds the difference in list1 with respect to list2 and returns new list containing elements that do not
	 * exist in the list2.
	 *
	 * @param  list1.
	 * @param  list2.
	 * @return new list containing elements that are not presents in the list2.
	 */
	private List<TlsSignatureAlgorithmWithHashTls13> getDifference(final List<TlsSignatureAlgorithmWithHashTls13> list1,
																   final List<TlsSignatureAlgorithmWithHashTls13> list2) {

		List<TlsSignatureAlgorithmWithHashTls13> missingSigAlgs = new LinkedList<>();

		for(TlsSignatureAlgorithmWithHashTls13 sigAlg: list1){
			boolean found = false;
			for(TlsSignatureAlgorithmWithHashTls13 expectedSigAlg: list2){
				if(sigAlg.getSignatureScheme() == expectedSigAlg.getSignatureScheme()){
					found = true;
				}
			}
			if(!found){
				missingSigAlgs.add(sigAlg);
			}
		}
		return missingSigAlgs;

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
