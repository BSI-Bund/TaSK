package com.achelos.task.micsverifier;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.achelos.task.abstracttestsuite.RunState;
import com.achelos.task.abstracttestsuite.TestCaseRun;
import com.achelos.task.abstracttestsuite.TestSuiteRun;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.datastructures.mics.TlsVersionSupport;
import com.achelos.task.xmlparser.datastructures.tlsspecification.CipherSuite;
import com.achelos.task.xmlparser.datastructures.tlsspecification.DiffHellGroup;
import com.achelos.task.xmlparser.datastructures.tlsspecification.RestrictionLevel;
import com.achelos.task.xmlparser.datastructures.tlsspecification.SignatureAlgorithm;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;


class MICSChecklistVerifier {

	public static final String ICS_CHECKLIST_TESTSUITE_ID = "ICS Checklist";
	private static final String PSK_CIPHER_SUITES_TYPE = "PSK-based-CipherSuites";
	private static final String PSK_AND_CERT_CIPHER_SUITES_TYPE = "PSK-and-certificate-based-CipherSuites";

	private final MICS mics;
	private final TlsSpecification tlsSpecification;
	private final LoggingConnector logger;

	/**
	 * Hidden Constructor.
	 */
	private MICSChecklistVerifier(final MICS mics, final TlsSpecification tlsSpecification) {
		this.mics = mics;
		this.tlsSpecification = tlsSpecification;
		logger = LoggingConnector.getInstance();
	}

	/**
	 * Verifies the MICS Checklist for the MICS according to Module 0: ICS Checklist (Chapter 6.1 of TR-03116-TS) (up
	 * until but not including 6.1.1)
	 *
	 * @param mics The MICS to check against.
	 * @param tlsSpecification The Configuration, i.e. the application specific tls specification
	 * @return true if successfully verified. false otherwise.
	 */
	public static List<TestCaseRun> verifyMicsChecklist(final MICS mics, final TlsSpecification tlsSpecification) {
		var logger = LoggingConnector.getInstance();
		var testCaseRunList = new ArrayList<TestCaseRun>();
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Initializing the MICS file checklist verification.");
		var checklistVerifier = new MICSChecklistVerifier(mics, tlsSpecification);
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Successfully initialized the MICS file checklist verification.");
		var micsChecklistTestSuite = new TestSuiteRun(ICS_CHECKLIST_TESTSUITE_ID,
				Arrays.asList("TLS_ICS_01", "TLS_ICS_02", "TLS_ICS_03", "TLS_ICS_04", "TLS_ICS_05", "TLS_ICS_06",
						"TLS_ICS_07", "TLS_ICS_08", "TLS_ICS_09", "TLS_ICS_10", "TLS_ICS_11", "TLS_ICS_12"));
		micsChecklistTestSuite.setStartTime();
		logger.tellLogger(BasicLogger.MSG_NEW_TESTSUITE, micsChecklistTestSuite);

		// TLS_ICS_01
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_01.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_01());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_01.");
		// TLS_ICS_02
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_02.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_02());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_02.");
		// TLS_ICS_03
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_03.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_03());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_03.");
		// TLS_ICS_04
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_04.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_04());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_04.");
		// TLS_ICS_05
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_05.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_05());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_05.");
		// TLS_ICS_06
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_06.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_06());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_06.");
		// TLS_ICS_07
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_07.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_07());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_07.");
		// TLS_ICS_08
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_08.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_08());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_08.");
		// TLS_ICS_09
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_09.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_09());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_09.");
		// TLS_ICS_10
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_10.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_10());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_10.");
		// TLS_ICS_11
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_11.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_11());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_11.");
		// TLS_ICS_12
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_ICS_12.");
		testCaseRunList.add(checklistVerifier.TLS_ICS_12());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_ICS_12.");

		micsChecklistTestSuite.setEndTime();
		logger.tellLogger(BasicLogger.MSG_TESTSUITE_ENDED, micsChecklistTestSuite);

		return testCaseRunList;
	}

	/*
	 * Check that the vendor has submitted a current ICS for the implementation to be tested. It covers the exact
	 * version of the submitted software.
	 */
	private TestCaseRun TLS_ICS_01() {
		final String testCaseName = "TLS_ICS_01";
		final String testCaseDescription = "TLS_ICS_01 in TR-03116-TS";
		final String testCasePurpose
				= "The vendor has submitted a current ICS for the implementation to be tested. It covers the exact version of the submitted software.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);
		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that the vendor has submitted a current ICS for the implementation to be tested.");
		var result = !mics.getVersion().isBlank();

		testRun.increaseWarningCount();
		var infoString = MICSVerifier.LOGGER_COMPONENT + "The MICS file is submitted for the version: " + mics.getVersion();
		infoString = infoString
				+ " The version of the MICS file has to be manually checked against the exact version of the submitted software.";
		logger.info(infoString);
		testRun.addStatusMessage(infoString);
		reportResult(testCaseName, result,
				"The version of the MICS file has to be manually checked against the exact version of the submitted software.");

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);
		return testRun;
	}

	/*
	 * Check that Table 4 of the ICS contains all mandatory TLS versions according to the application specific
	 * requirements
	 */
	private TestCaseRun TLS_ICS_02() {
		final String testCaseName = "TLS_ICS_02";
		final String testCaseDescription = "TLS_ICS_02 in TR-03116-TS";
		final String testCasePurpose
				= "Table 4 of the ICS contains all mandatory TLS versions according to the application-specific requirement.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking if Table 4 of the ICS contains all mandatory TLS versions according to the application-specific requirements.");
		var missingTlsVersions = new ArrayList<String>();
		var specifiedTlsVersions = tlsSpecification.getTlsVersionSupport().values();
		var micsSupportedTlsVersions = mics.getSupportedTlsVersions();
		for (var specifiedTlsVersion : specifiedTlsVersions) {
			if (specifiedTlsVersion.getRestriction() == RestrictionLevel.REQUIRED) {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + specifiedTlsVersion.getTlsVersionEnum().getName()
						+ " is mandatory.");
				var included = false;
				for (var supportedTlsVersion : micsSupportedTlsVersions) {
					if (supportedTlsVersion.getTlsVersion().equals(specifiedTlsVersion.getTlsVersionEnum())) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + specifiedTlsVersion.getTlsVersionEnum().getName()
								+ " is supported according to the MICS file.");

						included = true;
						break;
					}
				}
				if (!included) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + specifiedTlsVersion.getTlsVersionEnum().getName()
							+ " is not supported according to the MICS file.");
					missingTlsVersions.add("The MICS file is missing required TLS version: "
							+ specifiedTlsVersion.getTlsVersionEnum().getName());
				}
			}
		}

		var result = missingTlsVersions.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "All mandatory TLS versions according to the application-specific requirements are contained in the MICS file.");
			reportResult("TLS_ICS_02", result);
		} else {
			for (var s : missingTlsVersions) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "Some mandatory TLS version according to the application-specific requirements is not contained in the MICS file.");
			reportResult("TLS_ICS_02", result, missingTlsVersions);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Check that Table 4 of the ICS does not contain any TLS version which is not recommended according to the
	 * application-specific requirements.
	 */
	private TestCaseRun TLS_ICS_03() {
		final String testCaseName = "TLS_ICS_03";
		final String testCaseDescription = "TLS_ICS_03 in TR-03116-TS";
		final String testCasePurpose
				= "Table 4 of the ICS does not contain any TLS version which is not recommended according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking if Table 4 of the ICS does not contain any TLS version which is not recommended according to the application-specific requirements.");
		var notRecommendedTlsVersions = new ArrayList<String>();
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + supportedTlsVersion.getTlsVersion().getName()
					+ " is supported according to the MICS file.");
			var allowed = false;
			for (var specifiedTlsVersion : tlsSpecification.getTlsVersionSupport().values()) {
				if (specifiedTlsVersion.getTlsVersionEnum().equals(supportedTlsVersion.getTlsVersion())) {
					if (specifiedTlsVersion.getRestriction() != RestrictionLevel.FORBIDDEN) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + supportedTlsVersion.getTlsVersion().getName()
								+ " is allowed according to the application-specific requirements.");
						allowed = true;
					}
					break;
				}
			}
			if (!allowed) {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLS version " + supportedTlsVersion.getTlsVersion().getName()
						+ " is not allowed according to the application-specific requirements.");
				notRecommendedTlsVersions.add(
						"MICS includes not recommended TLS version: " + supportedTlsVersion.getTlsVersion().getName());
			}
		}

		var result = notRecommendedTlsVersions.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "All TLS versions contained in the MICS file are allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_03", result);
		} else {
			for (var s : notRecommendedTlsVersions) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "Some TLS version contained in the MICS file is not allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_03", result, notRecommendedTlsVersions);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);
		return testRun;
	}

	/*
	 * Check that Table 5 of the ICS contains all mandatory cipher suites according to the application-specific
	 * requirements.
	 */
	private TestCaseRun TLS_ICS_04() {
		final String testCaseName = "TLS_ICS_04";
		final String testCaseDescription = "TLS_ICS_04 in TR-03116-TS";
		final String testCasePurpose
				= "Table 5 of the ICS contains all mandatory cipher suites according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 5 of the ICS contains all mandatory cipher suites according to the application-specific requirements.");
		var missingCipherSuites = new ArrayList<String>();

		// TLS version TLSv1.2 (if supported)
		TlsVersionSupport tls12Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_2)) {
				tls12Support = supportedTlsVersion;
				break;
			}
		}
		if (tls12Support != null) {
			var specifiedCipherSuites = tlsSpecification.getTlsv1_2Spec().getCipherSuiteSupport();
			var atLeastOneCipherSuites = new ArrayList<CipherSuite>();
			for (var specifiedCipherSuite : specifiedCipherSuites.values()) {
				// Required cipher suites
				if (specifiedCipherSuite.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(
							MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription() + " is mandatory.");
					if (specifiedCipherSuite.getType().equals(PSK_CIPHER_SUITES_TYPE)
							|| specifiedCipherSuite.getType().equals(PSK_AND_CERT_CIPHER_SUITES_TYPE)) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Skipping the PSK cipher suites for now.");
						continue;
					}
					var included = false;
					for (var supportedCipherSuite : tls12Support.getSupportedCipherSuites()) {
						if (supportedCipherSuite.equals(specifiedCipherSuite.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription()
								+ " is not supported according to the MICS file.");
						missingCipherSuites.add("The MICS file is missing required cipher suite for TLSv1.2: "
								+ specifiedCipherSuite.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedCipherSuite.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneCipherSuites.add(specifiedCipherSuite);
				}
			}

			if (!atLeastOneCipherSuites.isEmpty()) {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "At least one of the following cipher suites is required.");
				var atLeastOneFulfilled = false;
				for (var specifiedCipherSuite : atLeastOneCipherSuites) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "" + specifiedCipherSuite.getDescription());
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedCipherSuite : tls12Support.getSupportedCipherSuites()) {
						if (supportedCipherSuite.equals(specifiedCipherSuite.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}

				if (!atLeastOneFulfilled) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "None of these cipher suites is supported according to the MICS file.");
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneCipherSuites) {
						sb.append(s.getDescription() + ", ");
					}
					missingCipherSuites.add(
							"The MICS file is missing at least one of the following cipher suites for TLSv1.2: " + sb.toString());
				} else {
					logger.debug(
							MICSVerifier.LOGGER_COMPONENT + "At least one of these cipher suites is supported according to the MICS file.");
				}
			}
		}

		// TLS version TLSv1.3 (if supported)
		TlsVersionSupport tls13Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				tls13Support = supportedTlsVersion;
				break;
			}
		}
		if (tls13Support != null) {
			var specifiedCipherSuites = tlsSpecification.getTlsv1_3Spec().getCipherSuiteSupport();
			var atLeastOneCipherSuites = new ArrayList<CipherSuite>();
			for (var specifiedCipherSuite : specifiedCipherSuites.values()) {
				// Required cipher suites
				if (specifiedCipherSuite.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(
							MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription() + " is mandatory.");
					var included = false;
					for (var supportedCipherSuite : tls13Support.getSupportedCipherSuites()) {
						if (supportedCipherSuite.equals(specifiedCipherSuite.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + specifiedCipherSuite.getDescription()
								+ " is not supported according to the MICS file.");
						missingCipherSuites.add("The MICS file is missing required cipher suite for TLSv1.3: "
								+ specifiedCipherSuite.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedCipherSuite.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneCipherSuites.add(specifiedCipherSuite);
				}
			}

			if (!atLeastOneCipherSuites.isEmpty()) {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "At least one of the following cipher suites is required.");
				var atLeastOneFulfilled = false;
				for (var specifiedCipherSuite : atLeastOneCipherSuites) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "" + specifiedCipherSuite.getDescription());
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedCipherSuite : tls13Support.getSupportedCipherSuites()) {
						if (supportedCipherSuite.equals(specifiedCipherSuite.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "None of the following cipher suites are supported according to the MICS file.");
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneCipherSuites) {
						sb.append(s.getDescription() + ", ");
					}
					missingCipherSuites.add(
							"The MICS file is missing at least one of the following cipher suites for TLSv1.3: " + sb.toString());
				} else {
					logger.debug(
							MICSVerifier.LOGGER_COMPONENT + "At least one of these cipher suites is supported according to the MICS file.");
				}
			}
		}

		var result = missingCipherSuites.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "All mandatory cipher suites according to the application-specific requirements are supported according to the MICS file.");
			reportResult("TLS_ICS_04", result);
		} else {
			for (var s : missingCipherSuites) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "Some mandatory cipher suite according to the application-specific requirements is not supported according to the MICS file.");
			reportResult("TLS_ICS_04", result, missingCipherSuites);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Check that the DUT does not support any cipher suite not recommended according to the application-specific
	 * requirements.
	 */
	private TestCaseRun TLS_ICS_05() {
		final String testCaseName = "TLS_ICS_05";
		final String testCaseDescription = "TLS_ICS_05 in TR-03116-TS";
		final String testCasePurpose
				= "The DUT does not support any cipher suite not recommended according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that the DUT does not support any cipher suite not recommended according to the application-specific requirements.");
		var notRecommendedCipherSuites = new ArrayList<String>();

		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			var supportedCipherSuites = supportedTlsVersion.getSupportedCipherSuites();
			HashMap<String, CipherSuite> specifiedCipherSuites = null;
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				specifiedCipherSuites = tlsSpecification.getTlsv1_3Spec().getCipherSuiteSupport();
			} else {
				specifiedCipherSuites = tlsSpecification.getTlsv1_2Spec().getCipherSuiteSupport();
			}

			for (var supportedCipherSuite : supportedCipherSuites) {
				logger.debug(
						MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + supportedCipherSuite + " is supported according to the MICS file.");
				var allowed = false;
				for (var specifiedCipherSuite : specifiedCipherSuites.values()) {
					if (specifiedCipherSuite.getDescription().equals(supportedCipherSuite)) {
						if (specifiedCipherSuite.getRestriction() != RestrictionLevel.FORBIDDEN) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + supportedCipherSuite
									+ " is allowed according to the application-specific requirements.");
							allowed = true;
						}
						break;
					}
				}
				if (!allowed) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Cipher suite " + supportedCipherSuite
							+ " is not allowed according to the application-specific requirements.");
					notRecommendedCipherSuites.add("The MICS file includes not recommended cipher suite for TLS version "
							+ supportedTlsVersion.getTlsVersion().getName() + ": " + supportedCipherSuite);
				}
			}
		}

		var result = notRecommendedCipherSuites.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "All supported cipher suites in the MICS file are allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_05", result);
		} else {
			for (var s : notRecommendedCipherSuites) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "Some supported cipher suites in the MICS file are not allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_05", result, notRecommendedCipherSuites);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Check that Table 7 of the ICS contains only named elliptic curves according to IANA.
	 */
	private TestCaseRun TLS_ICS_06() {
		final String testCaseName = "TLS_ICS_06";
		final String testCaseDescription = "TLS_ICS_06 in TR-03116-TS";
		final String testCasePurpose = "Table 7 of the ICS contains only named elliptic curves according to IANA.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 7 of the ICS contains only named elliptic curves according to IANA.");
		var notNamedCurves = new ArrayList<String>();
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			for (var group : supportedTlsVersion.getSupportedGroups()) {
				try {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "The MICS file contains elliptic curve name: " + group);
					if (TlsNamedCurves.valueOf(group) == null) {
						throw new Exception();
					}
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Elliptic Curve Name: " + group + " is defined according to IANA.");
				} catch (Exception e) {
					notNamedCurves.add("MICS includes group not being a namedCurve defined by IANA for TLS version"
							+ supportedTlsVersion.getTlsVersion().getName() + ": " + group);
				}
			}
		}
		var result = notNamedCurves.isEmpty();
		if (result) {
			logger.info(MICSVerifier.LOGGER_COMPONENT + "All elliptic curve names in the MICS file are defined according to IANA.");
			reportResult("TLS_ICS_06", result);
		} else {
			for (var s : notNamedCurves) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(MICSVerifier.LOGGER_COMPONENT + "Some elliptic curve names in the MICS file are not defined according to IANA.");
			reportResult("TLS_ICS_06", result, notNamedCurves);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Check that Table 7 of the ICS contains all mandatory elliptic curves according to the application-specific
	 * requirements.
	 */
	private TestCaseRun TLS_ICS_07() {
		final String testCaseName = "TLS_ICS_07";
		final String testCaseDescription = "TLS_ICS_07 in TR-03116-TS";
		final String testCasePurpose
				= "Table 7 of the ICS contains all mandatory elliptic curves according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 7 of the ICS contains all mandatory elliptic curves according to the application-specific requirements.");
		var missingGroups = new ArrayList<String>();

		// TLS version TLSv1.2 (if supported)
		TlsVersionSupport tls12Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_2)) {
				tls12Support = supportedTlsVersion;
				break;
			}
		}
		if (tls12Support != null) {
			var specifiedGroups = tlsSpecification.getTlsv1_2Spec().getDHGroupSupport();
			var atLeastOneGroups = new ArrayList<DiffHellGroup>();
			for (var specifiedGroup : specifiedGroups.values()) {
				// Required cipher suites
				if (specifiedGroup.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
							+ " is required according to the application-specific requirements.");
					var included = false;
					for (var supportedGroup : tls12Support.getSupportedGroups()) {
						if (supportedGroup.equals(specifiedGroup.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
								+ " is not supported according to the MICS file.");
						missingGroups.add(
								"MICS is missing required named curves for TLSv1.2: " + specifiedGroup.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedGroup.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneGroups.add(specifiedGroup);
				}
			}

			if (!atLeastOneGroups.isEmpty()) {
				var atLeastOneFulfilled = false;
				for (var specifiedGroup : atLeastOneGroups) {
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedGroup : tls12Support.getSupportedGroups()) {
						if (supportedGroup.equals(specifiedGroup.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneGroups) {
						sb.append(s.getDescription() + ", ");
					}
					missingGroups.add(
							"MICS is missing at least one of the following named curves for TLSv1.2: " + sb.toString());
				}
			}
		}

		// TLS version TLSv1.3 (if supported)
		TlsVersionSupport tls13Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				tls13Support = supportedTlsVersion;
				break;
			}
		}
		if (tls13Support != null) {
			var specifiedGroups = tlsSpecification.getTlsv1_3Spec().getDHGroupSupport();
			var atLeastOneGroups = new ArrayList<DiffHellGroup>();
			for (var specifiedGroup : specifiedGroups.values()) {
				// Required cipher suites
				if (specifiedGroup.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
							+ " is required according to the application-specific requirements.");
					var included = false;
					for (var supportedGroup : tls13Support.getSupportedGroups()) {
						if (supportedGroup.equals(specifiedGroup.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Group " + specifiedGroup.getDescription()
								+ " is not supported according to the MICS file.");
						missingGroups.add(
								"MICS is missing required named curves for TLSv1.3: " + specifiedGroup.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedGroup.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneGroups.add(specifiedGroup);
				}
			}

			if (!atLeastOneGroups.isEmpty()) {
				var atLeastOneFulfilled = false;
				for (var specifiedGroup : atLeastOneGroups) {
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedGroup : tls13Support.getSupportedGroups()) {
						if (supportedGroup.equals(specifiedGroup.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneGroups) {
						sb.append(s.getDescription() + ", ");
					}
					missingGroups.add(
							"MICS is missing at least one of the following NamedCurves for TLSv1.3: " + sb.toString());
				}
			}
		}

		var result = missingGroups.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "All mandatory DH groups according to the application-specific requirements are supported according to the MICS file.");
			reportResult("TLS_ICS_07", result);
		} else {
			for (var s : missingGroups) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "Some mandatory DH groups according to the application-specific requirements are not supported according to the MICS file.");
			reportResult("TLS_ICS_07", result, missingGroups);
		}


		// Check that the DUT does not support any named curves not recommended according to the application-specific requirements.
		logger.debug(
				"MICSVerifier: Checking that the DUT does not support any named curves not recommended according to the application-specific requirements.");
		var notRecommendedNamedCurves = new ArrayList<String>();

		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			var supportedGroups = supportedTlsVersion.getSupportedGroups();
			HashMap<String, DiffHellGroup> specifiedNamedCurves = null;
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				specifiedNamedCurves = tlsSpecification.getTlsv1_3Spec().getDHGroupSupport();
			} else {
				specifiedNamedCurves = tlsSpecification.getTlsv1_2Spec().getDHGroupSupport();
			}

			for (var supportedGroup : supportedGroups) {
				logger.debug(
						"MICSVerifier: Named curve " + supportedGroup + " is supported according to MICS file.");
				var allowed = false;
				for (var specifiedNamedCurve : specifiedNamedCurves.values()) {
					if (specifiedNamedCurve.getDescription().equals(supportedGroup)) {
						if (specifiedNamedCurve.getRestriction() != RestrictionLevel.FORBIDDEN) {
							logger.debug("MICSVerifier: Named curve " + supportedGroup
									+ " is allowed according to the application-specific requirements.");
							allowed = true;
						}
						break;
					}
				}
				if (!allowed) {
					logger.debug("MICSVerifier: Named Curve " + supportedGroup
							+ " is not allowed according to the application-specific requirements.");
					notRecommendedNamedCurves.add("MICS includes not recommended NamedCurve for TLS Version "
							+ supportedTlsVersion.getTlsVersion().getName() + ": " + supportedGroup);
				}
			}
		}

		result = notRecommendedNamedCurves.isEmpty();
		if (result) {
			logger.info(
					"MICSVerifier: All supported NamedCurves in MICS file are allowed according to the application-specific requirements.");
		} else {
			for (var s : notRecommendedNamedCurves) {



				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					"MICSVerifier: Some supported NamedCurves in MICS file are not allowed according to the application-specific requirements.");
		}

		if (!(notRecommendedNamedCurves.isEmpty() && missingGroups.isEmpty())) {
			reportResult("TLS_ICS_07", false, Stream.concat(notRecommendedNamedCurves.stream(), missingGroups.stream()).toList());
		} else {
			reportResult("TLS_ICS_07", true);
		}


		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Check that Table 6 of the ICS contains only conformant key lengths according to the application specific
	 * requirements.
	 */
	private TestCaseRun TLS_ICS_08() {
		final String testCaseName = "TLS_ICS_08";
		final String testCaseDescription = "TLS_ICS_08 in TR-03116-TS";
		final String testCasePurpose
				= "Table 6 of the ICS contains only conformant key lengths according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 6 of the ICS contains only conformant key lengths according to the application-specific requirements.");
		var notRecommendedKeyLengths = new ArrayList<String>();

		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			var supportedKeyLengths = supportedTlsVersion.getSupportedKeyLengths();
			var specifiedKeyLengths = tlsSpecification.getTlsMinimumKeyLength();

			for (var supportedKeyLengthEntry : supportedKeyLengths.entrySet()) {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "Checking supported Key Lengths for " + supportedKeyLengthEntry.getKey() + ".");
				var allowed = false;
				for (var specifiedKeyLength : specifiedKeyLengths.values()) {
					if (specifiedKeyLength.algorithmName.equals(supportedKeyLengthEntry.getKey())) {
						for (var length : specifiedKeyLength.minimumKeyLengths) {
							if (!length.useUntil.trim().endsWith("+")) {
								var useUntil = Integer.parseInt(length.useUntil.trim());
								try {
									var currentYear = Calendar.getInstance().get(Calendar.YEAR);
									if (useUntil < currentYear) {
										continue;
									}
								} catch (Exception e) {
									throw new IllegalArgumentException("Unable to query current Year.", e);
								}
							}
							if (length.minimumKeyLength <= supportedKeyLengthEntry.getValue()) {
								logger.debug(MICSVerifier.LOGGER_COMPONENT + "Required Key Length " + length.minimumKeyLength.toString()
										+ " is fulfilled by the MICS file.");
								allowed = true;
								break;
							}
						}
					}
				}
				if (!allowed) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "MICS includes not recommended Key Length " + supportedKeyLengthEntry.getKey() + ": "
							+ supportedKeyLengthEntry.getValue());
					notRecommendedKeyLengths.add("MICS includes not recommended key lengths for TLS version "
							+ supportedTlsVersion.getTlsVersion().getName() + ": " + supportedKeyLengthEntry.getKey() + ": "
							+ supportedKeyLengthEntry.getValue());
				}
			}
		}

		var result = notRecommendedKeyLengths.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file contains only conformant key lengths according to the application-specific requirements.");
			reportResult("TLS_ICS_08", result);
		} else {
			for (var s : notRecommendedKeyLengths) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file contains some non-conformant key length according to the application-specific requirements.");
			reportResult("TLS_ICS_08", result, notRecommendedKeyLengths);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Table 8 of the ICS contains all mandatory signature algorithms according to the application-specific requirements
	 */
	private TestCaseRun TLS_ICS_09() {
		final String testCaseName = "TLS_ICS_09";
		final String testCaseDescription = "TLS_ICS_09 in TR-03116-TS";
		final String testCasePurpose
				= "Table 8 of the ICS contains all mandatory signature algorithms according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 8 of the ICS contains all mandatory signature algorithms according to the application-specific requirements.");
		var missingSignAlgorithms = new ArrayList<String>();

		// TLS version TLSv1.2 (if supported)
		TlsVersionSupport tls12Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_2)) {
				tls12Support = supportedTlsVersion;
				break;
			}
		}
		if (tls12Support != null) {
			var specifiedSignAlgorithms = tlsSpecification.getTlsv1_2Spec().getSignAlgorithmSupport();
			var atLeastOneSignAlgorithms = new ArrayList<SignatureAlgorithm>();
			for (var specifiedSignAlg : specifiedSignAlgorithms.values()) {
				// Required cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Algorithm " + specifiedSignAlg.getDescription()
							+ " is mandatory according to application-specific requirements.");
					var included = false;
					for (var supportedSignAlg : tls12Support.getSupportedSignAlgorithms()) {
						if (supportedSignAlg.equals(specifiedSignAlg.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Algorithm " + specifiedSignAlg.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Algorithm " + specifiedSignAlg.getDescription()
								+ " is not supported according to the MICS file.");
						missingSignAlgorithms.add("MICS is missing required Signature Algorithm for TLSv1.2: "
								+ specifiedSignAlg.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneSignAlgorithms.add(specifiedSignAlg);
				}
			}

			if (!atLeastOneSignAlgorithms.isEmpty()) {
				var atLeastOneFulfilled = false;
				for (var specifiedSignAlg : atLeastOneSignAlgorithms) {
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedSignAlg : tls12Support.getSupportedSignAlgorithms()) {
						if (supportedSignAlg.equals(specifiedSignAlg.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneSignAlgorithms) {
						sb.append(s.getDescription() + ", ");
					}
					missingSignAlgorithms
							.add("MICS is missing at least one of the following signature algorithms for TLSv1.2: "
									+ sb.toString());
				}
			}
		}

		// TLS version TLSv1.3 (if supported)
		TlsVersionSupport tls13Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				tls13Support = supportedTlsVersion;
				break;
			}
		}
		if (tls13Support != null) {
			var specifiedSignAlgorithms = tlsSpecification.getTlsv1_3Spec().getHandshakeSignAlgorithmSupport();
			var atLeastOneSignAlgorithms = new ArrayList<SignatureAlgorithm>();
			for (var specifiedSignAlg : specifiedSignAlgorithms.values()) {
				// Required cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
							+ " is mandatory according to application-specific requirements.");
					var included = false;
					for (var supportedGroup : tls13Support.getSupportedSignAlgorithms()) {
						if (supportedGroup.equals(specifiedSignAlg.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
								+ " is not supported according to the MICS file.");
						missingSignAlgorithms.add("MICS is missing required Handshake Signature Algorithm for TLSv1.3: "
								+ specifiedSignAlg.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneSignAlgorithms.add(specifiedSignAlg);
				}
			}

			if (!atLeastOneSignAlgorithms.isEmpty()) {
				var atLeastOneFulfilled = false;
				for (var specifiedSignAlg : atLeastOneSignAlgorithms) {
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedGroup : tls13Support.getSupportedSignAlgorithms()) {
						if (supportedGroup.equals(specifiedSignAlg.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneSignAlgorithms) {
						sb.append(s.getDescription() + ", ");
					}
					missingSignAlgorithms.add(
							"MICS is missing at least one of the following handshake signature algorithms for TLSv1.3: "
									+ sb.toString());
				}
			}
		}

		var result = missingSignAlgorithms.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file contains all mandatory signature algorithms according to the application-specific requirements.");
			reportResult("TLS_ICS_09", result);
		} else {
			for (var s : missingSignAlgorithms) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file does not contain all mandatory signature algorithms according to the application-specific requirements.");
			reportResult("TLS_ICS_09", result, missingSignAlgorithms);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Table 9 of the ICS contains all mandatory signature algorithms for certificates according to the
	 * application-specific requirements.
	 */
	private TestCaseRun TLS_ICS_10() {
		final String testCaseName = "TLS_ICS_10";
		final String testCaseDescription = "TLS_ICS_10 in TR-03116-TS";
		final String testCasePurpose
				= "Table 9 of the ICS contains all mandatory signature algorithms for certificates according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 9 of the ICS contains all mandatory signature algorithms for certificates according to the application-specific requirements.");
		var missingSignAlgorithms = new ArrayList<String>();

		// TLS version TLSv1.3 (if supported)
		TlsVersionSupport tls13Support = null;
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				tls13Support = supportedTlsVersion;
				break;
			}
		}
		if (tls13Support != null) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLSv1.3 is supported.");
			var specifiedSignAlgorithms = tlsSpecification.getTlsv1_3Spec().getCertificateSignAlgorithmSupport();
			var atLeastOneSignAlgorithms = new ArrayList<SignatureAlgorithm>();
			for (var specifiedSignAlg : specifiedSignAlgorithms.values()) {
				// Required cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.REQUIRED) {
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
							+ " is mandatory according to application-specific requirements.");
					var included = false;
					for (var supportedGroup : tls13Support.getSupportedSignAlgorithmsCert()) {
						if (supportedGroup.equals(specifiedSignAlg.getDescription())) {
							logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
									+ " is supported according to the MICS file.");
							included = true;
							break;
						}
					}
					if (!included) {
						logger.debug(MICSVerifier.LOGGER_COMPONENT + "Signature Scheme " + specifiedSignAlg.getDescription()
								+ " is not supported according to the MICS file.");
						missingSignAlgorithms.add("MICS is missing required Certificate Signature Algorithm for TLSv1.3: "
								+ specifiedSignAlg.getDescription());
					}
				}

				// At Least One - cipher suites
				if (specifiedSignAlg.getRestriction() == RestrictionLevel.ATLEAST_ONE) {
					atLeastOneSignAlgorithms.add(specifiedSignAlg);
				}
			}

			if (!atLeastOneSignAlgorithms.isEmpty()) {
				var atLeastOneFulfilled = false;
				for (var specifiedSignAlg : atLeastOneSignAlgorithms) {
					if (atLeastOneFulfilled) {
						break;
					}
					for (var supportedGroup : tls13Support.getSupportedSignAlgorithmsCert()) {
						if (supportedGroup.equals(specifiedSignAlg.getDescription())) {
							atLeastOneFulfilled = true;
							break;
						}
					}
				}
				if (!atLeastOneFulfilled) {
					StringBuilder sb = new StringBuilder();
					for (var s : atLeastOneSignAlgorithms) {
						sb.append(s.getDescription() + ", ");
					}
					missingSignAlgorithms.add(
							"MICS is missing at least one of the following certificate signature algorithms for TLSv1.3: "
									+ sb.toString());
				}
			}
		} else {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "TLSv1.3 is not supported.");
		}

		var result = missingSignAlgorithms.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file contains all mandatory signature algorithms for certificates according to the application-specific requirements.");
			reportResult("TLS_ICS_10", result);
		} else {
			for (var s : missingSignAlgorithms) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The MICS file does not contain all mandatory signature algorithms for certificates according to the application-specific requirements.");
			reportResult("TLS_ICS_10", result, missingSignAlgorithms);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * Table 14 provides a maximum session duration not exceeding the maximum session duration defined by the
	 * application-specific requirements.
	 */
	private TestCaseRun TLS_ICS_11() {
		final String testCaseName = "TLS_ICS_11";
		final String testCaseDescription = "TLS_ICS_11 in TR-03116-TS";
		final String testCasePurpose
				= "Table 14 provides a maximum session duration not exceeding the maximum session duration defined by the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that Table 14 provides a maximum session duration not exceeding the maximum session duration defined by the application-specific requirements.");
		var micsSessionLifetime = mics.getSessionLifetime();
		var specifiedMaxLifetime = tlsSpecification.getTlsSessionLifetime();
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Maximum session duration according to the MICS file is " + micsSessionLifetime.toString());
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Maximum session lifetime according to application-specific requirements is "
				+ specifiedMaxLifetime.toString());
		var result = micsSessionLifetime.compareTo(specifiedMaxLifetime) < 0;
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The max. session duration specified in the MICS file is allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_11", result);
		} else {
			var message = "The MICS file session lifetime " + micsSessionLifetime.toString()
					+ " is greater than the specified max session lifetime " + specifiedMaxLifetime.toString();
			testRun.addStatusMessage(testCaseName);
			testRun.increaseErrorCount();
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The max session duration specified in the MICS file is not allowed according to the application-specific requirements.");
			reportResult("TLS_ICS_11", result, message);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * The order of the cipher suites as specified in Table 5 represents the correct priority: the less preferred cipher
	 * suites (e.g. due to a transitional rule) are put at the end of the list.
	 */
	private TestCaseRun TLS_ICS_12() {
		final String testCaseName = "TLS_ICS_12";
		final String testCaseDescription = "TLS_ICS_12 in TR-03116-TS";
		final String testCasePurpose
				= "The order of the cipher suites as specified in Table 5 represents the correct priority: the less preferred cipher suites (e.g. due to a transitional rule) are put at the end of the list.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that the order of the cipher suites as specified in Table 5 represents the correct priority: the less preferred cipher suites (e.g. due to a transitional rule) are put at the end of the list.");
		var wrongPriorityCipherSuites = new ArrayList<String>();

		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			var supportedCipherSuites = supportedTlsVersion.getSupportedCipherSuites();
			HashMap<String, CipherSuite> specifiedCipherSuites = null;
			if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
				specifiedCipherSuites = tlsSpecification.getTlsv1_3Spec().getCipherSuiteSupport();
			} else {
				specifiedCipherSuites = tlsSpecification.getTlsv1_2Spec().getCipherSuiteSupport();
			}

			// Skip last element.
			for (int i = 0; i < supportedCipherSuites.size() - 1; i++) {
				var supportedCipherSuite = supportedCipherSuites.get(i);
				var priorityRight = false;
				if (!specifiedCipherSuites.containsKey(supportedCipherSuite)
						|| !specifiedCipherSuites.containsKey(supportedCipherSuites.get(i + 1))) {
					var wrongCipherSuite = specifiedCipherSuites.containsKey(supportedCipherSuite)
							? supportedCipherSuites.get(i + 1)
							: supportedCipherSuite;
					wrongPriorityCipherSuites.add("The cipher suite " + wrongCipherSuite + " for TLS version "
							+ supportedTlsVersion.getTlsVersion().getName()
							+ " should not be supported and hence the priority is wrong.");
					continue;
				}
				var priority = specifiedCipherSuites.get(supportedCipherSuite).getPriority();
				var priorityOfNext = specifiedCipherSuites.get(supportedCipherSuites.get(i + 1)).getPriority();
				if (priority <= priorityOfNext) {
					priorityRight = true;
				}
				if (!priorityRight) {
					wrongPriorityCipherSuites.add("The priority of cipher suites for TLS version "
							+ supportedTlsVersion.getTlsVersion().getName() + ": " + supportedCipherSuite
							+ " is wrong. The cipher suite " + supportedCipherSuite
							+ " should be prioritized less than the cipher suite " + supportedCipherSuites.get(i + 1));
				}
			}
		}

		var result = wrongPriorityCipherSuites.isEmpty();
		if (result) {
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The order of the cipher suites as specified in Table 5 represents the correct priority.");
			reportResult("TLS_ICS_12", result);
		} else {
			for (var s : wrongPriorityCipherSuites) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logger.info(
					MICSVerifier.LOGGER_COMPONENT + "The order of the cipher suites as specified in Table 5 does not represent the correct priority.");
			reportResult("TLS_ICS_12", result, wrongPriorityCipherSuites);
		}
		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	private void reportResult(final String testCase, final Boolean result, final List<String> additionalInformation) {
		reportResult(testCase, result, additionalInformation.toArray(String[]::new));
	}

	private void reportResult(final String testCase, final Boolean result, final String... additionalInformation) {
		var reportMessage = getResultString(testCase, result, additionalInformation);
		reportResult(reportMessage);
	}

	private void reportResult(final String reportMessage) {
		logger.info(reportMessage);
	}

	private static String getResultString(final String testCase, final Boolean result,
			final String... additionalInformation) {
		var sb = new StringBuilder();
		sb.append("[" + testCase + "]: ");
		var resultString = result ? "Successful" : "Failure";
		sb.append(resultString);
		for (var s : additionalInformation) {
			sb.append(System.lineSeparator());
			sb.append(s);
			sb.append(";");
		}
		return sb.toString();
	}

}
