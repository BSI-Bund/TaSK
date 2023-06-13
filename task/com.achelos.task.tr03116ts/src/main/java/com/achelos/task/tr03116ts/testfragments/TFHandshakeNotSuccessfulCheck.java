package com.achelos.task.tr03116ts.testfragments;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commons.enums.TlsVersion;

import java.util.List;


/**
 * A test fragment to check if "TLS Test Tool exiting" message is received.
 */
public class TFHandshakeNotSuccessfulCheck extends AbstractTestFragment {

	public TFHandshakeNotSuccessfulCheck(final IStepExecution parentStepExec) {
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
		if (params != null && params.length == 2 && params[0] instanceof TlsTestToolExecutor && params[1] instanceof TlsVersion) {
			// Fetch the TlsTestToolExecutor
			final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];
			final TlsVersion tlsVersion = (TlsVersion) params [1];

			logger.info("Check if the TLS connection was not established: ");
			if(tlsVersion == TlsVersion.TLS_V1_2){
				testTool.assertMessageNotLogged(TestToolResource.Handshake_successful);
			} else if (tlsVersion == TlsVersion.TLS_V1_3){
				testTool.assertMessageNotLogged(TestToolResource.No_Error_After_Hs_Finished);
			}
			return testTool;
		}
		logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool parameter. Aborting.");
		return null;
	}
}
