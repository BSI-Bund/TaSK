package com.achelos.task.tr03116ts.testcases.b.b1.gp;

import java.util.Arrays;
import java.util.Collections;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPCloseConnection;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;


/**
 * Test case TLS_B1_GP_09 - Select strongest cipher suite.
 * <p>
 * This positive test evaluates the ability of the DUT to select a stronger cipher suite given a choice.
 */
public class TLS_B1_GP_09 extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B1_GP_09";
	private static final String TEST_CASE_DESCRIPTION = "Select strongest cipher suite";
	private static final String TEST_CASE_PURPOSE
			= "This positive test evaluates the ability of the DUT to select a stronger cipher suite given a choice.";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTCPIPCloseConnection tFTCPIPCloseConnection;

	public TLS_B1_GP_09() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfClientHello = new TFTLSClientHello(this);
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTCPIPCloseConnection = new TFTCPIPCloseConnection(this);
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
	protected void preProcessing() throws Exception {

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
	 * <li>The TLS ClientHello offers cipher suites which are supported according to the ICS, however in the reverted
	 * order. This means that the less preferable cipher suite is put at the beginning of the list, while the most
	 * preferable is put at the end of the list.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The DUT ignores the order of the cipher suites in the client hello and does not select any interim cipher
	 * suite from the beginning of the list. Instead one of the recommended cipher suites from the end of the list is
	 * selected.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * </ul>
	 */
	// @SuppressWarnings("unused")
	@Override
	protected void executeUsecase() throws Exception {
		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/** highest supported TLS version */
		TlsVersion tlsVersion = configuration.getHighestSupportedTlsVersion();
		if (tlsVersion == null) {
			logger.error("No supported TLS versions found.");
			return;
		}
		logger.debug("TLS version: " + tlsVersion.getName());

		/* supported cipher suites */
		var cipherSuites = configuration.getSupportedCipherSuites(tlsVersion);
		if (null == cipherSuites || cipherSuites.isEmpty()) {
			logger.error("No supported Cipher suites found.");
			return;
		}

		/* reverse the cipher suite order */
		Collections.reverse(cipherSuites);
		logger.debug("Supported Cipher suites:");
		for (TlsCipherSuite cipherSuite : cipherSuites) {
			logger.debug(cipherSuite.name());
		}

		tfClientHello.executeSteps("1", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
				+ " .", null, testTool, tlsVersion, cipherSuites);
		testTool.start();

		tFTCPIPNewConnection.executeSteps("2", "The TLS ClientHello offers cipher suites which are "
				+ "supported according to the ICS, however in the reverted order.", Arrays.asList(),
				testTool);

		tFTCPIPCloseConnection.executeSteps("3", "", Arrays.asList(),
				testTool);

		testTool.assertMessageLogged(TestToolResource.Handshake_successful);
		testTool.assertServerHelloCipherSuitesContains(cipherSuites.get(cipherSuites.size() - 1));
	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected void cleanAndExit() throws Exception {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}

