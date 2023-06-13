package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsDHGroup;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B2_GP_06 - Prime of sufficient length.
 * <p>
 * This positive test verifies that the DUT offers a FFDHE group with a prime of a sufficient length.
 */
public class TLS_B2_GP_06 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_06";
	private static final String TEST_CASE_DESCRIPTION = "Prime of sufficient length";
	private static final String TEST_CASE_PURPOSE = "This positive test verifies that the DUT offers a FFDHE group "
			+ "with a prime of a sufficient length.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_GP_06() {

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
	 * <li>The TLS ClientHello offers the highest TLS version supported according to the ICS.
	 * <li>The TLS ClientHello offers a cipher suite that uses FFDHE and is supported according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * <li>The ServerDHParams provided with the KeyExchange of the DUT contain a prime modulus of a sufficient length.
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

		// TBD: This test case is not applicable for the TLS Server Profile and will be covered in a later milestone

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		// all unsupported tls version
		var tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (null == tlsVersion) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		List<TlsCipherSuite> ffdheCipherSuites = null;
		if(tlsVersion == TlsVersion.TLS_V1_3){
			ffdheCipherSuites = configuration.getSupportedCipherSuites(tlsVersion);
		}else{
			ffdheCipherSuites = configuration.getSupportedFFDHECipherSuites(tlsVersion);
		}
		if (null == ffdheCipherSuites || ffdheCipherSuites.isEmpty()) {
			logger.error("No supported DHE cipher suites found.");
			return;
		}
		logger.debug("Supported DHE Cipher suites:");
		for (TlsCipherSuite cipherSuite : ffdheCipherSuites) {
			logger.debug(cipherSuite.getName());
		}

		int iterationCount = 1;
		//Does a loop here make really sense?
		for (TlsCipherSuite cipherSuite : ffdheCipherSuites) {

			tfClientCertificate.executeSteps("1",
					"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(),
					testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT);

			tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
					+ ", cipher suite " + cipherSuite.getName() + " .", null, testTool, tlsVersion, cipherSuite);

			testTool.start(iterationCount++,  ffdheCipherSuites.size());

			/* open connection */
			tFTCPIPNewConnection.executeSteps("3",
					"The TLS ClientHello offers the TLS version " + tlsVersion.getName()
							+ ", cipher suite " + cipherSuite.getName() + " .",
					Arrays.asList(), testTool);

			step(4, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
			testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
			testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);

			step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
					"The TLS protocol is executed without errors and the channel is established.");
			testTool.assertMessageLogged(TestToolResource.Handshake_successful);

			if(tlsVersion == TlsVersion.TLS_V1_2) {
				final String dhPHexString = testTool.getValue(TestToolResource.ServerKeyExchange_params_dh_p);
				if (null != dhPHexString) {
					String dhP = dhPHexString.replaceAll(" ", "");
					var dheKeyLengths = configuration.getSufficientDHEKeyLengths(tlsVersion);
					OptionalInt min = dheKeyLengths.stream().mapToInt(TlsDHGroup::getKeyLength).min();
					if (min.isEmpty()) {
						logger.error("No sufficient key length found");
					}
					if (dhP.length() / 2 < min.getAsInt() / 8) {
						logger.error("dhP does not have sufficient length");
					}
				}
			}

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
