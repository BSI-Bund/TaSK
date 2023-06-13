package com.achelos.task.xmlparser.datastructures.common;

import generated.jaxb.input.InformationOnTR03145Certification;
import generated.jaxb.testrunplan.TestRunPlan;


/**
 * Internal data strucute representing information about TR03145 Certification.
 */
public class TR03145CertificationInfo {

	/**
	 * The Name of the Certifiied CA.
	 */
	public String nameOfTheCA;
	/**
	 * The BSI Certificate Number.
	 */
	public String bSICertificateNumber;
	/**
	 * The Subject of the certificate.
	 */
	public String subject;
	/**
	 * The Validity NotBefore value of the certificate.
	 */
	public String validityNotBefore;
	/**
	 * The Validity NotAfter value of the certificate.
	 */
	public String validityNotAfter;
	/**
	 * The Subject Key Identifier of the certificate.
	 */
	public String subjectKeyIdentifier;

	private TR03145CertificationInfo(final String nameOfTheCA, final String bSICertificateNumber, final String subject,
			final String validityNotBefore, final String validityNotAfter, final String subjectKeyIdentifier) {
		super();
		this.nameOfTheCA = nameOfTheCA;
		this.bSICertificateNumber = bSICertificateNumber;
		this.subject = subject;
		this.validityNotBefore = validityNotBefore;
		this.validityNotAfter = validityNotAfter;
		this.subjectKeyIdentifier = subjectKeyIdentifier;
	}

	/**
	 * Parse the Data Structure from JAXB generated unmarshalled class coming from the MICS file.
	 *
	 * @param rawTR03145CertInfo Raw object of jaxb generated data structure coming from unmarshalling a MICS XML file.
	 * @return Parsed Data Structure.
	 */
	public static TR03145CertificationInfo
			parseFromMICSJaxb(final InformationOnTR03145Certification rawTR03145CertInfo) {
		if (rawTR03145CertInfo == null) {
			throw new IllegalArgumentException(
					"An error occurred while creating TR03145Certification Info Object from the MICS file. Input \"null\" is not allowed.");
		}
		return new TR03145CertificationInfo(rawTR03145CertInfo.getNameOfTheCA(),
				rawTR03145CertInfo.getBSICertificateNumber(), rawTR03145CertInfo.getSubject(),
				rawTR03145CertInfo.getValidityNotBefore(), rawTR03145CertInfo.getValidityNotAfter(),
				rawTR03145CertInfo.getSubjectKeyIdentifier());
	}

	/**
	 * Parse the Data Structure from JAXB generated unmarshalled class coming from TestRunPlan.
	 *
	 * @param rawTR03145CertInfo Raw object of jaxb generated data structure coming from unmarshalling a TestRunPlan XML
	 * file.
	 * @return Parsed Data Structure.
	 */
	public static TR03145CertificationInfo
			parseFromTRPJaxb(final TestRunPlan.TlsConfiguration.InformationOnTR03145Certification rawTR03145CertInfo) {
		if (rawTR03145CertInfo == null) {
			throw new IllegalArgumentException(
					"An error occurred while creating TR03145Certification Info Object from TRP. Input \"null\" is not allowed.");
		}
		return new TR03145CertificationInfo(rawTR03145CertInfo.getNameOfTheCA(),
				rawTR03145CertInfo.getBSICertificateNumber(), rawTR03145CertInfo.getSubject(),
				rawTR03145CertInfo.getValidityNotBefore(), rawTR03145CertInfo.getValidityNotAfter(),
				rawTR03145CertInfo.getSubjectKeyIdentifier());
	}

	/**
	 * Get the Name of the CA according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return Name of the CA according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getCAName() {
		return nameOfTheCA;
	}

	/**
	 * Get the BSI certificate number according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return BSI certificate number according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getBSICertificateNumber() {
		return bSICertificateNumber;
	}

	/**
	 * Get the Subject according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return Subject according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Get the Validity NotBefore value according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return Validity NotBefore value according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getValidityNotBefore() {
		return validityNotBefore;
	}

	/**
	 * Get the Validity NotAfter value according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return Validity NotAfter value according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getValidityNotAfter() {
		return validityNotAfter;
	}

	/**
	 * Get the Subject Key Identifier according to Table 11 of the TR-03116-TS ICS document.
	 *
	 * @return Subject Key Identifier according to Table 11 of the TR-03116-TS ICS document.
	 */
	public String getSubjectKeyIdentifier() {
		return subjectKeyIdentifier;
	}

}
