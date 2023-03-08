package com.achelos.task.tr03116ts.testfragments;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.configuration.TlsTestToolConfigurationHandshakeType;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls12;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.*;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutpreparation.HandshakePreparationInfo;
import jakarta.xml.bind.DatatypeConverter;


/**
 * This test fragment configures a TLS connection
 */
public class TFTLSClientHello extends AbstractTestFragment {

	TestRunPlanConfiguration configuration;

	public TFTLSClientHello(final IStepExecution parentStepExec) {
		super(parentStepExec, "TLS Client Hello");

		configuration = TestRunPlanConfiguration.getInstance();
	}

	/**
	 * Configure a TLS client connecting to a server.
	 * <p>
	 * Server address and port must be provided by global configuration.
	 * <ul>
	 * <li>The TLS ClientHello offers the TLS version from parameter params.
	 * <li>The TLS ClientHello offers one or more cipher suites from parameter params.
	 * <li>In case the cipher suite is based on ECC, the TLS ClientHello offers valid elliptic curves in the appropriate
	 * extension according to the ICS.
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. <br>
	 * In case it is sent, all algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ul>
	 *
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result expected result text
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params <del>name=value pairs</del><br>
	 * tlsTestToolExecutor : required<br>
	 * typesafe parameters => {@link TlsTestToolExecutor}, {@link TlsVersion}, {@link TlsCipherSuite}, ... <br>
	 * tlsVersion: TLS version; default = {@link TlsVersion#TLS_V1_2}<br>
	 * cipherSuite: <br>
	 * signature algorithm: <br>
	 * supported group: <br>
	 * tlsLibrary: TLS Library default = {@link TlsTestToolTlsLibrary#MBED_TLS}
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
		List<TlsSignatureAlgorithmWithHash> tlsSignatureAlgorithms = new ArrayList<>();
		List<TlsNamedCurves> tlsSupportedGroups = new ArrayList<>();
		TlsExtension tlsExtension = null;
		TlsExtensionList tlsExtensions = TlsExtensionList.emptyList();
		TlsTestToolTlsLibrary tlsLibrary = null; // default library
		TlsTestToolConfigurationHandshakeType handshakeType = TlsTestToolConfigurationHandshakeType.NORMAL;
		HandshakePreparationInfo dutPreparationInfo = null;

		// Fetch the TlsTestToolExecutor
		if ((null == params) || (0 >= params.length)) {
			logger.error("The test fragment" + testFragmentName + " called without TLS TestToolExecutor parameter. Aborting.");
			return null;
		}
		for (Object param : params) {

			if (param instanceof TlsTestToolExecutor) {
				testTool = (TlsTestToolExecutor) param;
			} else if (param instanceof TlsVersion) {
				tlsVersion = (TlsVersion) param;
			} else if (param instanceof TlsCipherSuite) {
				tlsCipherSuites.add((TlsCipherSuite) param);
			} else if (param instanceof TlsSignatureAlgorithmWithHash) {
				tlsSignatureAlgorithms.add((TlsSignatureAlgorithmWithHash) param);
			} else if (param instanceof TlsNamedCurves) {
				tlsSupportedGroups.add((TlsNamedCurves) param);
			} else if (param instanceof List) {
				var genericList = (List<?>) param;

				if (genericList.size() > 0) {
					if (genericList.get(0) instanceof TlsCipherSuite) {
						tlsCipherSuites = (List<TlsCipherSuite>) genericList;
					} else if (genericList.get(0) instanceof TlsSignatureAlgorithmWithHash) {
						tlsSignatureAlgorithms = (List<TlsSignatureAlgorithmWithHash>) genericList;
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
			}
			else if (param instanceof TlsTestToolConfigurationHandshakeType) {
				handshakeType = (TlsTestToolConfigurationHandshakeType) param;
			}
		}

		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool Executor parameter. Aborting.");
			return null;
		}

		// reset
		testTool.defaultClientConfiguration();

		// If necessary, do DUT Preparation Steps and return the Information retrieved from them.
		if (handshakeType == TlsTestToolConfigurationHandshakeType.NORMAL) {
			try {
				dutPreparationInfo = configuration.getDutPreparer().prepareHandshake();
			} catch (Exception e) {
				logger.error("Unable to prepare Device Under Test for handshake. Aborting");
				throw e;
			}
		}

		// basic configuration
		int stepCounter = 0;
		stepCounter++;
		step(prefix, stepCounter, "DUT acts as a TLS server and is reachable via TCP/IP at " +
				configuration.getDutAddress() + ":"
				+ configuration.getDutPort(), "");
		testTool.setMode(TlsTestToolMode.client);
		if (dutPreparationInfo != null) {
			if (!dutPreparationInfo.getHostName().equalsIgnoreCase(configuration.getDutAddress())
			|| !Integer.toString(dutPreparationInfo.getPort()).equalsIgnoreCase(configuration.getDutPort())) {
				logger.error("DUT Address in TestRunplan  does not equal the real address of the device under test.");
			}
		}
		testTool.setClientHostAndPort();
		testTool.setSessionHandshakeType(handshakeType);

		// The TLS ClientHello offers the configured TLS version.
		stepCounter++;
		step(prefix, stepCounter, "The TLS ClientHello offers the TLS version " + tlsVersion, "");
		testTool.setTlsVersion(tlsVersion);
		if (tlsVersion == TlsVersion.TLS_V1_2 ) {
			if(tlsLibrary==null) {
				tlsLibrary = TlsTestToolTlsLibrary.MBED_TLS;
			}

		} else if(tlsVersion == TlsVersion.TLS_V1_3) {
			if(tlsLibrary==null) {
				tlsLibrary = TlsTestToolTlsLibrary.OpenSSL;
			}
		} else { //TLS 1.1 and lower
			testTool.setTlsVersion(TlsVersion.TLS_V1_2);
			testTool.manipulateHelloVersion(tlsVersion.getMajor(), tlsVersion.getMinor());
			tlsLibrary = TlsTestToolTlsLibrary.MBED_TLS;
			tlsVersion = TlsVersion.TLS_V1_2;
		}

		// The TLS ClientHello offers one or more cipher suites.
		stepCounter++;

		if (tlsCipherSuites.size() == 0) {
			tlsCipherSuites = configuration.getSupportedCipherSuites(tlsVersion);
		}
		step(prefix, stepCounter, "The TLS ClientHello offers the cipher suites " + tlsCipherSuites.toString(), "");
		testTool.setCipherSuite(tlsCipherSuites);

		/*add signature algorithms to Client Hello*/
		if (0 >= TlsVersion.TLS_V1_2.compareTo(tlsVersion)) {
			stepCounter++;
			step(prefix, stepCounter,
					"The presence of the signature_algorithms extension depends on the used TLS version."
							+ "	 * In case it is sent, all algorithms that are supported according to the ICS are listed.",
					"");

			if (tlsSignatureAlgorithms.size() == 0) {
				tlsSignatureAlgorithms = configuration.getSupportedSignatureAlgorithms(tlsVersion);
			}
			if (tlsSignatureAlgorithms.size() > 0) {
				TlsExtSignatureAlgorithms sigAlgExt = new TlsExtSignatureAlgorithms();
				tlsSignatureAlgorithms.forEach(sigAlgExt::addSupportedSignatureAlgorithm);
				tlsExtensions.add(sigAlgExt);

				//TLS 1.3: We add the API call additonally in OpenSSL so OpenSSL does have the same internal state, as we send in the ClientHello (with overwrittenClientHelloExtensions)
				if(tlsVersion==TlsVersion.TLS_V1_3) {
					List<TlsSignatureScheme> signatureSchemes = new LinkedList<>();
					tlsSignatureAlgorithms.forEach(ss -> signatureSchemes.add(((TlsSignatureAlgorithmWithHashTls13)(ss)).getSignatureScheme()));
					testTool.setSignatureSchemes(signatureSchemes);
				}

			}
		}

		/*add supportedGroups to Client Hello*/
		if (tlsCipherSuites.size() > 0) {
			if (configuration.containsPFSCipherSuite(tlsCipherSuites) || tlsVersion == TlsVersion.TLS_V1_3) {
				stepCounter++;
				step(prefix, stepCounter, "In case the cipher suite is based on ECC, the TLS ClientHello "
						+ "offers valid elliptic curves in the appropriate extension according to the ICS.", "");
				if (tlsSupportedGroups.size() == 0) {
					tlsSupportedGroups
							= configuration.getSupportedGroups(tlsVersion);
				}
				if (tlsSupportedGroups.size() > 0) {
					TlsExtSupportedGroups extGroups = new TlsExtSupportedGroups();

					tlsSupportedGroups.forEach(extGroups::addNamedGroup);
					tlsExtensions.add(extGroups);

					//TLS 1.3: We add the API call additonally in OpenSSL so OpenSSL does have the same internal state, as we send in the ClientHello (with overwrittenClientHelloExtensions)
					if(tlsVersion== TlsVersion.TLS_V1_3) {
						testTool.setTlsSupportedGroups(tlsSupportedGroups);
					}
				}
			}
		}


		if (!tlsExtensions.isEmpty()) {
			// The TLS ClientHello offers one or more extensions.

			//if we want to overwrite the ClientHelloExtensions in TLS 1.3, we need to add the supportedVersions extension as well
			if(tlsVersion == TlsVersion.TLS_V1_3) {
				tlsExtensions.add(new TlsExtSupportedVersions(TlsVersion.TLS_V1_3));
				tlsExtensions.add(TlsExtEcPointFormats.createDefault());
			}

			stepCounter++;
			step(prefix, stepCounter,
					"The TLS ClientHello offers the extensions " + tlsExtensions.getExtensionNames(), "");

			/*Do not add tlsExtensions if this is a resumption test case*/
			if(handshakeType== TlsTestToolConfigurationHandshakeType.NORMAL) {
				testTool.manipulateClientHelloExtensions(tlsExtensions);
			}
		}

		// This sets the server psk and pskidentity.
		if (dutPreparationInfo != null) {
			if (dutPreparationInfo.getPsk() != null && !dutPreparationInfo.getPsk().isBlank()) {
				step(prefix, stepCounter++, "Set the PreSharedKey value for the TLS client", "");
				testTool.setPSK(DatatypeConverter.parseHexBinary(dutPreparationInfo.getPsk()));
				if (dutPreparationInfo.getPskIdentity() != null && !dutPreparationInfo.getPskIdentity().isBlank()) {
					testTool.setPSKIdentity(dutPreparationInfo.getPskIdentity());
				}
			}
		} else if (configuration.getPSKValue()!=null && configuration.getPSKValue().length>0) {
			step(prefix, stepCounter++, "Set the PreSharedKey value for the TLS client", "");
			testTool.setPSK(configuration.getPSKValue());
			if(configuration.getPSKIdentityHint()!=null && !configuration.getPSKIdentityHint().isEmpty()){
				testTool.setPSKIdentityHint(configuration.getPSKIdentityHint());
			}
		}

		testTool.setTlsLibrary(tlsLibrary);

		stepCounter++;
		return testTool;
	}
}
