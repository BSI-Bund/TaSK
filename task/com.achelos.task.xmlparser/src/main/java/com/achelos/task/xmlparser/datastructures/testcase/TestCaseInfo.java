package com.achelos.task.xmlparser.datastructures.testcase;

import java.util.List;

import generated.jaxb.configuration.TestCase;

/**
 * Internal Data Structure containing information about a Test Case Specification.
 */
public class TestCaseInfo {
	/**
	 * The ID of the test case.
	 */
	public String id;
	/**
	 * The Version of the test case specification.
	 */
	public String version;
	/**
	 * A list of references regarding the test case specification.
	 */
	public List<String> references;
	/**
	 * A list of test profile identifier for this test case.
	 */
	public List<String> profileIds;


	private TestCaseInfo(final String id, final String version, final List<String> references,
			final List<String> profileIds) {
		this.id = id;
		this.version = version;
		this.references = references;
		this.profileIds = profileIds;
	}

	/**
	 * Parse an JAXB internal representation of a TestCase XML file into an internal data structure holding the same information.
	 * @return An internal data structure containing the Test Case Specification information.
	 */
	public static TestCaseInfo parseFromJaxb(final TestCase testCaseItem) {
		var id = testCaseItem.getId();
		var version = testCaseItem.getVersion();
		var references = testCaseItem.getReference();
		var profileIds = testCaseItem.getProfile();

		return new TestCaseInfo(id, version, references, profileIds);
	}

	@Override
	public String toString() {
		var separator = ", ";

		StringBuilder sb = new StringBuilder();
		sb.append("ID = ");
		sb.append(id);
		sb.append(separator);
		sb.append("Version = ");
		sb.append(version);
		sb.append(separator);
		sb.append("References = ");
		sb.append(references.toString());
		sb.append(separator);
		sb.append("Profiles = ");
		sb.append(profileIds.toString());

		return sb.toString();
	}


}
