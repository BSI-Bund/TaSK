package com.achelos.task.tr03116ts.testfragments;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;

import java.util.List;


/**
 * A test fragment for closing the current TLS Session
 */
public class TFServerSessionClose extends AbstractTestFragment {

	public TFServerSessionClose(final IStepExecution parentStepExec) {
		super(parentStepExec, "Close TLS Session.");
	}

	/**
	 * Close current TLS Session if applicable.
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

			step(prefix, 1, "TLS connection is closed", "Connection is closed.");
			testTool.assertMessageLogged(TestToolResource.Closed_TLS_Session.getInternalToolOutputMessage());
			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool parameter. Aborting.");
		return null;
	}
}
