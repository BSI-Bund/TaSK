package com.achelos.task.tr03116ts.testcases.a.a1.gp;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.utilities.logging.IterationCounter;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;


/**
 * Test case TLS_A1_GP_03_T - Supported groups match the ICS.
 * <p>
 * This test verifies that the offered Supported Groups extension matches the declaration in the ICS. Furthermore, a TLS
 * connection is possible.
 * <p>
 * The test MUST be repeated for all TLS versions [TLS_VERSION] and supported domain parameters [GROUP].
 */
public class TLS_A1_GP_03_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A1_GP_03_T";
	private static final String TEST_CASE_DESCRIPTION = "Supported groups match the ICS";
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
		int maxIterationCount
				= calculateMaxIterationCount(tlsVersions);

		for (TlsVersion tlsVersion : tlsVersions) {
			var eccCipherSuite = configuration.getSingleSupportedECCCipherSuite(tlsVersion);
			var dheCipherSuite = configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);
			var cipherSuiteTls13 = configuration.getSingleSupportedCipherSuite(tlsVersion);
			if(tlsVersion == TlsVersion.TLS_V1_2){
				if (null == eccCipherSuite && null == dheCipherSuite) {
					logger.error("No supported ECDH or DH cipher suite found");
					return;
				}
			} else /*TLS 1.3*/{
				if (null == cipherSuiteTls13) {
					logger.error("No supported cipher suite found");
					return;
				}
			}
			var namedGroups = configuration.getSupportedGroups(tlsVersion);

			if (namedGroups.isEmpty()) {
				logger.error("No supported groups found");
				return;
			}

			for(var namedGroup: namedGroups){
				TlsCipherSuite cipherSuite = null;
				if(tlsVersion == TlsVersion.TLS_V1_2){
					if(namedGroup.isFFDHEGroup()){
						cipherSuite = dheCipherSuite;
					}
					else /*Elliptic Curve Group*/{
						cipherSuite = eccCipherSuite;
					}
				} else /*TLS 1.3*/ {
					cipherSuite = cipherSuiteTls13;
				}
				executeHandshake(tlsVersion, cipherSuite, namedGroup, namedGroups, iterationCount++, maxIterationCount);
			}
		}

	}

	public void executeHandshake(TlsVersion tlsVersion, TlsCipherSuite cipherSuite, TlsNamedCurves namedGroup, List<TlsNamedCurves> namedGroups,  int iterationCount, int maxIterationCount) throws Exception {
		logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
		step(1, "Setting TLS version: " + tlsVersion.getName() + " and Supported Group: "
				+ namedGroup + " and CipherSuite: " + cipherSuite.name(), null);

		tfserverCertificate.executeSteps("2",
				"The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite,
				TlsTestToolCertificateTypes.CERT_DEFAULT);

		tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
				Arrays.asList(),
				testTool, tlsVersion, TlsTestToolTlsLibrary.MBED_TLS, cipherSuite, namedGroup);

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.",
				Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
				dutExecutor);

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

		tfLocalServerClose.executeSteps("10", "Server closed successfully", Arrays.asList(),
				testTool);
		dutExecutor.resetProperties();
		testTool.resetProperties();

	}

	public int calculateMaxIterationCount(List<TlsVersion> tlsVersionList){
		int maxIterationCount=0;
		for(var tlsVersion: tlsVersionList){
			if(tlsVersion == TlsVersion.TLS_V1_2){
				if(!configuration.getSupportedECCCipherSuites(tlsVersion).isEmpty()){
					maxIterationCount += configuration.getSupportedGroups(tlsVersion).
						stream().filter(TlsNamedCurves::isECCGroup).toList().size();
				} else if (!configuration.getSupportedFFDHECipherSuites(tlsVersion).isEmpty()) {
					maxIterationCount += configuration.getSupportedGroups(tlsVersion).
							stream().filter(TlsNamedCurves::isFFDHEGroup).toList().size();				}
			} else /*TLS 1.3*/ {
				maxIterationCount +=configuration.getSupportedGroups(tlsVersion).size();
			}
		}
		return maxIterationCount;
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
