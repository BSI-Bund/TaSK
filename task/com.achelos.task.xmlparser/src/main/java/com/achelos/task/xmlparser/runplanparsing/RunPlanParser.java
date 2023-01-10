package com.achelos.task.xmlparser.runplanparsing;

import java.io.File;

import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.testrunplan.TestRunPlanData;
import com.achelos.task.xmlparser.xmlparsing.XmlParsing;
import com.achelos.task.xmlparser.xmlparsing.XmlPrinting;

import generated.jaxb.testrunplan.TestRunPlan;

/**
 * Helper class used to parse a Test Run Plan from an XML file into an internal data structure representation.
 */
public class RunPlanParser {

	// Hide Constructor.
	private RunPlanParser() {
		// Do nothing.
	}

	/**
	 * Parse a TestRunPlan from an XML file and return the internal data structure representation of it.
	 *
	 * @param runPlanFile TestRunPlan in XML file.
	 * @return the internal data structure representation of TestRunPlan
	 */
	public static TestRunPlanData parseRunPlan(final File runPlanFile) {
		var logger = LoggingConnector.getInstance();
		logger.debug("RunPlan Parser: Trying to unmarshall the test run plan XML file: " + runPlanFile.getAbsolutePath());
		var rawRunPlan = XmlParsing.unmarshallTestRunPlan(runPlanFile);
		logger.tellLogger(BasicLogger.MSG_TESTRUNPLAN, rawRunPlan);
		var testRunPlanData = TestRunPlanData.parseFromJaxb(rawRunPlan);
		verifyRunPlan(testRunPlanData);
		return testRunPlanData;
	}

	/**
	 * Print a TestRunPlan into an XML file.
	 *
	 * @param runPlan {@link TestRunPlan} to write into XML file.
	 * @param fileToWrite file to write the XML report to.
	 */
	public static void printRunPlan(final TestRunPlan runPlan, final File fileToWrite) {
		try {
			XmlPrinting.printTestRunPlanXml(runPlan, fileToWrite);
		} catch (Exception e) {
			throw new RuntimeException("TestRunPlan printing: Unable to print the test run plan XML file.", e);
		}
	}

	/**
	 * Do some checks on the TestRunplan e.g. to verify some information is always present.
	 * @param trpData the TestRunplan data to verify
	 * @throws RuntimeException If an error was found in the TRP.
	 */
	private static void verifyRunPlan(TestRunPlanData trpData) {
		/*
		 * This assures, that a PSK is set, if a TLSv1.2 Ciphersuite with PSK is supported.
		 * For TLSv1.3 another check should be added.
		 */
		if (!trpData.getSupportedPSKCipherSuites(TlsVersion.TLS_V1_2).isEmpty()) {
			var pskValue = trpData.getPSKValue();
			if (pskValue == null || pskValue.length == 0) {
				throw new RuntimeException("TestRunplan contains PSK ciphersuites for TLS version v1.2, but no PSK value.");
			}
		}
	}
}
