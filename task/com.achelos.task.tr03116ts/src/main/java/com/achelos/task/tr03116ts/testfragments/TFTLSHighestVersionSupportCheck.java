package com.achelos.task.tr03116ts.testfragments;

import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtSupportedVersions;

import static com.achelos.task.commons.enums.TlsExtensionTypes.supported_versions;


/**
 * Test fragment to check if client offers highest TLS version in "ClientHello.client_version".
 */
public class TFTLSHighestVersionSupportCheck extends AbstractTestFragment {

	public TFTLSHighestVersionSupportCheck(final IStepExecution parentStepExec) {
		super(parentStepExec, "Check if the highest TLS version is offered by Client.");
	}

	@Override
	public final Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);

		// Fetch the TlsTestToolExecutor
		if ((null == params) || (params.length <= 0) || !(params[0] instanceof TlsTestToolExecutor)) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}
		final TlsTestToolExecutor tlsTestToolExecutor = (TlsTestToolExecutor) params[0];

		final TlsVersion highestSupportedTlsVersion
				= tlsTestToolExecutor.getConfiguration().getHighestSupportedTlsVersion();

		boolean supportHighestVersion = false;
		if(highestSupportedTlsVersion == TlsVersion.TLS_V1_3){
			supportHighestVersion = tlsTestToolExecutor.supportsTls13inSupportedVersionsExt();
		}else /*TLS 1.2*/{
			String clientHelloVersion = tlsTestToolExecutor.getValue(TestToolResource.ClientHello_version);
			if(clientHelloVersion == null){
				logger.error("Unable to find \"ClientHello.client_version\".");
				return null;
			}
			supportHighestVersion = clientHelloVersion.equals(highestSupportedTlsVersion.getTlsVersionHexString());

		}

		if (supportHighestVersion) {
			logger.info("The TLS ClientHello offers the highest TLS version \""
					+ highestSupportedTlsVersion
					+ "\" stated in the ICS.");
		} else {
			logger.error(
					"The TLS ClientHello does not offer the highest TLS version \""
							+ highestSupportedTlsVersion
							+ "\" stated in the ICS.");
		}

		return null;
	}

}
