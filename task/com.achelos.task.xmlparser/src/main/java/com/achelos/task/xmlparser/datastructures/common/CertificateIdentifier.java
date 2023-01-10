package com.achelos.task.xmlparser.datastructures.common;

import generated.jaxb.input.TLSCertificates.CertificateChain.Certificate;
import generated.jaxb.testrunplan.TestRunPlan;

/**
 * An internal data structure class representing the Identifier of a Certificate.
 */
public class CertificateIdentifier {

	/**
	 * Enum values for the hierarchy level of the certificate.
	 */
	public enum CertificateType {
		ROOT_CA("RootCACertificate"),
		INTERMEDIATE_CA("IntermediateCertificate"),
		END_ENTITY("EndEntityCertificate");

		private String certificateTypeName;

		CertificateType(final String certificateTypeName) {
			this.certificateTypeName = certificateTypeName;
		}

		/**
		 * Returns a string representation of the hierarchy level.
		 * @return string representation of the hierarchy level.
		 */
		public String getText() {
			return certificateTypeName;
		}

		/**
		 * Get the CertificateType enum value from the string representation of the hierarchy level.
		 * @return the CertificateType enum value from the string representation of the hierarchy level.
		 */
		public static CertificateType fromCertificateTypeName(final String certificateTypeName) {
			for (var certType : CertificateType.values()) {
				if (certType.certificateTypeName.equalsIgnoreCase(certificateTypeName)) {
					return certType;
				}
			}
			throw new IllegalArgumentException("Unknown certificateTypeName: " + certificateTypeName);
		}

	}

	String subject;
	byte[] fingerprint;
	CertificateType certType;

	private CertificateIdentifier(final String subject, final byte[] fingerprint, final CertificateType certType) {
		this.subject = subject;
		this.fingerprint = fingerprint;
		this.certType = certType;
	}

	/**
	 * Parse the Certificate Identifier data structure from a raw JAXB internal MICS structure.
	 * @return a CertificateIdentifier holding the same information as the JAXB MICS structure provided.
	 */
	public static CertificateIdentifier parseFromMICSJaxb(final Certificate cert) {
		if (cert == null) {
			throw new IllegalArgumentException(
					"Unable to create CertificateIdentifier Object: Illegal Argument \"null\".");
		}
		var certType = CertificateType.fromCertificateTypeName(cert.getType());
		return new CertificateIdentifier(cert.getSubject(), cert.getFingerprint(), certType);
	}

	/**
	 * Parse the Certificate Identifier data structure from a raw JAXB internal Test Run Plan structure.
	 * @return a CertificateIdentifier holding the same information as the JAXB Test Run Plan structure provided.
	 */
	public static CertificateIdentifier
			parseFromTRPJaxb(final TestRunPlan.TlsConfiguration.TLSCertificates.CertificateChain.Certificate cert) {
		if (cert == null) {
			throw new IllegalArgumentException(
					"Unable to create CertificateIdentifier Object: Illegal Argument \"null\".");
		}
		var certType = CertificateType.fromCertificateTypeName(cert.getType());
		return new CertificateIdentifier(cert.getSubject(), cert.getFingerprint(), certType);
	}

	/**
	 * Return the Subject of the Certificate.
	 * @return the Subject of the Certificate.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Return the Fingerprint of the Certificate.
	 * @return the Fingerprint of the Certificate.
	 */
	public byte[] getFingerprint() {
		return fingerprint.clone();
	}

	/**
	 * Return the hierarchy level of the Certificate.
	 * @return the hierarchy level of the Certificate.
	 */
	public CertificateType getCertType() {
		return certType;
	}
}
