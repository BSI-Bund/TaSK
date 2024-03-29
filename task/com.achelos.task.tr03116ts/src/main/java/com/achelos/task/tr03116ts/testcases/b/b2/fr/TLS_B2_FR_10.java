package com.achelos.task.tr03116ts.testcases.b.b2.fr;

import java.util.Arrays;
import java.util.Collections;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtExtendedMasterSecret;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B2_FR_10 - Extended-Master-Secret extension.
 * <p>
 * This test verifies that the Extended-Master-Secret extension is selected and can be used in a connection when it is
 * offered by the client.
 */
public class TLS_B2_FR_10 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_FR_10";
	private static final String TEST_CASE_DESCRIPTION = "Extended-Master-Secret extension";
	private static final String TEST_CASE_PURPOSE = 
			"This test verifies that the Extended-Master-Secret extension is selected and "
			+ "can be used in a connection when it is offered by the client.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_FR_10() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfClientHello = new TFTLSClientHello(this);
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
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
	 * <li>The TLS ClientHello offers a cipher suite that is supported according to the ICS.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello contains the Extended-Master-Secret extension.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * <li>The TLS server selects the Extended-Master-Secret extension.
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

		/** any supported algorithm cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error(MessageConstants.NO_SUPPORTED_CIPHER_SUITE);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_CIPHER_SUITE + cipherSuite.getName());

		step(1, "Send ClientHello message with extensions containing the extended_master_secret extension.",
				"Receive ServerHello message from TOE.\r\n"
						+ "ServerHello.extensions contains the extended_master_secret extension.");
		
		tfClientCertificate.executeSteps("2",
				"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].", Arrays.asList(), testTool,
				tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT);

		tfClientHello.executeSteps("3", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ ", cipher suite " + cipherSuite.name() + " .", null, testTool, tlsVersion,
				cipherSuite,new TlsExtExtendedMasterSecret(), TlsTestToolTlsLibrary.OpenSSL);

		testTool.start();

		tFTCPIPNewConnection.executeSteps("4", "", Collections.emptyList(),
				testTool);
		
		step(5, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
		testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
		testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);
		
		step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		
		step(6, "Check if the TLS server selects the Extended-Master-Secret extension.",
				"The TLS server selects the Extended-Master-Secret extension.");
		testTool.assertServerSupportsExtension(TlsExtensionTypes.extended_master_secret);
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
