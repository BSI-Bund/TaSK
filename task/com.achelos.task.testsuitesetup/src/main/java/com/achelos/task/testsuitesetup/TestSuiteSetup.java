package com.achelos.task.testsuitesetup;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.configuration.MICSConfiguration;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.DateTimeUtils;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.runplanparsing.RunPlanParser;

import generated.jaxb.testrunplan.DUTCapabilities;
import generated.jaxb.testrunplan.TestRunPlan;


/**
 * Class handling the preparation and generation of a Test Run Plan.
 */
public class TestSuiteSetup {

	private TestSuiteSetup() {

	}

	/**
	 * Execute the test suite setup, i.e. the generation of the TRP.
	 *
	 * @param reportDirectory test report path where all reports and log files shall be stored.
	 * @return test run plan File, or null if not successful.
	 */
	public static File executeTestSuiteSetup(final MICS mics, final File micsFile,
			final MICSConfiguration configuration,
			final String reportDirectory) {

		var logger = LoggingConnector.getInstance();

		var testRunPlan = new TestRunPlan();

		// TestRunPlan Generation Time
		testRunPlan.setTestRunPlanGenerationTime(getCurrentTime());

		// Test Configuration
		testRunPlan.setTestConfiguration(getTestConfiguration(mics, configuration));

		// MICS Info
		testRunPlan.setMICSInfo(getMICSInfo(mics, micsFile));

		// TLS Configuration
		var tlsConfig = ParameterInitialization.getTlsConfiguration(mics, configuration);
		testRunPlan.setTlsConfiguration(tlsConfig);

		// Test Cases
		var testCases = TestCaseMapper.getTestCases(mics, configuration.testCases);
		testRunPlan.setTestCases(testCases);

		try {
			var testRunPlanFile = Paths.get(reportDirectory, "TestRunPlan.xml")
					.toFile();
			RunPlanParser.printRunPlan(testRunPlan, testRunPlanFile);
			return testRunPlanFile;
		} catch (Exception e) {
			logger.error("Test Suite Setup: Unable to generate TestRunPlan File.", e);
			throw e;
		}

	}

	private static String getCurrentTime() {
		return DateTimeUtils.getISOFormattedTimeStamp();
	}

	private static TestRunPlan.TestConfiguration getTestConfiguration(final MICS mics, final MICSConfiguration config) {
		var testConfiguration = new TestRunPlan.TestConfiguration();

		// Set the DUT Application Type and Information
		setDUTApplicationType(testConfiguration, mics);

		// DUTCapabilities
		var dutCapabilityList = getDUTCapabilities(mics.getProfiles());
		if (!dutCapabilityList.isEmpty()) {
			var dutCapabilities = new DUTCapabilities();
			var containedlist = dutCapabilities.getCapability();
			containedlist.addAll(dutCapabilityList);
			testConfiguration.setDUTCapabilities(dutCapabilities);
		}

		return testConfiguration;
	}

	private static TestRunPlan.MICSInfo getMICSInfo(final MICS mics, final File micsFile) {
		var micsInfo = new TestRunPlan.MICSInfo();
		micsInfo.setName(mics.getTitle());
		micsInfo.setDescription(mics.getDescription());
		micsInfo.setPathToFile(micsFile.getAbsolutePath());
		return micsInfo;
	}

	private static List<String> getDUTCapabilities(List<String> micsProfiles) {
		List<String> dutCapabilities = new ArrayList<>();
		for (var dutCapability : com.achelos.task.xmlparser.datastructures.testrunplan.DUTCapabilities.values()) {
			if (micsProfiles.contains(dutCapability.getProfileName())){
				dutCapabilities.add(dutCapability.getProfileName());
			}
		}
		return dutCapabilities;
	}

	private static void setDUTApplicationType(TestRunPlan.TestConfiguration testConfiguration, final MICS mics) {
		var dUTApplicationType = mics.getApplicationType();
		testConfiguration.setApplicationType(dUTApplicationType);

		if (dUTApplicationType.contains("TR-03108-1-EMSP")) {
			testConfiguration.setStartTLS(mics.useStartTls());
		}
		if (dUTApplicationType.contains("CLIENT")) {
			testConfiguration.setRMIURL(mics.getDutRMIURL());
			testConfiguration.setRMIPort(mics.getDutRMIPort());
			if (dUTApplicationType.contains("EID-CLIENT")) {
				testConfiguration.setEIDClientPort(mics.getDutEIDClientPort());
			}

		} else if (dUTApplicationType.contains("SERVER")) {
			testConfiguration.setURL(mics.getServerURL());
			testConfiguration.setPort(mics.getServerPort());
			if (mics.getDutRMIURL() != null && !mics.getDutRMIURL().isBlank()) {
				testConfiguration.setRMIURL(mics.getDutRMIURL());
				testConfiguration.setRMIPort(mics.getDutRMIPort());
			}
		} else {
			throw new RuntimeException("DUT is of unknown type: " + dUTApplicationType);
		}
	}

}
