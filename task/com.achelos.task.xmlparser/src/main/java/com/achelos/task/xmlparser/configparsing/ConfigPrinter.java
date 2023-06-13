package com.achelos.task.xmlparser.configparsing;

import java.util.HashMap;
import java.util.List;

import com.achelos.task.xmlparser.datastructures.applicationmapping.AppMapping;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.testcase.TestCaseInfo;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;


/**
 * Helper class used to get String representations of configuration objects.
 */
public class ConfigPrinter {

	// Hide Constructor.
	private ConfigPrinter() {
		// Empty.
	}

	/**
	 * Returns a String representation of the provided GlobalConfig data structure.
	 *
	 * @param globalConfig GlobalConfig data structure
	 * @return a String representation of the provided GlobalConfig data structure
	 */
	public static String printGlobalConfig(final HashMap<String, GlobalConfigParameter> globalConfig) {
		StringBuilder configAsString = new StringBuilder();
		StringHelper.appendHashMapToStringBuilder(configAsString, "GlobalConfig", globalConfig, 0);
		return configAsString.toString();
	}

	/**
	 * Returns a String representation of the provided TlsSpecification data structure.
	 *
	 * @param tlsSpec TlsSpecification data structure
	 * @return a String representation of the provided TlsSpecification data structure
	 */
	public static String printTlsSpecification(final TlsSpecification tlsSpec) {
		return tlsSpec.toString();
	}

	/**
	 * Returns a String representation of the provided TestProfiles data structure.
	 *
	 * @param testProfiles TestProfiles data structure
	 * @return a String representation of the provided TestProfiles data structure
	 */
	public static String printTestProfiles(final List<String> testProfiles) {
		StringBuilder testProfilesAsString = new StringBuilder();
		StringHelper.appendListToStringBuilder(testProfilesAsString, "Available TestProfiles", testProfiles, 0);
		return testProfilesAsString.toString();
	}

	/**
	 * Returns a String representation of the provided testCases data structure.
	 *
	 * @param testCases test cases data structure
	 * @return a String representation of the provided testCases data structure
	 */
	public static String printTestCaseInfo(final List<TestCaseInfo> testCases) {
		StringBuilder testCasesAsString = new StringBuilder();
		StringHelper.appendListToStringBuilder(testCasesAsString, "TestCases", testCases, 0);
		return testCasesAsString.toString();
	}

	/**
	 * Returns a String representation of the provided AppMapping data structure.
	 *
	 * @param applicationMapping AppMapping data structure
	 * @return a String representation of the provided AppMapping data structure
	 */
	public static String printApplicationMapping(final AppMapping applicationMapping) {
		return applicationMapping.toString();
	}

}
