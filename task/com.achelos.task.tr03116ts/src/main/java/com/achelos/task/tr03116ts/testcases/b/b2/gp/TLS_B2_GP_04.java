package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFHandshakeNotSuccessfulCheck;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B2_GP_04 - Reject unsupported ECC curves.
 * <p>
 * This test ensures that the connection is not established if the client offers only elliptic curve cipher suites and
 * unsupported curves according to the ICS.
 */
public class TLS_B2_GP_04 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_04";
	private static final String TEST_CASE_DESCRIPTION = "Reject unsupported ECC curves";
	private static final String TEST_CASE_PURPOSE
			= "This test ensures that the connection is not established if the client "
					+ "offers only elliptic curve cipher suites and unsupported curves according to the ICS.";


	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSClientHello tfClientHello;
	private final TFClientCertificate tfClientCertificate;
	private final TFAlertMessageCheck tFAlertMessageCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;


	public TLS_B2_GP_04() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tfClientHello = new TFTLSClientHello(this);
		tfClientCertificate = new TFClientCertificate(this);
		tFAlertMessageCheck = new TFAlertMessageCheck(this);
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
	 * <li>The TLS ClientHello offers only an elliptic curve-based cipher suite that is supported according to the ICS.
	 * <li>The TLS ClientHello offers only elliptic curves which are not supported according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT rejects the ClientHello and sends a ""handshake failure"" alert or another suitable error
	 * description.
	 * <li>No TLS channel is established.
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

		/** one supported ECC algorithm cipher suite */
		TlsCipherSuite eccCipherSuite = null;
		if(tlsVersion == TlsVersion.TLS_V1_2) {
			eccCipherSuite = configuration.getSingleSupportedECCCipherSuite(tlsVersion);
		}
		if(tlsVersion == TlsVersion.TLS_V1_3){
			eccCipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		}
		if (eccCipherSuite == null) {
			logger.error("No supported ECC cipher suite found.");
			return;
		}
		logger.debug("Supported Elliptic Curve CipherSuite: " + eccCipherSuite.getName());

		/** not supported elliptic curves */
		var notSupportedEllipticCurves
				= configuration.getNotSupportedEllipticCurves(tlsVersion);
		if (null == notSupportedEllipticCurves || notSupportedEllipticCurves.isEmpty()) {
			logger.error("No unsupported Elliptic Curves found.");
			return;
		}
		logger.debug("Unsupported Elliptic Curves:");
		for (TlsNamedCurves group : notSupportedEllipticCurves) {
			logger.debug(group.name());
		}

		step(1, "The TLS ClientHello offers only elliptic curves which are not supported"
				+ " according to the ICS.", "");
		
		tfClientCertificate.executeSteps("2",
				"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(), testTool,
				tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT);

		// configure client hello
		tfClientHello.executeSteps("3", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + eccCipherSuite.getName() + " .", null, testTool, tlsVersion, eccCipherSuite,
				notSupportedEllipticCurves);
		testTool.start();

		// connect
		tFTCPIPNewConnection.executeSteps("4", "", Arrays.asList(), testTool);

		// check for handshake_failure
		tFAlertMessageCheck.executeSteps("5", "The DUT rejects the ClientHello and sends a \"handshake failure\" alert "
				+ "or another suitable error description",
				Arrays.asList("level=warning/fatal", "description=handshake_failure"), testTool, TlsAlertDescription.handshake_failure);
		tfHandshakeNotSuccessfulCheck.executeSteps("6", "No TLS channel is established", null, testTool, tlsVersion);
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
