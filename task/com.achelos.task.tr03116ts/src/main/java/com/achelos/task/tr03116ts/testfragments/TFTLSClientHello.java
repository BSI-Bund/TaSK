package com.achelos.task.tr03116ts.testfragments;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.achelos.task.abstracttestsuite.AbstractTestFragment;
import com.achelos.task.abstracttestsuite.IStepExecution;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsTestToolMode;
import com.achelos.task.commons.enums.TlsTestToolTlsLibrary;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.commons.tlsextensions.TlsExtEncryptThenMac;
import com.achelos.task.commons.tlsextensions.TlsExtSignatureAlgorithms;
import com.achelos.task.commons.tlsextensions.TlsExtSupportedGroups;
import com.achelos.task.commons.tlsextensions.TlsExtension;
import com.achelos.task.commons.tlsextensions.TlsExtensionList;
import com.achelos.task.configuration.TestRunPlanConfiguration;


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
		TlsTestToolTlsLibrary tlsLibrary = TlsTestToolTlsLibrary.MBED_TLS; // default library

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
		}

		if (null == testTool) {
			logger.error("The test fragment" + testFragmentName + " called without TLS Test Tool Executor parameter. Aborting.");
			return null;
		}

		// reset
		testTool.defaultClientConfiguration();

		// basic configuration
		int stepCounter = 0;
		stepCounter++;
		step(prefix, stepCounter, "DUT acts as a TLS server and is reachable via TCP/IP at " +
				configuration.getDutAddress() + ":"
				+ configuration.getDutPort(), "");
		testTool.setMode(TlsTestToolMode.client);
		testTool.setClientHostAndPort();

		// The TLS ClientHello offers the configured TLS version.
		stepCounter++;
		step(prefix, stepCounter, "The TLS ClientHello offers the TLS version " + tlsVersion, "");
		if (tlsVersion == TlsVersion.TLS_V1_2 || tlsVersion == TlsVersion.TLS_V1_3) {
			testTool.setTlsVersion(tlsVersion);
		} else {
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
				if (tlsVersion == TlsVersion.TLS_V1_2) {
					TlsExtSignatureAlgorithms sigAlgExt = new TlsExtSignatureAlgorithms();
					tlsSignatureAlgorithms.stream().forEach(sa -> sigAlgExt
							.addSupportedSignatureAlgorithm(sa.getHashAlgorithm(), sa.getSignatureAlgorithm()));
					tlsExtensions.add(sigAlgExt);

				} else if (tlsVersion == TlsVersion.TLS_V1_3) {
					List<TlsSignatureScheme> signatureSchemes = new LinkedList<>();
					tlsSignatureAlgorithms.forEach(ss -> signatureSchemes.add(ss.getSignatureScheme()));

					testTool.setSignatureSchemes(signatureSchemes);
				}
			}
		}

		if (tlsCipherSuites.size() > 0) {
			if (configuration.containsPFSCipherSuite(tlsCipherSuites)) {
				stepCounter++;
				step(prefix, stepCounter, "In case the cipher suite is based on ECC, the TLS ClientHello "
						+ "offers valid elliptic curves in the appropriate extension according to the ICS.", "");
				if (tlsSupportedGroups.size() == 0) {
					tlsSupportedGroups
							= configuration.getSupportedEllipticCurvesAndFFDHE(tlsVersion);
				}
				if (tlsSupportedGroups.size() > 0) {
					if (tlsVersion == TlsVersion.TLS_V1_2) {
						TlsExtSupportedGroups extGroups = new TlsExtSupportedGroups();

						tlsSupportedGroups.stream().forEach(ng -> extGroups.addNamedGroup(ng));
						tlsExtensions.add(extGroups);
					} else if (tlsVersion == TlsVersion.TLS_V1_3) {
						testTool.setTlsSupportedGroups(tlsSupportedGroups);
					}
				}
			}
		}

		if (null != tlsExtensions && !tlsExtensions.isEmpty()) {
			// The TLS ClientHello offers one or more extensions.
			stepCounter++;
			step(prefix, stepCounter,
					"The TLS ClientHello offers the extensions " + tlsExtensions.getExtensionNames(), "");
			testTool.manipulateClientHelloExtensions(tlsExtensions);
		}

		testTool.setTlsLibrary(tlsLibrary);

		stepCounter++;
		return testTool;
	}
}
