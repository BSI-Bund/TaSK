package com.achelos.task.tr03116ts.testcases.a.a1.ch;


import java.util.Arrays;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.certificatehelper.ManipulateForceCertificateUsage;
import com.achelos.task.commons.enums.TlsAlertLevel;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.tr03116ts.testfragments.TFAlertMessageCheck;
import com.achelos.task.tr03116ts.testfragments.TFDUTClientNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFLocalServerClose;
import com.achelos.task.tr03116ts.testfragments.TFServerCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Testcase TLS_A1_CH_03 - Host certificate with wrong domain name
 * 
 * <p>
 * The test case verifies the correct behaviour of the DUT in case the server sends a certificate that does not match
 * the domain the DUT wanted to connect to. The SubjectAltName of type dNSName in the host certificate does not match
 * with the server's host name.
 *
 */
public class TLS_A1_CH_03 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_CH_03";
	private static final String TEST_CASE_DESCRIPTION = "Host certificate with wrong domain name";
	private static final String TEST_CASE_PURPOSE
			= "The test case verifies the correct behaviour of the DUT in case the server sends a certificate "
					+ "that does not match the domain the DUT wanted to connect to. The SubjectAltName of type dNSName "
					+ "in the host certificate does not match with the server's host name.";
	
	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFAlertMessageCheck tfAlertMessageCheck;

	public TLS_A1_CH_03() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);
		
		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
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
	 * <li>The server supplies the certificate chain [CERT_INVALID_DOMAIN_NAME].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the certificate chain and sends a ""certificate_unknown"" alert or another suitable
	 * error description.
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

		/** one supported cipher suite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error("No supported cipher suite is found.");
			return;
		}
		logger.debug("Supported CipherSuite: " + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and Cipher suite: "
				+ cipherSuite.getName(), null);
		
		tfserverCertificate.executeSteps("2", "The server supplies the certificate chain [CERT_INVALID_DOMAIN_NAME].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite,
				TlsTestToolCertificateTypes.CERT_INVALID_DOMAIN_NAME_SAN, new ManipulateForceCertificateUsage());

		tfServerHello.executeSteps("3",
				"The TLS server answers the DUT choosing a TLS version and a cipher suite that is "
						+ "contained in the ClientHello.",
				Arrays.asList(), testTool, tlsVersion, cipherSuite);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);
		
		tfAlertMessageCheck.executeSteps("5", "The DUT does not accept the certificate chain and sends a "
				+ "\"certificate_unknown\" alert or another suitable error description.",
				Arrays.asList("level=warning/fatal"), testTool);

		tfLocalServerClose.executeSteps("6", "Server closed successfully", Arrays.asList(),
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
