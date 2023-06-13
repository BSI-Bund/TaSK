package com.achelos.task.tr03116ts.testfragments;


import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.*;
import com.achelos.task.commons.tlsextensions.*;
import com.achelos.task.configuration.TestRunPlanConfiguration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * This test fragment configures a TLS connection.
 */
public class TFTLSServerHello extends AbstractTestFragment {

	TestRunPlanConfiguration configuration;
	

	public TFTLSServerHello(final IStepExecution parentStepExec) {
		super(parentStepExec, "TLS Server Hello");

		configuration = TestRunPlanConfiguration.getInstance();
	}

	/**
	 * Configure a TLS server responding to a client hello.
	 * <p>
	 * Server address and port must be provided by global configuration.
	 * <ul>
	 * <li>The TLS server answers the DUT choosing a TLS version and a cipher suite that is contained in the
	 * ClientHello.
	 * </ul>
	 *
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result expected result text
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params <del>name=value pairs</del><br>
	 * typesafe parameters => {@link TlsTestToolExecutor}, {@link TlsVersion}, {@link TlsCipherSuite}, ... <br>
	 * tlsTestToolExecutor : required<br>
	 * tlsVersion: TLS version; default = {@link TlsVersion#TLS_V1_2}<br>
	 * cipherSuite: <br>
	 * signature algorithm: <br>
	 * supported group: <br>
	 * tlsLibrary: TLS Library default = {@link TlsTestToolTlsLibrary#MBED_TLS} <br>
	 * requestClientCertificate:
	 * @return caller relevant information
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object executeSteps(final String prefix, final String result,
			final List<String> strParams, final Object... params) throws Exception {
		super.executeSteps(prefix, result, strParams, params);

		TlsTestToolExecutor testTool = null;
		TlsVersion tlsVersion = TlsVersion.TLS_V1_2; // init with default value
		List<TlsCipherSuite> tlsCipherSuites = new ArrayList<>();
		List<TlsNamedCurves> tlsSupportedGroups = new ArrayList<>();
		TlsExtension tlsExtension = null;
		TlsExtensionList tlsExtensions = TlsExtensionList.emptyList();
		TlsTestToolTlsLibrary tlsLibrary = null; // default library
		TlsECGroup ecGroup = null;
		TlsDHGroup dhGroup = null;
		List<TlsSignatureAlgorithmWithHash> tlsSignatureAlgorithms = new ArrayList<>();
		boolean requestClientCertificate = false;
		
		
		// Fetch the TlsTestToolExecutor
		if ((null == params) || (0 >= params.length)) {
			logger.error("Test fragment \"TLS Server Hello\" called without TlsTestToolExecutor parameter");
			return null;
		}
		for (Object param : params) {

			if (param instanceof TlsTestToolExecutor) {
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof TlsVersion) {
				tlsVersion = (TlsVersion) param;
			} else if (param instanceof TlsCipherSuite) {
				tlsCipherSuites.add((TlsCipherSuite) param);
			} else if (param instanceof TlsNamedCurves) {
				tlsSupportedGroups.add((TlsNamedCurves) param);
			} else if (param instanceof TlsECGroup) {
				ecGroup = (TlsECGroup) param;
			} else if (param instanceof TlsSignatureAlgorithmWithHash) {
				tlsSignatureAlgorithms.add((TlsSignatureAlgorithmWithHash) param);
			} else if (param instanceof TlsDHGroup) {
				dhGroup = (TlsDHGroup) param;
			} else if (param instanceof List) {
				var genericList = (List<?>) param;

				if (genericList.size() > 0) {
					if (genericList.get(0) instanceof TlsCipherSuite) {
						tlsCipherSuites = (List<TlsCipherSuite>) genericList;
					} else if (genericList.get(0) instanceof TlsNamedCurves) {
						tlsSupportedGroups = (List<TlsNamedCurves>) genericList;
					}
				}
			} else if (param instanceof TlsExtension) {
				tlsExtension = (TlsExtension) param;
				tlsExtensions.add(tlsExtension);
				if (param instanceof TlsExtEncryptThenMac) {
					/*
					 * this is a special case since we overwrite the ETM extension in the ClientHello, However, the ETM
					 * extension is reflected in the ServerHello and the TlsLibrary does not recognize since we
					 * internally disabled all extensions by default normally, this is only necessary in MbedTLS, but
					 * does not break OpenSSL if we use it there also (we do it for all libraries --> simplicity)
					 */
					testTool.tlsEncryptThenMacEnable();
				}
			} else if (param instanceof TlsTestToolTlsLibrary) {
				tlsLibrary = (TlsTestToolTlsLibrary) param;
			} else if (param instanceof RequestClientCertificate) {
				requestClientCertificate = true;
			}
		}

		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TlsTestToolExecutor parameter");
			return null;
		}

		// basic configuration
		int stepCounter = 1;
		step(prefix, stepCounter, "The TlsTestTool acts as a server and is reachable via TCP/IP: " +
				TlsTestToolExecutor.TLS_TEST_TOOL_LOCAL_HOST_AS_SERVER + ":" + configuration.getTlsTestToolPort(),
				"");
		stepCounter++;
		testTool.setMode(TlsTestToolMode.server);
		testTool.setServerHostAndPort();
		testTool.setListenTimeout();

		// The TLS ClientHello offers the configured TLS version.
		step(prefix, stepCounter++, "The TLS server supports the following TLS version: " + tlsVersion, "");
		if (tlsVersion == TlsVersion.TLS_V1_2) {
			if(tlsLibrary==null){
				tlsLibrary = TlsTestToolTlsLibrary.MBED_TLS;
			}
			testTool.setTlsVersion(tlsVersion);
		} else if (tlsVersion == TlsVersion.TLS_V1_3){
			testTool.setTlsVersion(TlsVersion.TLS_V1_3);
			tlsLibrary = TlsTestToolTlsLibrary.OpenSSL;
		} else {
			testTool.setTlsVersion(TlsVersion.TLS_V1_2);
			testTool.manipulateHelloVersion(tlsVersion.getMajor(), tlsVersion.getMinor());
			tlsLibrary = TlsTestToolTlsLibrary.MBED_TLS;
			tlsVersion = TlsVersion.TLS_V1_2;
		}

		// The TLS ClientHello offers one or more cipher suites.

		if (tlsCipherSuites.size() == 0) {
			tlsCipherSuites = configuration.getSupportedCipherSuites(tlsVersion);
		}
		step(prefix, stepCounter++,
				"The TLS server supports the following Cipher suite(s): " + tlsCipherSuites.toString(), "");
		testTool.setCipherSuite(tlsCipherSuites);

		if (ecGroup != null) {
			testTool.setECGroup(ecGroup);
			step(prefix, stepCounter++,
					"The TLS server supports the following ECC Group: "
							+ ecGroup,
					"");
		}
		if (dhGroup != null) {
			testTool.setDHGroup(dhGroup);
			step(prefix, stepCounter++,
					"The TLS server supports the following DHE Group: "
							+ dhGroup,
					"");
		}
		if (tlsSupportedGroups.size() > 0) {
			// only works for MbedTLS
			if (tlsVersion == TlsVersion.TLS_V1_2) {
				if (tlsSupportedGroups.size() > 1) {
					logger.debug(
							"Only the first Supported Group is selected since TlsTestTool in server mode"
									+ " can not set more than one group");
				}
				if (tlsSupportedGroups.get(0).isFFDHEGroup()) {
					testTool.setDHGroup(tlsSupportedGroups.get(0));
					step(prefix, stepCounter++,
							"The TLS server supports the following FFDHE Group: "
									+ tlsSupportedGroups.get(0).getName(),
							"");
				} else {
					/* supportedGroup is elliptic curve */
					testTool.setECGroup(tlsSupportedGroups.get(0));
					step(prefix, stepCounter++,
							"The TLS server supports the following ECC Group: "
									+ tlsSupportedGroups.get(0).getName(),
							"");
				}
			} else if (tlsVersion == TlsVersion.TLS_V1_3) {
				testTool.setTlsSupportedGroups(tlsSupportedGroups); // maybe this works for OpenSSL
				//logger.error("This functionality is not implemented for TLS 1.3 yet");
			}
		}
		if(tlsSignatureAlgorithms.size()>0&& tlsVersion == TlsVersion.TLS_V1_3){
			List<TlsSignatureScheme> signatureSchemes = new LinkedList<>();
			tlsSignatureAlgorithms.forEach(ss -> signatureSchemes.add(((TlsSignatureAlgorithmWithHashTls13)(ss)).getSignatureScheme()));
			testTool.setSignatureSchemes(signatureSchemes);		}

		if (null != tlsExtensions && !tlsExtensions.isEmpty()) {
			if(tlsVersion != TlsVersion.TLS_V1_3) {
				tlsExtensions.add(new TlsExtRenegotiationInfo()); // Maybe add for all testcases in ServerHello Fragment
			}
//			if(tlsVersion == TlsVersion.TLS_V1_3) {
//				tlsExtensions.add(new TlsExtSupportedVersions(TlsVersion.TLS_V1_3, false));
//			}
			step(prefix, stepCounter++, "The TLS ServerHello offers the following extension(s): "
					+ tlsExtensions.getExtensionNames(), "");
			if(tlsVersion != TlsVersion.TLS_V1_3) {
				testTool.manipulateServerHelloExtensions(tlsExtensions);
			}
			else{
				testTool.manipulateEncryptedExtensionsTls13(tlsExtensions);
			}
		}

		if(configuration.getPSKValue()!=null && configuration.getPSKValue().length>0){
			step(prefix, stepCounter++, "Set the PreSharedKey value for the TLS server", "");
			testTool.setPSK(configuration.getPSKValue());
			// Always set default PSK Identity.
			if (configuration.getPSKIdentity()!=null && !configuration.getPSKIdentity().isBlank()){
				testTool.setPSKIdentity(configuration.getPSKIdentity());
			} else {
				testTool.setPSKIdentity("Client_identity");
			}
			if(configuration.getPSKIdentityHint()!=null && !configuration.getPSKIdentityHint().isEmpty()){
				testTool.setPSKIdentityHint(configuration.getPSKIdentityHint());
			}
		}
		
		if (requestClientCertificate) {
			testTool.setCaCertificateFile(Path.of(configuration.getCertificateValidationRootCA()));
			testTool.tlsVerifyPeer();
		}

		testTool.setTlsLibrary(tlsLibrary);

		return testTool;
	}
}
