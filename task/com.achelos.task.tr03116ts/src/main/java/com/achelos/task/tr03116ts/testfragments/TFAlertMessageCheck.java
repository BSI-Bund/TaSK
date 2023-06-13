package com.achelos.task.tr03116ts.testfragments;


import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
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
		
		TlsTestToolExecutor testTool = null;
		TlsAlertLevel level = null;
		TlsAlertDescription description = null;
		
		for (Object param : params) {
			if (param instanceof TlsTestToolExecutor) {
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof TlsAlertLevel) {
				level = (TlsAlertLevel) param;
			} else if (param instanceof TlsAlertDescription) {
				description = (TlsAlertDescription) param;
			}
		}
		
		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool Executor parameter. Aborting.");
			return null;
		}
		
		
		String stepDescription = "Check if the DUT has sent an alert message";
		String expectedResult = "Receive an alert message";
		if(level != null) {
			stepDescription += " [level = " + level + "]";
			expectedResult += " [level = " + level + "]";
		}
		
		if(description != null) {
			stepDescription += " [description = " + description + "]";
			expectedResult += " [description = " + description + "]";
		}
		
		stepDescription += ".";
		expectedResult += ".";
		
		step(prefix, 1, stepDescription, expectedResult);
		
		
		testTool.assertAlertLogged(level, description);
		return testTool;
		
		
		
	}
}
