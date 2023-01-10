package com.achelos.task.tr03116ts.testfragments;

import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;


/**
 * A test fragment for closing the current TCP/IP connection
 */
public class TFTCPIPCloseConnection extends AbstractTestFragment {

	public TFTCPIPCloseConnection(final IStepExecution parentStepExec) {
		super(parentStepExec, "TCP/IP close connection");
	}

	/**
	 * Close current TCP/IP connection if applicable.
	 *
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
		if (params != null && params.length == 1 && params[0] instanceof TlsTestToolExecutor) {
			// Fetch the TlsTestToolExecutor
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];

			step(prefix, 1, "Close current TCP/IP connection if applicable.", "Connection is closed.");
			testTool.infoMessageLogged("TCP/IP connection is closed");
			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool parameter. Aborting.");
		return null;
	}
}
