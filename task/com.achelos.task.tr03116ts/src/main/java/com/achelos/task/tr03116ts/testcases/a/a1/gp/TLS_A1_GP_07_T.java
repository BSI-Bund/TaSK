package com.achelos.task.tr03116ts.testcases.a.a1.gp;


import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Testcase TLS_A1_GP_07_T - Ephemeral domain parameters
 * <p>
 * This test verifies that the DUT supports ephemeral domain parameters of sufficient length.
 * <p>
 * Depending on the client´s capabilities, the test MUST be repeated for DHE ephemeral domain parameters of sufficient
 * length from Table 20.
 */
public class TLS_A1_GP_07_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_GP_07_T";
	private static final String TEST_CASE_DESCRIPTION = "Ephemeral domain parameters";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the DUT supports ephemeral domain parameters of sufficient length.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFTLSServerHello tftlsServerHello;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFServerCertificate tfserverCertificate;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;

	public TLS_A1_GP_07_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfLocalServerClose = new TFLocalServerClose(this);
		tftlsServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
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
	 * <li>The TLS server answers the DUT choosing a TLS version and a FFDHE cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The TLS server sends an ephemeral key based on FFDHE domain parameters of sufficient length.
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

		// all supported tls version
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

			var dheCipherSuite = configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);
			var sufficientLengthDHEGroups
					= configuration.filterSupportedGroupsToFFDHEGroups(tlsVersion);

			if (null == dheCipherSuite) {
				logger.error("No supported DHE cipher suite found");
				return;
			}

			int iterationCount = 1;

			int maxIterationCount = 0;
			if (sufficientLengthDHEGroups.size() != 0) {
				maxIterationCount += sufficientLengthDHEGroups.size();
			}

			for (var sufficientDhGroup : sufficientLengthDHEGroups) {
				logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
				step(1, "Setting TLS version: " + tlsVersion.getName() + " DHE CipherSuite: "
						+ dheCipherSuite.name()
						+ " DHE Group: " + sufficientDhGroup.name(), null);

				tfserverCertificate.executeSteps("2",
						"The TLS server supplies the certificate chain [CERT_DEFAULT].",
						Arrays.asList(), testTool, tlsVersion, dheCipherSuite,
						TlsTestToolCertificateTypes.CERT_DEFAULT);
				
				tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
						Arrays.asList(),
						testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, dheCipherSuite, sufficientDhGroup);

				tFDutClientNewConnection.executeSteps("4",
						"The TLS server receives a ClientHello handshake message from the DUT.",
						Arrays.asList(), testTool,
						new IterationCounter(iterationCount, maxIterationCount), dutExecutor);

				iterationCount++;
				step(5, "Check if the TLS protocol is executed without errors and the channel is established.",
						"The TLS protocol is executed without errors and the channel is established.");
				testTool.assertMessageLogged(TestToolResource.Handshake_successful);

				tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);

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
