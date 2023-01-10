package com.achelos.task.tr03116ts.testcases.a.a1.fr;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
import com.achelos.task.tr03116ts.testfragments.TFTLSHighestVersionSupportCheck;
import com.achelos.task.tr03116ts.testfragments.TFTLSServerHello;


/**
 * Testcase TLS_A1_FR_13 - random value for gmt_unix_time
 * <p>
 * Positive test verifying the value for gmt_unix_time in ClientHello to be random.
 */
public class TLS_A1_FR_13 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_FR_13";
	private static final String TEST_CASE_DESCRIPTION = "random value for gmt_unix_time";
	private static final String TEST_CASE_PURPOSE
			= "Positive test verifying the value for gmt_unix_time in ClientHello to be random.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFTLSHighestVersionSupportCheck fFTLSHighestVersionSupportCheck;

	public TLS_A1_FR_13() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		fFTLSHighestVersionSupportCheck = new TFTLSHighestVersionSupportCheck(this);
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
	 * <li>The TLS ClientHello offers the highest TLS version stated in the ICS.
	 * <li>The ClientHello message includes a random structure ""Random"" consisting of ""gmt_unix_time"" and
	 * ""random_bytes"".
	 * <li>The value for ""gmt_unix_time"" is set randomly and does not correlate to the current time and date in any
	 * way.
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

		/** highest supported TLS Version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error("No supported TLS Versions found.");
			return;
		}
		logger.debug("TLS Version: " + tlsVersion.name());

		/** one supported CipherSuite */
		TlsCipherSuite cipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
		if (cipherSuite == null) {
			logger.error("No supported Cipher Suite found.");
			return;
		}
		logger.debug("Supported CipherSuite: " + cipherSuite.name());

		step(1, "Setting TLS version: " + tlsVersion.getName() + " and Cipher suite: "
				+ cipherSuite, null);

		tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite, TlsTestToolCertificateTypes.CERT_DEFAULT);
		
		tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(), testTool,
				tlsVersion, cipherSuite);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.", Arrays.asList(), testTool,
				dutExecutor);

		fFTLSHighestVersionSupportCheck.executeSteps("5",
				"The TLS ClientHello offers the highest TLS version stated in the ICS.", null,
				testTool);

		step(6, "Check if the ClientHello message includes a random structure \"Random\" consisting of "
				+ "\"gmt_unix_time\" and \"random_bytes\".",
				"The ClientHello message includes a random structure \"Random\" consisting of "
						+ "\"gmt_unix_time\" and \"random_bytes\".");
		String clientHelloRandom = testTool.getValue(TestToolResource.ClientHello_random);
		Date clientHelloRandomTime = null;
		if (clientHelloRandom != null) {

			long timestamp = testTool.getGmtunixtimeInHelloRandom(clientHelloRandom);
			final int oneThousand = 1000;
			clientHelloRandomTime = new Date(timestamp * oneThousand);
			logger.info("The ClientHello message includes a random structure \"Random\" consisting of "
					+ "the following \"gmt_unix_time\": " + clientHelloRandomTime);

		} else {
			logger.error("The ClientHello message does not include a random structure \"Random\" in the ClientHello message.");
		}

		step(7, "Check if the value for \"gmt_unix_time\" is set randomly and does not correlate to "
				+ "the current time and date in any way.",
				"The value for \"gmt_unix_time\" is set randomly and does not correlate to the current time and date"
						+ " in any way.");

		
		if (clientHelloRandomTime != null) {
			
			Date currentTime = new Date(System.currentTimeMillis());
			
			logger.info("Received gmt_unix_time: " + clientHelloRandomTime.toString());
			logger.info("Current time: " + currentTime.toString());
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String receivedDate = formatter.format(clientHelloRandomTime);
			String currentDate = formatter.format(currentTime);

			if (currentDate.equals(receivedDate)) {
				logger.error("The value for \"gmt_unix_time\" is not set randomly and correlates to the "
						+ "current time and date in any way.");
			} else {
				logger.info("The value for \"gmt_unix_time\" is set randomly and does not correlate to the "
						+ "current time and date in any way.");
			}
		} else {
			logger.error("The value for \"gmt_unix_time\" is not set in the ClientHello.");
		}
		step(8, "Check if the TLS protocol is executed without errors and the channel is established.",
				"The TLS protocol is executed without errors and the channel is established.");
		testTool.assertMessageLogged(TestToolResource.Handshake_successful);

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
