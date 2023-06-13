package com.achelos.task.tr03116ts.testcases.a.a2.gp;


import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsAlertLevel;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;
import com.achelos.task.utilities.logging.IterationCounter;


/**
 * Test case TLS_A2_GP_06_T - Domain parameters with insufficient length.
 * <p>
 * This test verifies the behaviour of the DUT in case the server tries to use ephemeral domain parameters with
 * insufficient length.
 * <p>
 * Depending on the client´s capabilities, the test MUST be repeated for ECDHE and DHE ephemeral domain parameters of
 * insufficient length from Table 20.
 */
public class TLS_A2_GP_06_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_GP_06_T";
	private static final String TEST_CASE_DESCRIPTION = "Domain parameters with insufficient length";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies the behaviour of the DUT in case the server tries to use "
					+ "ephemeral domain parameters with insufficient length.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFTLSServerHello tftlsServerHello;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFServerCertificate tfserverCertificate;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;

	public TLS_A2_GP_06_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfLocalServerClose = new TFLocalServerClose(this);
		tftlsServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfAlertMessageCheck = new TFAlertMessageCheck(this);
		tFDutClientNewConnection = new TFDUTClientNewConnection(this);
		tfApplicationCheck = new TFApplicationSpecificInspectionCheck(this);
		tfHandshakeNotSuccessfulCheck = new TFHandshakeNotSuccessfulCheck(this);
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
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ol>
	 *
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server only offers a cipher suite based on FFDHE or ECDHE for key negotiation listed in the ICS of the DUT.
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>The TLS server sends an ephemeral key based on domain parameters of insufficient length.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The TLS client does not accept the ServerHello and sends a ""handshake_failure"" alert or another suitable error description.
	 * <li>No TLS connection is established.
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
		int maxIterationCount = calculateMaxIterationCount(tlsVersions);
		for (TlsVersion tlsVersion : tlsVersions) {

			var notSupportedEllipticCurves
					= configuration.getNotSupportedEllipticCurves(tlsVersion);

			TlsCipherSuite eccCipherSuite = null;
			TlsCipherSuite dheCipherSuite = null;
			if(tlsVersion == TlsVersion.TLS_V1_2){
				eccCipherSuite = configuration.getSingleSupportedECCCipherSuite(tlsVersion);
				dheCipherSuite = configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);
			}else /*TLS 1.3*/{
				eccCipherSuite = configuration.getSingleSupportedCipherSuite(tlsVersion);
				dheCipherSuite = eccCipherSuite;
			}

			configuration.getSingleSupportedFFDHECipherSuite(tlsVersion);
			var insufficientLengthDHEGroups
					= configuration.getInsufficientDHEKeyLengths(tlsVersion);

			var notSupportedFFDHEGroups = configuration.getNotSupportedDHEGroups(tlsVersion);

			if (null == eccCipherSuite && null == dheCipherSuite) {
				// If neither a ECDHE nor a DHE Cipher Suite are supported, PFS is not available.
				logger.error("No supported ECDHE or DHE cipher suite found. " +
						"If neither an ECDHE nor a DHE Cipher Suite are supported, PFS is not available.");
				continue;
			}

			if(tlsVersion == TlsVersion.TLS_V1_2){
				if (null != dheCipherSuite) {
					// Insufficient Length DH Groups test only for TLS 1.2
					logger.info("Start with insufficient DHE length groups");
					for (var insufficientDhGroup : insufficientLengthDHEGroups) {
						executeHandshake(tlsVersion, dheCipherSuite, insufficientDhGroup, iterationCount++, maxIterationCount);
					}
				}
			}
			if (null != eccCipherSuite) {
				logger.info("Start with not supported elliptic curve groups");
				for (var notSupportedECDHEGroup : notSupportedEllipticCurves) {
					executeHandshake(tlsVersion, eccCipherSuite,notSupportedECDHEGroup, iterationCount++, maxIterationCount);
				}
			}
			if (null != dheCipherSuite) {
				// Not Supported DHE Groups
				logger.info("Start with not supported FFDHE groups");
				for (var notSupportedFFDHEGroup : notSupportedFFDHEGroups) {
					executeHandshake(tlsVersion, dheCipherSuite,notSupportedFFDHEGroup, iterationCount++, maxIterationCount);
				}
			}
		}
	}

	public void executeHandshake(TlsVersion tlsVersion, TlsCipherSuite cipherSuite, Object namedGroup, int iterationCount, int maxIterationCount) throws Exception {
		logger.info("Start iteration " + iterationCount + " of " + maxIterationCount + ".");
		step(1, "Setting TLS version: " + tlsVersion.getName() + " DHE CipherSuite: "
				+ cipherSuite.name() + " DHE Group: " + namedGroup.toString(), null);

		tfserverCertificate.executeSteps("2",
				"The TLS server supplies the certificate chain [CERT_DEFAULT].",
				Arrays.asList(), testTool, tlsVersion, cipherSuite,
				TlsTestToolCertificateTypes.CERT_DEFAULT);

		tftlsServerHello.executeSteps("3", "Server started and waits for new client connection",
				Arrays.asList(),
				testTool, tlsVersion, cipherSuite, namedGroup, new RequestClientCertificate());

		tFDutClientNewConnection.executeSteps("4",
				"The TLS server receives a ClientHello handshake message from the DUT.",
				Arrays.asList(), testTool, new IterationCounter(iterationCount, maxIterationCount),
				dutExecutor);
		if(tlsVersion == TlsVersion.TLS_V1_2) {
			tfAlertMessageCheck.executeSteps("5", "The TLS client does not accept the ServerHello " +
							"and sends a \"handshake_failure\" alert or another suitable error description.",
					Arrays.asList("level=fatal","description=handshake_failure"), testTool, TlsAlertLevel.fatal,
					TlsAlertDescription.handshake_failure);
		}

		tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);

		tfHandshakeNotSuccessfulCheck.executeSteps("7", "No TLS channel is established", null, testTool, tlsVersion);

		tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
				testTool);
		dutExecutor.resetProperties();
		testTool.resetProperties();
	}

	public int calculateMaxIterationCount(List<TlsVersion> tlsVersionList){
		int maxIterationCount=0;
		for(var tlsVersion: tlsVersionList) {
			if (tlsVersion == TlsVersion.TLS_V1_2) {
				maxIterationCount += configuration.getInsufficientDHEKeyLengths(tlsVersion).size();
			}
			if (!configuration.getSupportedECCCipherSuites(tlsVersion).isEmpty() || tlsVersion == TlsVersion.TLS_V1_3) {
				maxIterationCount += configuration.getNotSupportedEllipticCurves(tlsVersion).
						stream().filter(TlsNamedCurves::isECCGroup).toList().size();
			}
			if (!configuration.getSupportedFFDHECipherSuites(tlsVersion).isEmpty() || tlsVersion == TlsVersion.TLS_V1_3) {
				maxIterationCount += configuration.getNotSupportedDHEGroups(tlsVersion).size();
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
