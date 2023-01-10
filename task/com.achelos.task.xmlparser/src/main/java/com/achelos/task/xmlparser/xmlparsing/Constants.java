package com.achelos.task.xmlparser.xmlparsing;

import java.io.InputStream;


/**
 * Class containing Constants for the XmlParser module. Mostly XSD Schema Resource paths.
 */
class Constants {
	/**
	 * Path to TLSSpecification.xsd.
	 */
	static final String RESOURCE_TLS_SPEC_XSD = "schemas/configuration/TLSSpecification.xsd";
	/**
	 * Path to TestProfiles.xsd.
	 */
	static final String RESOURCE_TEST_PROFILE_XSD = "schemas/configuration/TestProfiles.xsd";
	/**
	 * Path to ApplicationMapping.xsd.
	 */
	static final String RESOURCE_APP_MAPPING_XSD = "schemas/configuration/ApplicationMapping.xsd";
	/**
	 * Path to TestCase.xsd.
	 */
	static final String RESOURCE_TESTCASE_XSD = "schemas/configuration/TestCase.xsd";
	/**
	 * Path to GlobalConfig.xsd.
	 */
	static final String RESOURCE_GLOBAL_CONFIG_XSD = "schemas/configuration/GlobalConfig.xsd";
	/**
	 * Path to TlsConfigurationData.xsd.
	 */
	static final String RESOURCE_TLS_CONFIG_DATA_XSD = "schemas/configuration/TlsConfigurationData.xsd";
	/**
	 * Path to MICS.xsd.
	 */
	static final String RESOURCE_MICS_XSD = "schemas/input/MICS.xsd";
	/**
	 * TestRunPlan.xsdTLSSpecification.xsd.
	 */
	static final String RESOURCE_TEST_RUN_PLAN_XSD = "schemas/testrunplan/TestRunPlan.xsd";
	/**
	 * TestRunPlan.xsdTLSSpecification.xsd.
	 */
	static final String RESOURCE_TASK_REPORT_XSD = "schemas/output/TaSKReport.xsd";

	/**
	 * Return the Resource provided as a InputStream
	 *
	 * @param resourcePath The resource to provide as InputStream.
	 * @return an InputStream containing the resource.
	 * @throws IllegalArgumentException if the resource is unknown.
	 */
	static InputStream getResourceAsStream(final String resourcePath) {

		if (resourcePath.equals(RESOURCE_TLS_SPEC_XSD) || resourcePath.equals(RESOURCE_TEST_PROFILE_XSD)
				|| resourcePath.equals(RESOURCE_APP_MAPPING_XSD) || resourcePath.equals(RESOURCE_TESTCASE_XSD)
				|| resourcePath.equals(RESOURCE_GLOBAL_CONFIG_XSD) || resourcePath.equals(RESOURCE_TLS_CONFIG_DATA_XSD)
				|| resourcePath.equals(RESOURCE_MICS_XSD) || resourcePath.equals(RESOURCE_TEST_RUN_PLAN_XSD)
				|| resourcePath.equals(RESOURCE_TASK_REPORT_XSD)) {
			return Constants.class.getClassLoader().getResourceAsStream(resourcePath);
		}

		throw new IllegalArgumentException("XMLParser: Unknown Resource Path provided.");

	}

	/**
	 * Hidden Constructor.
	 */
	private Constants() {
		// Empty.
	}
}
