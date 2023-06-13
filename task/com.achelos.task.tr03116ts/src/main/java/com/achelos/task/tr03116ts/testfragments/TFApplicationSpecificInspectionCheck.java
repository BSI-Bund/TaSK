package com.achelos.task.tr03116ts.testfragments;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.dutexecution.DUTExecutor;
import com.achelos.task.logging.LoggingConnector;

import java.util.List;


/**
 * A test fragment to check the Application Specific Inspection Instruction
 */
public class TFApplicationSpecificInspectionCheck extends AbstractTestFragment {

	public TFApplicationSpecificInspectionCheck(final IStepExecution parentStepExec) {
		super(parentStepExec, "Check Application Specific Inspection Instructions");
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
		if (params != null && params.length == 2 && params[0] instanceof TlsTestToolExecutor && params[1] instanceof DUTExecutor) {
			// Fetch the TlsTestToolExecutor
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];
			final DUTExecutor dutExecutor = (DUTExecutor) params[1];

			boolean handshakeSuccessful = testTool.assertMessageLogged(TestToolResource.Handshake_successful, LoggingConnector.DEBUG);
			dutExecutor.checkApplicationSpecificInspectionInstructions(handshakeSuccessful);

			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool parameter. Aborting.");
		return null;
	}
}
