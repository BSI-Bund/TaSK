package com.achelos.task.tr03116ts.testfragments;


import java.util.ArrayList;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.helper.CrlOcspCertificate;
import com.achelos.task.commons.certificatehelper.ManipulateForceCertificateUsage;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;


/**
 * A test fragment for selecting a specific certificate TLS Test Tool.
 */
public class TFServerCertificate extends AbstractTestFragment {

	private final TestRunPlanConfiguration configuration;

	public TFServerCertificate(final IStepExecution parentStepExec) {
		super(parentStepExec, "Server certificate");
		configuration = TestRunPlanConfiguration.getInstance();
	}

	@Override
	public Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);

		TlsTestToolExecutor testTool = null;
		TlsVersion tlsVersion = null;
		List<TlsCipherSuite> tlsCipherSuites = new ArrayList<>();
		List<TlsSignatureAlgorithmWithHash> tlsSignatureAlgorithms = new ArrayList<>();
		TlsTestToolCertificateTypes certificateType = null;
		CrlOcspCertificate crlOcspCertificate = null;
		boolean manipulateForceCertificateUsage= false;
		int stepCounter = 1;

		// Fetch the TlsTestToolExecutor
		if ((null == params) || (0 >= params.length)) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}
		for (Object param : params) {

			if (param instanceof TlsTestToolExecutor) {
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof TlsVersion) {
				tlsVersion = (TlsVersion) param;
			} else if (param instanceof TlsTestToolCertificateTypes) {
				certificateType = (TlsTestToolCertificateTypes) param;
			} else if (param instanceof TlsCipherSuite) {
				tlsCipherSuites.add((TlsCipherSuite) param);
			} else if (param instanceof CrlOcspCertificate) {
				 crlOcspCertificate = (CrlOcspCertificate) param;
			} else if (param instanceof ManipulateForceCertificateUsage){
				manipulateForceCertificateUsage = true;
			}

			else if (param instanceof List) {
				var genericList = (List<?>) param;

				if (genericList.size() > 0) {
					if (genericList.get(0) instanceof TlsCipherSuite) {
						tlsCipherSuites = (List<TlsCipherSuite>) genericList;
					} else if (genericList.get(0) instanceof TlsSignatureAlgorithmWithHash) {
						tlsSignatureAlgorithms = (List<TlsSignatureAlgorithmWithHash>) genericList;
					}
				}
			}
		}

		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}

		// it is also possible to put several certificates in one file !!
		String serverCertFileName = "";
		// set Server Certificate+
		if (crlOcspCertificate != null) {
			TlsCipherSuite cipherSuite
					= tlsCipherSuites == null || tlsCipherSuites.isEmpty() ? null : tlsCipherSuites.get(0);
			String[] certAndKeyPath
					= testTool.getConfiguration().getCrlOcspCertificate(cipherSuite, certificateType);
			serverCertFileName = testTool.setCertificateAndPrivateKey(certAndKeyPath[0], certAndKeyPath[1]);
		} else {
			if (0 >= TlsVersion.TLS_V1_2.compareTo(tlsVersion)) {
				if (tlsCipherSuites.isEmpty() && tlsSignatureAlgorithms.isEmpty()) {
					var sigAlgList = configuration.getSupportedSignatureAlgorithms(tlsVersion);
					if (sigAlgList.isEmpty()) {
						logger.error("No signature algorithms specified in the MICS file.");
						throw new RuntimeException("No signature algorithms specified in the MICS file.");
					} else {
						step(prefix, stepCounter++,
								"The TLS server supports the following Signature algorithm: " + sigAlgList.get(0),
								"");
						serverCertFileName
								= testTool.setCertificateAndPrivateKey(tlsVersion, sigAlgList.get(0), certificateType);
					}
				} else if (!tlsCipherSuites.isEmpty()) {
					serverCertFileName
							= testTool.setCertificateAndPrivateKey(tlsVersion, tlsCipherSuites.get(0), certificateType);
				} else {
					step(prefix, stepCounter++,
							"The TLS server supports the following Signature algorithm: "
									+ tlsSignatureAlgorithms.get(0),
							"");
					serverCertFileName = testTool.setCertificateAndPrivateKey(tlsVersion, tlsSignatureAlgorithms.get(0),
							certificateType);
				}
			} else {
				/* TLS 1.3 implementation */
			}
		}
		step(prefix, stepCounter++, "The TLS server selects the following certificate: " + serverCertFileName, "");

		if(manipulateForceCertificateUsage){
			step(prefix, stepCounter++, "The TLS server is forced to use an unsupported/invalid certificate: ", "");
			testTool.manipulateForceCertificateUsage();
		}

		return testTool;
	}

}
