package com.achelos.task.tr03116ts.testfragments;


import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.abstracttestsuite.TestCaseCanceledException;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commons.enums.TlsAlertDescription;
import com.achelos.task.commons.enums.TlsAlertLevel;


/**
 * A test fragment for searching a specific TLS alert message in the TLS Tool Log.
 */
public class TFAlertMessageCheck extends AbstractTestFragment {

	public TFAlertMessageCheck(final IStepExecution parentStepExec) {
		super(parentStepExec, "Alert message check");
	}

	/**
	 * Check if an alert message has been printed. Parameters are the alert level and the alert description. The level
	 * parameter is required, while the description parameter is optional, so if the description parameter is null, it
	 * will be ignored.
	 *
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result expected result text
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params 1) level : The expected level of the alert. 2) description : The expected description of the alert.
	 * @return caller relevant information
	 */
	@Override
	public Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);
		// Fetch the TlsTestToolExecutor
		if (null != params && 0 < params.length && params[0] instanceof TlsTestToolExecutor) {
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];
			if (params.length == 1) {
				logStep(prefix, params);
				testTool.assertAlertLogged(null, null);
				return testTool;
			}
			if (params.length == 2) {
				logStep(prefix, params);
				testTool.assertAlertLogged((TlsAlertLevel) params[1], null);
				return testTool;
			}
			if (params.length == 3) {
				logStep(prefix, params);
				testTool.assertAlertLogged((TlsAlertLevel) params[1], (TlsAlertDescription) params[2]);
				return testTool;
			}
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool Executor parameter. Aborting.");
		return null;
	}


	private void logStep(final String prefix, final Object... params) throws TestCaseCanceledException {
		if (params.length == 2 || params.length == 3 && params[2] == null) {
			step(prefix, 1, "Check if the DUT has sent an alert message with level = " + params[1].toString() + ".",
					"Receive an alert message with level = " + params[1].toString() + ".");
		} else if (params.length == 3) {
			step(prefix, 1,
					"Check if the DUT has sent an alert message with level = " + params[1].toString()
							+ " and description = " + params[2].toString() + ".",
					"Receive an alert message with level = " + params[1].toString() + " and description = "
							+ params[2].toString() + ".");
		}
	}
}
