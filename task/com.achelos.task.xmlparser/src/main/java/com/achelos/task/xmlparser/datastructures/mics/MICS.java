package com.achelos.task.xmlparser.datastructures.mics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.xmlparser.configparsing.StringHelper;
import com.achelos.task.xmlparser.datastructures.common.ApplicationSpecificData;
import com.achelos.task.xmlparser.datastructures.common.CertificateIdentifier;
import com.achelos.task.xmlparser.datastructures.common.TR03145CertificationInfo;

import generated.jaxb.input.ICS;
import jakarta.xml.bind.DatatypeConverter;

/**
 * Internal data structure representing the MICS.
 */
public class MICS {

	private String title;
	private String version;
	private String description;
	private String applicationType;
	private String serverURL;
	private String serverPort;
	private String dutExecutable;
	private String dutCallArgumentsConnect;
	private String dutCallArgumentsReconnect;
	private Integer dutEIDClientPort;
	private String respectiveGuideline;
	private List<String> profiles;
	private List<TlsVersionSupport> supportedTlsVersions;
	private TR03145CertificationInfo informationOnTR03145Certification;
	private Duration sessionLifetime;
	private Boolean zeroRttData;
	private List<CertificateIdentifier> certificateChain;
	private List<String> domainNameList;
	private String pskIdentityHint;
	private byte[] pskValue;
	private ApplicationSpecificData appSpecificData;

	private MICS() {

	}

	/**
	 * Parse MICS data structure from internal JAXB Class.
	 *
	 * @param rawMics the raw internal JAXB class.
	 * @return MICS data structure
	 */
	public static MICS parseFromJaxb(final ICS rawMics) {
		var mics = new MICS();

		// Title
		mics.title = rawMics.getTitle() != null ? rawMics.getTitle() : "";

		// Version
		mics.version = rawMics.getVersion() != null ? rawMics.getVersion() : "";

		// Description
		mics.description = rawMics.getDescription() != null ? rawMics.getDescription() : "";

		// Application under Test
		var aut = rawMics.getApplicationUnderTest();
		if (aut == null) {
			throw new IllegalArgumentException(
					"Application under test is not provided in the MICS file or could not be parsed.");
		}
		mics.applicationType = aut.getApplicationType() != null ? aut.getApplicationType() : "";
		mics.respectiveGuideline = aut.getRespectiveTechnicalGuideline() != null ? aut.getRespectiveTechnicalGuideline()
				: "";
		mics.serverPort = aut.getPort() != null ? aut.getPort() : "";
		mics.serverURL = aut.getURL() != null ? aut.getURL() : "";
		mics.dutExecutable = aut.getDUTExecutable() != null ? aut.getDUTExecutable() : "";
		if (aut.getDUTCallArguments() != null) {
			mics.dutCallArgumentsConnect = aut.getDUTCallArguments().getStartConnectionArguments();
			mics.dutCallArgumentsReconnect = aut.getDUTCallArguments().getResumeConnectionArguments() != null
					? aut.getDUTCallArguments().getResumeConnectionArguments() : "";
			mics.dutEIDClientPort = aut.getDUTCallArguments().getEIDClientPort() != null ? aut.getDUTCallArguments().getEIDClientPort() : -1;
		} else {
			mics.dutCallArgumentsConnect = "";
			mics.dutCallArgumentsReconnect = "";
			mics.dutEIDClientPort = -1;
		}

		// Profiles
		mics.profiles = new ArrayList<>();
		if (rawMics.getProfiles() != null) {
			var allProfiles = rawMics.getProfiles();
			if (allProfiles.getProfile() != null) {
				mics.profiles.addAll(allProfiles.getProfile());
			}
		}

		// Information on TR03145 Certification
		if (rawMics.getInformationOnTR03145Certification() != null) {
			mics.informationOnTR03145Certification = TR03145CertificationInfo
					.parseFromMICSJaxb(rawMics.getInformationOnTR03145Certification());
		} else {
			mics.informationOnTR03145Certification = null;
		}

		// Connection Timeout
		try {
			mics.sessionLifetime = StringHelper.getDurationFromString(rawMics.getConnectionTimeout());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Illegal ConnectionTimeout: " + rawMics.getConnectionTimeout(), e);
		}

		mics.zeroRttData = rawMics.isZeroRTTData() != null ? rawMics.isZeroRTTData() : false;

		// TLS Certificates
		mics.domainNameList = new ArrayList<>();
		mics.certificateChain = new ArrayList<>();
		if (rawMics.getTLSCertificates() != null) {
			var tlsCertificates = rawMics.getTLSCertificates();
			if (tlsCertificates.getCertificateChain() != null) {
				for (var cert : tlsCertificates.getCertificateChain().getCertificate()) {
					mics.certificateChain.add(CertificateIdentifier.parseFromMICSJaxb(cert));
				}
			}
			if (tlsCertificates.getServerDomains() != null) {
				for (var domain : tlsCertificates.getServerDomains().getSubDomain()) {
					mics.domainNameList.add(domain);
				}
			}
		}

		// TLS with PSK Cipher Suites
		if (rawMics.getTLSWithPSKCipherSuites() != null) {
			mics.pskIdentityHint = rawMics.getTLSWithPSKCipherSuites().getPSKIdentityHintValue();
			mics.pskValue = rawMics.getTLSWithPSKCipherSuites().getPSKValue();
		} else {
			mics.pskIdentityHint = "";
			mics.pskValue = new byte[] { };
		}

		// Supported Cryptography
		mics.supportedTlsVersions = new ArrayList<>();
		if (rawMics.getSupportedCryptography() != null) {
			for (var tlsVersionSupport : rawMics.getSupportedCryptography().getSupportedTLSVersion()) {
				mics.supportedTlsVersions.add(TlsVersionSupport.parseFromJaxb(tlsVersionSupport));
			}
		}

		// ApplicationSpecificData
		// If rawMics.getApplicationSpecificData() is null, the resulting structure is
		// empty but usable.
		mics.appSpecificData = ApplicationSpecificData.parseFromMICSJaxb(rawMics.getApplicationSpecificData());

		return mics;
	}

	/**
	 * Returns a boolean indicating whether TR03145Certification information is present in this MICS.
	 * @return boolean indicating whether TR03145Certification information is present in this MICS.
	 */
	public boolean isTR03145CertificationPresent() {
		return informationOnTR03145Certification != null;
	}

	/**
	 * Returns a boolean indicating whether a Certificate Chain is present in this MICS.
	 * @return boolean indicating whether a Certificate Chain is present in this MICS.
	 */
	public boolean isCertificateChainPresent() {
		return !certificateChain.isEmpty();
	}

	/**
	 * Returns a boolean indicating whether PSK information is present in this MICS.
	 * @return boolean indicating whether PSK information is present in this MICS.
	 */
	public boolean isPskAvailable() {
		if ((pskIdentityHint != null && !pskIdentityHint.isBlank()) || (pskValue != null && pskValue.length != 0)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		var micsAsString = new StringBuilder("ICS:" + System.lineSeparator());
		// Title
		StringHelper.appendAttrToStringBuilder(micsAsString, "Title", title);
		// Description
		StringHelper.appendAttrToStringBuilder(micsAsString, "Description", description);
		// Application Type
		StringHelper.appendAttrToStringBuilder(micsAsString, "Application under Test", applicationType);
		// Guidelines
		StringHelper.appendAttrToStringBuilder(micsAsString, "Respective Guideline", respectiveGuideline);
		// Profiles
		StringHelper.appendListToStringBuilder(micsAsString, "Supported Profiles", profiles, 1);

		// Supported TLS versions
		for (var supportedTlsVersion : supportedTlsVersions) {
			micsAsString.append("\t Supported TLS version:" + System.lineSeparator());
			StringHelper.appendAttrToStringBuilder(micsAsString, "Version",
					supportedTlsVersion.getTlsVersion().getName(), 2);
			StringHelper.appendListToStringBuilder(micsAsString, "Supported CipherSuites",
					supportedTlsVersion.getSupportedCipherSuites(), 2);
			StringHelper.appendHashMapToStringBuilder(micsAsString, "Minimal Supported Key Lengths",
					supportedTlsVersion.getSupportedKeyLengths(), 2);
			StringHelper.appendListToStringBuilder(micsAsString, "Supported Elliptic Curves / DH Groups",
					supportedTlsVersion.getSupportedGroups(), 2);
			StringHelper.appendListToStringBuilder(micsAsString, "Supported Signature Algorithms",
					supportedTlsVersion.getSupportedSignAlgorithms(), 2);
			StringHelper.appendListToStringBuilder(micsAsString, "Supported Signature Algorithms for Certificates",
					supportedTlsVersion.getSupportedSignAlgorithmsCert(), 2);
			StringHelper.appendListToStringBuilder(micsAsString, "Supported TLS extensions",
					supportedTlsVersion.getSupportedTlsExtensions(), 2);
		}

		// Information on TR03145 Certification
		if (isTR03145CertificationPresent()) {
			micsAsString.append("\tTR03145 Certification:" + System.lineSeparator());
			StringHelper.appendAttrToStringBuilder(micsAsString, "Name of the CA",
					informationOnTR03145Certification.nameOfTheCA, 2);
			StringHelper.appendAttrToStringBuilder(micsAsString, "BSI Certificate Number",
					informationOnTR03145Certification.bSICertificateNumber, 2);
			StringHelper.appendAttrToStringBuilder(micsAsString, "Subject",
					informationOnTR03145Certification.subject, 2);
			StringHelper.appendAttrToStringBuilder(micsAsString, "Validity - Not Before",
					informationOnTR03145Certification.validityNotBefore, 2);
			StringHelper.appendAttrToStringBuilder(micsAsString, "Validity - Not After",
					informationOnTR03145Certification.validityNotAfter, 2);
			StringHelper.appendAttrToStringBuilder(micsAsString, "Subject Key Identifier",
					informationOnTR03145Certification.subjectKeyIdentifier, 2);
		}

		// TLSWithPSKCipherSuites
		if (isPskAvailable()) {
			micsAsString.append("\tPSK:" + System.lineSeparator());
			if (pskValue != null && pskValue.length != 0) {
				StringHelper.appendAttrToStringBuilder(micsAsString, "PSK Value",
						DatatypeConverter.printHexBinary(pskValue), 2);
			}
			if (pskIdentityHint != null && !pskIdentityHint.isBlank()) {
				StringHelper.appendAttrToStringBuilder(micsAsString, "PSK Identity Hint",
						pskIdentityHint, 2);
			}
		}

		// ConnectionTimeout
		StringHelper.appendAttrToStringBuilder(micsAsString, "Connection Timeout", sessionLifetime.toString());

		// Zero RTT Data
		StringHelper.appendAttrToStringBuilder(micsAsString, "Zero RTT Data", zeroRttData.toString());

		// TLS Certificates
		if (isCertificateChainPresent()) {
			micsAsString.append("\tTLS Certificates:" + System.lineSeparator());
			for (var cert : certificateChain) {
				micsAsString.append("\t\t Certificate:" + System.lineSeparator());
				StringHelper.appendAttrToStringBuilder(micsAsString, "Subject", cert.getSubject(), 3);
				StringHelper.appendAttrToStringBuilder(micsAsString, "Fingerprint",
						DatatypeConverter.printHexBinary(cert.getFingerprint()), 3);
			}
			StringHelper.appendListToStringBuilder(micsAsString, "(Sub-)Domains", domainNameList, 2);
		}

		return micsAsString.toString();
	}

	/**
	 * Return the Title of the MICS.
	 * @return the Title of the MICS.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return the Version of the MICS.
	 * @return the Version of the MICS.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Return the Description of the MICS.
	 * @return the Description of the MICS.
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * Return the ApplicationType of the MICS.
	 * @return the ApplicationType of the MICS.
	 */
	public String getApplicationType() {
		return applicationType;
	}

	/**
	 * Return the DUT Server URL of the MICS.
	 * @return the DUT Server URL of the MICS.
	 */
	public String getServerURL() {
		return serverURL;
	}

	/**
	 * Return the DUT Server Port of the MICS.
	 * @return the DUT Server Port of the MICS.
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * Return the DUT Client Executable of the MICS.
	 * @return the DUT Client Executable of the MICS.
	 */
	public String getDutExecutable() {
		return dutExecutable;
	}

	/**
	 * Return the DUT Client Call Argument for Connection stored in the MICS.
	 * @return the DUT Client Call Argument for Connection stored in the MICS.
	 */
	public String getDutCallArgumentsConnect() {
		return dutCallArgumentsConnect;
	}

	/**
	 * Return the DUT Client Call Argument for Reconnection stored in the MICS.
	 * @return the DUT Client Call Argument for Reconnection stored in the MICS.
	 */
	public String getDutCallArgumentsReconnect() {
		return dutCallArgumentsReconnect;
	}

	/**
	 * Return the DUTs value for eID-Client port stored in the MICS.
	 * @return the DUTs value for eID-Client port stored in the MICS.
	 */
	public Integer getDutEIDClientPort() {
		return dutEIDClientPort;
	}

	/**
	 * Return the respective Guideline stored in the MICS.
	 * @return the respective Guideline stored in the MICS.
	 */
	public String getRespectiveGuideline() {
		return respectiveGuideline;
	}

	/**
	 * Returns the list of Application Profiles stored in the MICS.
	 * @return the list of Application Profiles stored in the MICS.
	 */
	public List<String> getProfiles() {
		return new ArrayList<>(profiles);
	}

	/**
	 * Returns a list of supported Tls Versions stored in the MICS.
	 * @return a list of supported Tls Versions stored in the MICS.
	 */
	public List<TlsVersionSupport> getSupportedTlsVersions() {
		return new ArrayList<>(supportedTlsVersions);
	}

	/**
	 * Returns information on TR03145 Certification stored in the MICS.
	 * @return information on TR03145 Certification stored in the MICS.
	 */
	public TR03145CertificationInfo getInformationOnTR03145Certification() {
		return informationOnTR03145Certification;
	}

	/**
	 * Returns information on the Session Lifetime stored in the MICS.
	 * @return information on the Session Lifetime stored in the MICS.
	 */
	public Duration getSessionLifetime() {
		return sessionLifetime;
	}

	/**
	 * Returns information on the 0-RTT data stored in the MICS.
	 * @return information on the 0-RTT data stored in the MICS.
	 */
	public Boolean getZeroRttData() {
		return zeroRttData;
	}

	/**
	 * Returns the certificate chain information stored in the MICS.
	 * @return the certificate chain information stored in the MICS.
	 */
	public List<CertificateIdentifier> getCertificateChain() {
		return new ArrayList<>(certificateChain);
	}

	/**
	 * Returns a list of DUT domain names stored in the MICS.
	 * @return a list of DUT domain names stored in the MICS.
	 */
	public List<String> getDomainNameList() {
		return new ArrayList<>(domainNameList);
	}

	/**
	 * Returns the PSK Identity Hint stored in the MICS.
	 * @return the PSK Identity Hint stored in the MICS.
	 */
	public String getPskIdentityHint() {
		return pskIdentityHint;
	}

	/**
	 * Returns the PSK Value stored in the MICS.
	 * @return the PSK Value stored in the MICS.
	 */
	public byte[] getPskValue() {
		return pskValue.clone();
	}

	/**
	 * Return the Application Specific Data stored in the MICS.
	 * @return the Application Specific Data stored in the MICS.
	 */
	public ApplicationSpecificData getAppSpecificData() {
		return appSpecificData;
	}

}
