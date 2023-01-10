package com.achelos.task.tr03116ts.testfragments;


import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.dut.DUTExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationHandshakeType;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commons.enums.TlsCipherSuite;


/**
 * This test fragment that starts the TLS Test Tool Executor, performs handshake and checks for Client hello message.
 */
public class TFDUTClientNewConnection extends AbstractTestFragment {

	public TFDUTClientNewConnection(final IStepExecution parentStepExec) {
		super(parentStepExec, "DUT - Client - new connection");
	}

	/**
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result expected result text
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params optional parameters relevant for the test fragment
	 * @return caller relevant information
	 */
	@Override
	public Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);
		// Check if the required TLS Test Tool Executor was received
		if (params == null) {
			logger.error(
					"Fragment \"DUT - Client - new connection\" called without TLS Test Tool parameter. Aborting.");
			return null;
		}
		TlsTestToolExecutor testTool = null;
		IterationCounter iterationCounter = null;
		DUTExecutor dutExecutor = null;
		boolean isSessionResumption = false;
		for (Object param : params) {

			if (param instanceof TlsTestToolExecutor) {
				// Fetch the TlsTestToolExecutor
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof IterationCounter) {
				iterationCounter = (IterationCounter) param;
			} else if (param instanceof TlsTestToolConfigurationHandshakeType) {
				TlsTestToolConfigurationHandshakeType paramType = (TlsTestToolConfigurationHandshakeType) param;

				if (TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionID.equals(paramType)
						|| TlsTestToolConfigurationHandshakeType.SessionResumptionWithSessionTicket
								.equals(paramType)) {
					isSessionResumption = true;
				}
			} else if (param instanceof DUTExecutor) {
				dutExecutor = (DUTExecutor) param;
			} else {
				logger.debug("Ignore invalid parameter provided in test fragment " + testFragmentName + ".");
			}
		}

		if (null == testTool) {
			logger.error("Test fragment " + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		} else if (null == dutExecutor) {
			logger.error("Test fragment " + testFragmentName + " called without DUTExecutor parameter");
			return null;
		}

		if (iterationCounter != null) {
			testTool.start(iterationCounter.getCurrentIteration(), iterationCounter.getTotalNumberOfIterations());
		} else if (!isSessionResumption) {
			testTool.start();
		}

		step(prefix, 1,
				"Tester request: Please motivate the DUT to create a TLS connection over TCP/IP"
						+ " to " + TlsTestToolExecutor.TLS_TEST_TOOL_LOCAL_HOST_AS_SERVER + ":"
						+ testTool.getConfiguration().getTlsTestToolPort() + ".",
				"Tester confirms that the DUT was motivated.\r\nReceive a ClientHello message from the DUT.");

		// Check if it's a handshake with session resumption. Do not check for TCP/IP connection in case of session
		// resumption because connection is already established.
		if (!isSessionResumption) {
			// Wait until TLS server is ready and trigger TLS client via motivator
			testTool.assertMessageLogged(TestToolResource.Waiting_TCP_IP_conn_port);
		}

		if (iterationCounter != null) {
			dutExecutor.start(isSessionResumption, iterationCounter.getCurrentIteration(),
					iterationCounter.getTotalNumberOfIterations());
		} else {
			dutExecutor.start(isSessionResumption);
		}
		testTool.assertMessageLogged(TestToolResource.TCP_IP_conn_from);
		boolean clientHelloReceived = testTool.assertMessageLogged(TestToolResource.ClientHello);
		if (clientHelloReceived) {
			logger.debug("The TLS server received the ClientHello message from the DUT.");

			List<TlsCipherSuite> clientHelloCipherSuites = TlsCipherSuite
					.parseCipherSuiteStringList(testTool.getValue(TestToolResource.ClientHello_cipher_suites));
			logger.debug("The DUT offers following cipher suite(s): " + clientHelloCipherSuites.toString());
			try {
				testTool.getClientHelloExtensions();
			} catch (Exception e) {
				logger.debug("Unable to read extensions.");
			}
		}
		return testTool;
	}
}
