package com.achelos.task.tr03116ts.testfragments;

import java.util.Arrays;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.utilities.logging.LogBean;


/**
 * A test fragment for checking the TLS version in the Server.Hello.
 */
public class TFTLSVersionCheck extends AbstractTestFragment {

	private final TFConnectionCloseCheck tFConnectionCloseCheck;

	public TFTLSVersionCheck(final IStepExecution parentStepExec) {
		super(parentStepExec, "TLS version check");
		tFConnectionCloseCheck = new TFConnectionCloseCheck(this);
	}

	/**
	 * Checks the TLS version in the Server.Hello.
	 *
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result expected result text
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params 1) tlsVersion : The TLS version which should be tested. 2) isSupported : A boolean, should the TLS
	 * Version be supported?
	 * @return caller relevant information
	 */
	@Override
	public Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);
		// Fetch the TlsTestToolExecutor
		if ((null == params) || (3 > params.length) || !(params[0] instanceof TlsTestToolExecutor)) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}
		final TlsTestToolExecutor testTool = (TlsTestToolExecutor) params[0];

		TlsVersion tlsVersion = (TlsVersion) params[1];
		Boolean isSupported = (Boolean) params[2];
		Boolean isOptional = false;
		if (params.length > 3) {
			isOptional = (Boolean) params[3];
		}

		step(prefix, 1, "Send ClientHello with client_version=" + tlsVersion.getName() + ".", "");
		if (isSupported) {
			step(prefix, 2, "If <isSupported> is true.",
					"Receive ServerHello message with server_version=<tlsVersion>.");
			if (isOptional) {
				boolean validServerHello
						= testTool.assertMessageLogged(TestToolResource.ServerHello_valid, BasicLogger.INFO);
				if (!validServerHello) {
					return testTool;
				}
			} else {
				testTool.assertMessageLogged(TestToolResource.ServerHello_valid);
			}

			String serverVersion = null;
			if (TlsVersion.TLS_V1_3.equals(tlsVersion)) {
				final byte[] supportedVersionByte = testTool.findExtensionTypeLogged(TlsTestToolMode.server,
						TlsExtensionTypes.supported_versions);

				final TlsVersion supportedVersion = supportedVersionByte != null
						? TlsVersion.getTlsVersion(supportedVersionByte[0], supportedVersionByte[1]) : null;
				serverVersion = supportedVersion != null ? supportedVersion.getTlsVersionHexString() : null;
			} else {
				serverVersion = testTool.getValue(TestToolResource.ServerHello_server_version);
			}

			logger.info("Expected: ServerHello.server_version=" + tlsVersion.getTlsVersionHexString());
			step(prefix, 3,
					"If <isOptional>. If the version support is optional than no error message will be logged.",
					"Log info if the version is supported or not.");
			if (serverVersion != null) {
				if (serverVersion.equalsIgnoreCase(tlsVersion.getTlsVersionHexString()) || isOptional) {
					logger.info("Actual: ServerHello.server_version=" + serverVersion);
				} else {
					logger.error("Actual: ServerHello.server_version=" + serverVersion);
				}
			}
		} else {
			step(prefix, 4, "If <isSupported> is false.", "Expected result is checked in the following steps.");
			LogBean serverHelloValid = testTool.findMessage(TestToolResource.ServerHello_valid);
			LogBean handshakeSuccessful = testTool.findMessage(TestToolResource.Handshake_successful);
			LogBean handshakeFailed = testTool.findMessage(TestToolResource.Handshake_failed);
			if (serverHelloValid != null || handshakeSuccessful != null || handshakeFailed == null) {
				logger.error("Handshake was successful with " + tlsVersion.getName());
			} else {
				logger.info("Handshake failed with " + tlsVersion.getName());
			}
			tFConnectionCloseCheck.executeSteps(prefix + ".7", "",
					Arrays.asList(), testTool);
		}
		return testTool;

	}
}
