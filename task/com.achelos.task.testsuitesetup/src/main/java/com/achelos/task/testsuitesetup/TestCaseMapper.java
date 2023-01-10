package com.achelos.task.testsuitesetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.datastructures.testcase.TestCaseInfo;

import generated.jaxb.testrunplan.TestRunPlan.TestCases;


class TestCaseMapper {

	/**
	 * Hidden Constructor.
	 */
	private TestCaseMapper() {
		// Empty.
	}

	public static TestCases getTestCases(final MICS mics, final List<TestCaseInfo> testCasesList) {

		var testCases = new TestCases();

		var testCasesBeforeOrdering = new ArrayList<String>();

		var micsProfiles = mics.getProfiles();
		for (var testcase : testCasesList) {
			boolean include = true;
			for (var profileId : testcase.profileIds) {
				if (!micsProfiles.contains(profileId)) {
					include = false;
					break;
				}
			}
			if (include) {
				testCasesBeforeOrdering.add(testcase.id);
			}

		}

		// Order TestCases.
		Collections.sort(testCasesBeforeOrdering, (left, right) -> {
			// Generally: First GP, then FR, then CH TestCases. In these test cases we order
			// lexicographically.
			if (left.contains("_GP_") && (right.contains("_FR_") || right.contains("_CH_"))
					|| left.contains("_FR_") && right.contains("_CH_")) {
				return -1;
			}
			if (right.contains("_GP_") && (left.contains("_FR_") || left.contains("_CH_"))
					|| right.contains("_FR_") && left.contains("_CH_")) {
				return 1;
			} else {
				return left.compareTo(right);
			}
			// -1 - less than, 1 - greater than, 0 - equal, all inverse for descending
			// return lhs.customInt > rhs.customInt ? -1 : (lhs.customInt < rhs.customInt) ?
			// 1 : 0;
		});
		var testCaseList = testCases.getTestCase();
		testCaseList.addAll(testCasesBeforeOrdering);

		return testCases;
	}

}
