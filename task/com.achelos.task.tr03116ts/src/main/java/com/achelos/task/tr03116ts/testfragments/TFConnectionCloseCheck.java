package com.achelos.task.tr03116ts.testfragments;

import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;


/**
 * A test fragment for checking that the current TCP/IP connection has been closed.
 */
public class TFConnectionCloseCheck extends AbstractTestFragment {

	public TFConnectionCloseCheck(final IStepExecution parentStepExec) {
		super(parentStepExec, "Connection close check");
	}

	/**
	 * Verifies that the TLS connection has been closed.
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
		// Fetch the TlsTestToolExecutor
		if (null != params && 1 == params.length && params[0] instanceof TlsTestToolExecutor) {
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];
			step(prefix, 1, "Check if the DUT has closed the connection.", "Connection is closed by DUT.");
			testTool.assertMessageLogged(TestToolResource.TCP_IP_Conn_closed);
			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool Executor parameter. Aborting.");
		return null;

	}
}
