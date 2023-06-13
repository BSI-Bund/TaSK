package com.achelos.task.tr03116ts.testfragments;

import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;


/**
 * A test fragment to check if "TLS Test Tool exiting" message is received.
 */
public class TFLocalServerClose extends AbstractTestFragment {

	public TFLocalServerClose(final IStepExecution parentStepExec) {
		super(parentStepExec, "Local Server - Close");
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
		// Fetch the TlsTestToolExecutor
		if (params != null && params.length == 1 && params[0] instanceof TlsTestToolExecutor) {
			// Fetch the TlsTestToolExecutor
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];

			step(prefix, 1, "Local TLS server closes any open connections and shuts down.",
					"Local TLS server is stopped.");
			testTool.infoMessageLogged("TLS Test Tool exiting");
			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool parameter. Aborting.");
		return null;
	}
}
