package com.achelos.task.tr03116ts.testcases.a.a1.gp;


import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_GP_03_T - Supported Groups match the ICS
 * <p>
 * This test verifies that the offered Supported Groups extension matches the declaration in the ICS. Furthermore, a TLS
 * connection is possible.
 * <p>
 * The test MUST be repeated for all TLS versions [TLS_VERSION] and supported domain parameters [GROUP].
 */
public class TLS_A1_GP_03_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_GP_03_T";
	private static final String TEST_CASE_DESCRIPTION = "Supported Groups match the ICS";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that the offered Supported Groups extension matches the declaration in the ICS. "
					+ "Furthermore, a TLS connection is possible.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tftlsServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFTLSHighestVersionSupportCheck fFTLSHighestVersionSupportCheck;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;


	public TLS_A1_GP_03_T() {
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
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester causes the DUT to connect to the TLS server on [URL].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS server receives a ClientHello handshake message from the DUT.
	 * <li>The TLS ClientHello offers the highest TLS version stated in the ICS.
	 * <li>The NamedGroup extension contains the domain parameters stated in the ICS in specified order.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS server answers the DUT choosing a valid TLS version, a cipher suite supported by the ICS and domain
	 * parameters [GROUP].
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

		int iterationCount = 1;
		for (TlsVersion tlsVersion : tlsVersions) {
			var eccCipherSuite = configuration.getSingleSupportedECCCipherSuite(tlsVersion);
			var dheCipherSuite = configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);

			if (null == eccCipherSuite && null == dheCipherSuite) {
				logger.error("No supported ECDH or DH cipher suite found");
				return;
			}

			int maxIterationCount
					= configuration.getSupportedGroups(tlsVersion).size();

			if (null != eccCipherSuite) {
				var namedGroups = configuration.getSupportedGroups(tlsVersion);

				for (TlsNamedCurves curve : namedGroups) {
					boolean res = curve.isFFDHEGroup();
				}

				for (TlsNamedCurves namedGroup : namedGroups) {
					if (!namedGroup.isFFDHEGroup()) {
						logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
						step(1, "Setting TLS version: " + tlsVersion.getName() + " and Supported Group: "
								+ namedGroup + " and ECC CipherSuite: " + eccCipherSuite.name(), null);
						
						tfserverCertificate.executeSteps("2",
								"The TLS server supplies the certificate chain [CERT_DEFAULT].",
								Arrays.asList(), testTool, tlsVersion, eccCipherSuite,
								TlsTestToolCertificateTypes.CERT_DEFAULT);

						tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
								Arrays.asList(),
								testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, eccCipherSuite, namedGroup);

						tFDutClientNewConnection.executeSteps("4",
								"The TLS server receives a ClientHello handshake message from the DUT.",
								Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
								dutExecutor);
						iterationCount++;

						fFTLSHighestVersionSupportCheck.executeSteps("5",
								"The TLS ClientHello offers the highest TLS version stated in the ICS.", null,
								testTool);

						step(6, "Check if the NamedGroup extension contains the domain parameters stated in "
								+ "the ICS in specified order.",
								"The NamedGroup extension contains the domain parameters "
										+ "stated in the ICS in specified order.");
						testTool.checkDomainParameters(namedGroups);

						step(7, "Check if the TLS protocol is executed without errors and the channel is established.",
								"The TLS protocol is executed without errors and the channel is established.");
						testTool.assertMessageLogged(TestToolResource.Handshake_successful);

						tfApplicationCheck.executeSteps("8", "", Arrays.asList(), testTool, dutExecutor);

						tfLocalServerClose.executeSteps("9", "Server closed successfully", Arrays.asList(),
								testTool);
						dutExecutor.resetProperties();
						testTool.resetProperties();
					}
				}
			}

			if (null != dheCipherSuite) {

				var namedGroups = configuration.getSupportedGroups(tlsVersion);

				for (TlsNamedCurves namedGroup : namedGroups) {
					if (namedGroup.isFFDHEGroup()) {
						logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
						step(1, "Setting TLS version: " + tlsVersion.getName() + " and Supported Group: "
								+ namedGroup + " and DHE CipherSuite: " + eccCipherSuite.name(), null);

						tfserverCertificate.executeSteps("2",
								"The TLS server supplies the certificate chain [CERT_DEFAULT].",
								Arrays.asList(), testTool, tlsVersion, dheCipherSuite,
								TlsTestToolCertificateTypes.CERT_DEFAULT);
						
						tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
								Arrays.asList(),
								testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, dheCipherSuite, namedGroup);

						tFDutClientNewConnection.executeSteps("4",
								"The TLS server receives a ClientHello handshake message from the DUT.",
								Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
								dutExecutor);
						iterationCount++;

						step(5, "Check if the highest TLS version is offered by Client.",
								"The TLS ClientHello offers the highest TLS version stated in the ICS.");

						fFTLSHighestVersionSupportCheck.executeSteps("6",
								"The TLS ClientHello offers the highest TLS version stated in the ICS.", null,
								testTool);

						step(7, "Check if the NamedGroup extension contains the domain parameters stated in "
								+ "the ICS in specified order.",
								"The NamedGroup extension contains the domain parameters "
										+ "stated in the ICS in specified order.");
						testTool.checkDomainParameters(namedGroups);

						step(8, "Check if the TLS protocol is executed without errors and the channel is established.",
								"The TLS protocol is executed without errors and the channel is established.");
						testTool.assertMessageLogged(TestToolResource.Handshake_successful);

						tfApplicationCheck.executeSteps("9", "", Arrays.asList(), testTool, dutExecutor);

						tfApplicationCheck.executeSteps("10", "", Arrays.asList(), testTool, dutExecutor);

						tfLocalServerClose.executeSteps("11", "Server closed successfully", Arrays.asList(),
								testTool);
						dutExecutor.resetProperties();
						testTool.resetProperties();
					}
				}
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
