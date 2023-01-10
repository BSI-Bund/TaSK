package com.achelos.task.xmlparser.datastructures.common;

/**
 * Internal data structure representing an 'Application Specific Data' Object.
 */
public class ApplicationSpecificData {

	private String testClientCertPath;
	private String testClientPrivateKeyPath;

	private ApplicationSpecificData() {
		testClientCertPath = "";
		testClientPrivateKeyPath = "";
	}

	/**
	 * Parse the JAXB internal representation of Application Specific Data coming from an MICS file into the internal data structure.
	 * @return the internal data structure containing the information of the provided Application Specific Data.
	 */
	public static ApplicationSpecificData parseFromMICSJaxb(
			final generated.jaxb.input.ApplicationSpecificData rawAppSpecData) {
		var appSpecificData = new ApplicationSpecificData();

		// If null return empty structure.
		if (rawAppSpecData == null) {
			return appSpecificData;
		}

		// Test Client Certificate
		if (rawAppSpecData.getTestClientCertificate() != null) {
			appSpecificData.testClientCertPath = rawAppSpecData.getTestClientCertificate().getCertificateFile();
			appSpecificData.testClientPrivateKeyPath = rawAppSpecData.getTestClientCertificate()
					.getCertificatePrivateKey();
		}

		return appSpecificData;
	}

	/**
	 * Parse the JAXB internal representation of Application Specific Data coming from an TestRunPlan file into the internal data structure.
	 * @return the internal data structure containing the information of the provided Application Specific Data.
	 */
	public static ApplicationSpecificData parseFromTRPJaxb(
			final generated.jaxb.testrunplan.AppSpecificData rawAppSpecData) {
		var appSpecificData = new ApplicationSpecificData();

		// If null return empty structure.
		if (rawAppSpecData == null) {
			return appSpecificData;
		}

		// Test Client Certificate
		if (rawAppSpecData.getTestClientCertificate() != null) {
			appSpecificData.testClientCertPath = rawAppSpecData.getTestClientCertificate().getCertificateFile();
			appSpecificData.testClientPrivateKeyPath = rawAppSpecData.getTestClientCertificate()
					.getCertificatePrivateKey();
		}

		return appSpecificData;
	}

	/**
	 * Returns the information whether a Test Client Certificate is set in the Application Specific Data object.
	 * @return information whether a Test Client Certificate is set in the Application Specific Data object.
	 */
	public boolean isTestClientCertSet() {
		return !testClientCertPath.isBlank();
	}

	/**
	 * Returns the Path to the Test Client Certificate which is set in the Application Specific Data object.
	 * @return the Path to Test Client Certificate which is set in the Application Specific Data object.
	 */
	public String getTestClientCertPath() {
		return testClientCertPath;
	}

	/**
	 * Returns the Path to the Test Client Certificate Private Key which is set in the Application Specific Data object.
	 * @return the Path to Test Client Certificate Private Key which is set in the Application Specific Data object.
	 */
	public String getTestClientPrivateKeyPath() {
		return testClientPrivateKeyPath;
	}
}
