package com.achelos.task.tr03116ts.testfragments;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commons.certificatehelper.CertificateHelper;
import com.achelos.task.commons.certificatehelper.ManipulateForceCertificateUsage;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import org.bouncycastle.util.encoders.Base64;


/**
 * A test fragment for selecting a specific certificate for TLS Test Tool.
 */
public class TFClientCertificate extends AbstractTestFragment {

	private final TestRunPlanConfiguration configuration;

	public TFClientCertificate(final IStepExecution parentStepExec) {
		super(parentStepExec, "Client certificate");
		configuration = TestRunPlanConfiguration.getInstance();
	}

	@Override
	public final Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);

		TlsTestToolExecutor testTool = null;
		boolean manipulateForceCertificateUsage = false;
		TlsTestToolCertificateTypes certType = TlsTestToolCertificateTypes.CERT_DEFAULT;
		int stepCounter = 1;

		for (Object param : params) {
			if (param instanceof TlsTestToolExecutor) {
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof TlsTestToolCertificateTypes) {
				certType = (TlsTestToolCertificateTypes) param;
			} else if (param instanceof ManipulateForceCertificateUsage) {
				manipulateForceCertificateUsage = true;
			}
		}
		
		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}
		
		String certificateFileName = configuration.getClientAuthCertChainFile();
		String authKeyFile = configuration.getClientAuthKeyFile();

		if(certType == TlsTestToolCertificateTypes.CERT_DEFAULT_CLIENT) {
			testTool.setCertificateAndPrivateKey(certificateFileName, authKeyFile);
		} else if(certType == TlsTestToolCertificateTypes.CERT_INVALID_SIG_CLIENT){
			String manipulatedCertPath = manipulateClientCertificateInvalidSignature().toAbsolutePath().toString();
			testTool.setCertificateAndPrivateKey(manipulatedCertPath, authKeyFile);
		}
		step(prefix, stepCounter++, "The TLS client selects the following certificate: " + certificateFileName, "");

		if (manipulateForceCertificateUsage) {
			step(prefix, stepCounter++, "The TLS client is forced to use an unsupported/invalid certificate: ", "");
			testTool.manipulateForceCertificateUsage();
		}
		
		return testTool;
	}


	public Path manipulateClientCertificateInvalidSignature() throws IOException, CertificateEncodingException {
		FileInputStream fis = new FileInputStream(configuration.getClientAuthCertChainFile());
		List<X509Certificate> certList =CertificateHelper.parseMultipleCertificates(fis);

		if(certList.size()==0){
			throw new FileNotFoundException("Certificate File could not be parsed correctly");
		}

		var encoded = certList.get(0).getEncoded();
		encoded[encoded.length-1] ^= (byte) 0xff; //flip last byte of certificate (last byte is part of signature)

		String manipulatedCertificate = Base64.toBase64String(encoded);

		StringBuilder outputCertFile = new StringBuilder(String.format("-----BEGIN CERTIFICATE-----%n%s%n-----END CERTIFICATE-----", manipulatedCertificate));
		//append all remaining certs
		for(X509Certificate cert: certList.subList(1, certList.size())){
			String certString = Base64.toBase64String(cert.getEncoded());
			outputCertFile.append(String.format("%n-----BEGIN CERTIFICATE-----%n%s%n-----END CERTIFICATE-----", certString));
		}

		var tmpCertFilePath = Files.createTempFile("", ".pem");
		Files.writeString(Path.of(tmpCertFilePath.toString()), outputCertFile.toString());
		return tmpCertFilePath;
	}


}
