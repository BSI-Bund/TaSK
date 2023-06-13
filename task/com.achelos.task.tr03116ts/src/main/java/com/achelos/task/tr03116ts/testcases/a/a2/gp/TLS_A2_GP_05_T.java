package com.achelos.task.tr03116ts.testcases.a.a2.gp;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtSupportedVersions;
import com.achelos.task.commons.tlsextensions.TlsExtensionList;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.*;
import com.achelos.task.utilities.logging.IterationCounter;


/**
 * Test case TLS_A2_GP_05_T - Unsupported TLS version.
 * <p>
 * This test verifies that no downgrade to a TLS version that is not supported according to the ICS is possible.
 */
public class TLS_A2_GP_05_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_A2_GP_05_T";
	private static final String TEST_CASE_DESCRIPTION = "Unsupported TLS version";
	private static final String TEST_CASE_PURPOSE
			= "This test verifies that no downgrade to a TLS version that is not supported "
					+ "according to the ICS is possible.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private DUTExecutor dutExecutor = null;
	private final TFTLSServerHello tfServerHello;
	private final TFServerCertificate tfserverCertificate;
	private final TFLocalServerClose tfLocalServerClose;
	private final TFAlertMessageCheck tfAlertMessageCheck;
	private final TFDUTClientNewConnection tFDutClientNewConnection;
	private final TFApplicationSpecificInspectionCheck tfApplicationCheck;
	private final TFHandshakeNotSuccessfulCheck tfHandshakeNotSuccessfulCheck;


	public TLS_A2_GP_05_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfServerHello = new TFTLSServerHello(this);
		tfserverCertificate = new TFServerCertificate(this);
		tfLocalServerClose = new TFLocalServerClose(this);
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
	 * <li>The TLS server answers the DUT choosing an unsupported TLS version and a cipher suite that is contained in
	 * the ClientHello.
	 * </ol>
	 * 
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS server supplies the certificate chain [CERT_DEFAULT].
	 * <li>The server sends a CertificateRequest to the DUT.
	 * <li>No TLS extensions are supplied by the server which are not required to conduct the TLS handshake.
	 * </ol>
	 *
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT does not accept the certificate chain and sends a ""protocol_version"" alert or another suitable error description.
	 * <li>No TLS connection is established.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** highest supported TLS version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();

		// getUnsupportedVersions, make for TLS 1.2 downgrade to TLS 1.1 1.0 and SSLV3
		var unsupportedVersions = configuration.getNotSupportedTLSVersions();

		if (tlsVersion == null) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.TLS_VERSION + tlsVersion.getName());

		List<TlsVersion> lowerUnsupportedVersions = new ArrayList<>();
		for (TlsVersion unsupportedVersion : unsupportedVersions) {
			if (unsupportedVersion.getMinor() < tlsVersion.getMinor()) {
				lowerUnsupportedVersions.add(unsupportedVersion);
			}
		}

		int iterationCount = 1;
		for (TlsVersion lowerUnsupportedVersion : lowerUnsupportedVersions) {
			logger.info("Start iteration " + iterationCount + " of " + lowerUnsupportedVersions.size() + ".");
			step(1, "Setting TLS version to : " + lowerUnsupportedVersion.getName(), null);
			
			tfserverCertificate.executeSteps("2", "The TLS server supplies the certificate chain [CERT_DEFAULT].",
					Arrays.asList(), testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);
			
			tfServerHello.executeSteps("3", "Server started and waits for new client connection", Arrays.asList(),
					testTool, tlsVersion, TlsTestToolTlsLibrary.OpenSSL, new RequestClientCertificate());


			if(tlsVersion == TlsVersion.TLS_V1_3){
				/*in TLS 1.3 we only manipulate the extension supported versions to the downgraded TLS version*/
				var extensionList = new TlsExtensionList();
				extensionList.add(new TlsExtSupportedVersions(lowerUnsupportedVersion, false));
				testTool.manipulateServerHelloExtensions(extensionList);
			}
			else {
				testTool.manipulateHelloVersion(lowerUnsupportedVersion.getMajor(), lowerUnsupportedVersion.getMinor());
			}

			tFDutClientNewConnection.executeSteps("4",
					"The TLS server receives a ClientHello handshake message from the DUT.",
					Arrays.asList(), testTool, new IterationCounter(iterationCount, lowerUnsupportedVersions.size()),
					dutExecutor);
			iterationCount++;

			tfAlertMessageCheck.executeSteps("5", "The DUT does not accept the certificate chain and sends " +
							"a \"protocol_version\" alert or another suitable error description.",
					Arrays.asList("level=warning/fatal", "description=protocol_version"), testTool, TlsAlertDescription.protocol_version);

			tfApplicationCheck.executeSteps("6", "", Arrays.asList(), testTool, dutExecutor);

			tfHandshakeNotSuccessfulCheck.executeSteps("7", "No TLS channel is established", null, testTool, tlsVersion);

			tfLocalServerClose.executeSteps("8", "Server closed successfully", Arrays.asList(),
					testTool);
			dutExecutor.resetProperties();
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
		dutExecutor.cleanAndExit();
	}
}
