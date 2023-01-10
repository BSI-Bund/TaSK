package com.achelos.task.testsuitesetup;

import java.util.ArrayList;

import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.MICSConfiguration;
import com.achelos.task.xmlparser.datastructures.mics.MICS;

import generated.jaxb.testrunplan.KeyLengthType;
import generated.jaxb.testrunplan.TestRunPlan.TlsConfiguration;


class ParameterInitialization {

	/**
	 * Hidden Constructor.
	 */
	private ParameterInitialization() {
		// Empty.
	}

	public static TlsConfiguration getTlsConfiguration(final MICS mics, final MICSConfiguration config) {
		// TLS configuration data structure
		var tlsConfig = new TlsConfiguration();

		// List of Supported TLS versions
		var supportedTlsVersions = new ArrayList<String>();

		// Data Structures for Supported CipherSuites
		var cipherSuites = new TlsConfiguration.SupportedCipherSuites();
		var cipherSuiteList = cipherSuites.getCipherSuite();

		// Data Structures for Supported Groups
		var supportedGroups = new TlsConfiguration.TlsSupportedGroups();
		var supportedGroupsList = supportedGroups.getGroup();

		// Data Structures for Supported KeyLengths
		var supportedRSAKeyLengths = new KeyLengthType();
		var rsaKeyLengthsList = supportedRSAKeyLengths.getKeyLength();
		var supportedDSAKeyLengths = new KeyLengthType();
		var dsaKeyLengthsList = supportedDSAKeyLengths.getKeyLength();
		var supportedDHEKeyLengths = new KeyLengthType();
		var dheKeyLengthsList = supportedDHEKeyLengths.getKeyLength();

		// Data Structures for Supported Signature Algorithms;
		var supportedSignatureAlgorithms = new TlsConfiguration.SupportedSignatureAlgorithms();
		var signAlgoList = supportedSignatureAlgorithms.getSignatureAlgorithm();

		// Data Structures for Supported Signature Algorithms for Certificates;
		var supportedSignatureAlgorithmsCertificate = new TlsConfiguration.SupportedSignatureAlgorithmsCertificate();
		var signAlgoCertList = supportedSignatureAlgorithmsCertificate.getSignatureAlgorithm();

		// Data Structures for Supported TLS Extensions
		var supportedExtensions = new TlsConfiguration.SupportedExtensions();
		var extensionsList = supportedExtensions.getExtension();

		// Go through supported TLS versions and add information to above data
		// structures.
		for (var supportedTlsVersion : mics.getSupportedTlsVersions()) {
			// TLS version
			supportedTlsVersions.add(supportedTlsVersion.getTlsVersion().getName());

			// Supported CipherSuites
			for (var supportedCipherSuite : supportedTlsVersion.getSupportedCipherSuites()) {
				var cipherSuite = new TlsConfiguration.SupportedCipherSuites.CipherSuite();
				cipherSuite.setName(supportedCipherSuite);
				cipherSuite.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				cipherSuiteList.add(cipherSuite);
			}

			// Supported DH Groups
			for (var supportedGroup : supportedTlsVersion.getSupportedGroups()) {
				var group = new TlsConfiguration.TlsSupportedGroups.Group();
				group.setName(supportedGroup);
				group.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				group.setSupported(true);
				supportedGroupsList.add(group);
			}
			for (var namedCurve : config.allNamedCurves) {
				// Each of the Named Curves in the Config which is not supported
				// shall be listed as not supported in TRP.
				if (!supportedTlsVersion.getSupportedGroups().contains(namedCurve)) {
					var group = new TlsConfiguration.TlsSupportedGroups.Group();
					group.setName(namedCurve);
					group.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
					group.setSupported(false);
					supportedGroupsList.add(group);
				}
			}

			// Supported Key Lengths
			// RSA
			Integer minSupportedKeyLength = Integer.MAX_VALUE;
			if (supportedTlsVersion.getSupportedKeyLengths().containsKey("RSA")) {
				minSupportedKeyLength = supportedTlsVersion.getSupportedKeyLengths().get("RSA");
			}
			for (var keyLengthBigInt : config.allKeyLengths) {
				var keyLength = keyLengthBigInt.intValueExact();
				var keyLengthItem = new KeyLengthType.KeyLength();
				keyLengthItem.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				keyLengthItem.setValue(keyLength);
				keyLengthItem.setSupported(keyLength >= minSupportedKeyLength);
				rsaKeyLengthsList.add(keyLengthItem);
			}

			// DSA
			minSupportedKeyLength = Integer.MAX_VALUE;
			if (supportedTlsVersion.getSupportedKeyLengths().containsKey("DSA")) {
				minSupportedKeyLength = supportedTlsVersion.getSupportedKeyLengths().get("DSA");
			}
			for (var keyLengthBigInt : config.allKeyLengths) {
				var keyLength = keyLengthBigInt.intValueExact();
				var keyLengthItem = new KeyLengthType.KeyLength();
				keyLengthItem.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				keyLengthItem.setValue(keyLength);
				keyLengthItem.setSupported(keyLength >= minSupportedKeyLength);
				dsaKeyLengthsList.add(keyLengthItem);
			}

			// DHE
			minSupportedKeyLength = Integer.MAX_VALUE;
			if (supportedTlsVersion.getSupportedKeyLengths().containsKey("DHE")) {
				minSupportedKeyLength = supportedTlsVersion.getSupportedKeyLengths().get("DHE");
			}
			for (var keyLengthBigInt : config.allKeyLengths) {
				var keyLength = keyLengthBigInt.intValueExact();
				var keyLengthItem = new KeyLengthType.KeyLength();
				keyLengthItem.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				keyLengthItem.setValue(keyLength);
				keyLengthItem.setSupported(keyLength >= minSupportedKeyLength);
				dheKeyLengthsList.add(keyLengthItem);
			}

			// Supported Signature Algorithms
			for (var supportedSignAlgo : supportedTlsVersion.getSupportedSignAlgorithms()) {
				var signAlgo = new TlsConfiguration.SupportedSignatureAlgorithms.SignatureAlgorithm();
				if (supportedTlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
					signAlgo.setName(supportedSignAlgo);
				} else {
					var splits = supportedSignAlgo.trim().split("With");
					if (splits.length != 2) {
						throw new IllegalArgumentException(
								"Illegal Signature Algorithm provided: " + supportedSignAlgo);
					}
					signAlgo.setName(splits[0].toLowerCase());
					signAlgo.setHashName(splits[1].toLowerCase());
				}
				signAlgo.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				signAlgoList.add(signAlgo);
			}

			// Supported Signature Algorithms for Certificate
			for (var supportedSignAlgo : supportedTlsVersion.getSupportedSignAlgorithmsCert()) {
				var signAlgo = new TlsConfiguration.SupportedSignatureAlgorithmsCertificate.SignatureAlgorithm();
				signAlgo.setName(supportedSignAlgo);
				signAlgo.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				signAlgoCertList.add(signAlgo);
			}

			// Supported TLS Extensions
			for (var extension : supportedTlsVersion.getSupportedTlsExtensions()) {
				var supportedExtension = new TlsConfiguration.SupportedExtensions.Extension();
				supportedExtension.setTlsVersion(supportedTlsVersion.getTlsVersion().getName());
				supportedExtension.setValue(extension);
				extensionsList.add(supportedExtension);
			}
		}

		// TLS versions
		var tlsVersions = new TlsConfiguration.TlsVersions();
		var tlsVersionList = tlsVersions.getTlsVersion();
		for (var tlsVersion : config.allTlsVersions) {
			var version = new TlsConfiguration.TlsVersions.TlsVersion();
			version.setValue(tlsVersion);
			version.setSupported(supportedTlsVersions.contains(tlsVersion));
			tlsVersionList.add(version);
		}

		tlsConfig.setTlsVersions(tlsVersions);

		// Supported CipherSuites
		tlsConfig.setSupportedCipherSuites(cipherSuites);

		// Supported EC and DH Groups
		tlsConfig.setTlsSupportedGroups(supportedGroups);

		// Supported Signature Algorithms
		tlsConfig.setSupportedSignatureAlgorithms(supportedSignatureAlgorithms);

		// Supported Signature Algorithms Certificate
		tlsConfig.setSupportedSignatureAlgorithmsCertificate(supportedSignatureAlgorithmsCertificate);

		// Supported Key Lengths
		var keyLengths = new TlsConfiguration.KeyLengths();
		keyLengths.setRSA(supportedRSAKeyLengths);
		keyLengths.setDSA(supportedDSAKeyLengths);
		keyLengths.setDHE(supportedDHEKeyLengths);
		tlsConfig.setKeyLengths(keyLengths);

		// TLS With PSK
		if (mics.isPskAvailable()) {
			var tlsWithPSK = new TlsConfiguration.TlsWithPSK();
			tlsWithPSK.setPSKValue(mics.getPskValue());
			var pskIdentityHint = mics.getPskIdentityHint();
			if (pskIdentityHint != null && pskIdentityHint.length() != 0) {
				tlsWithPSK.setPSKIdentityHintValue(pskIdentityHint);
			}
			tlsConfig.setTlsWithPSK(tlsWithPSK);
		}

		// Supported TLS Extensions
		tlsConfig.setSupportedExtensions(supportedExtensions);

		// 0-RTT Support
		tlsConfig.setZeroRTTSupport(mics.getZeroRttData());

		// Information On TR-03145 Certification
		if (mics.getInformationOnTR03145Certification() != null) {
			var infoOnCertification = new TlsConfiguration.InformationOnTR03145Certification();
			infoOnCertification
					.setBSICertificateNumber(mics.getInformationOnTR03145Certification().bSICertificateNumber);
			infoOnCertification.setNameOfTheCA(mics.getInformationOnTR03145Certification().nameOfTheCA);
			infoOnCertification.setSubject(mics.getInformationOnTR03145Certification().subject);
			infoOnCertification
					.setSubjectKeyIdentifier(mics.getInformationOnTR03145Certification().subjectKeyIdentifier);
			infoOnCertification.setValidityNotAfter(mics.getInformationOnTR03145Certification().validityNotAfter);
			infoOnCertification.setValidityNotBefore(mics.getInformationOnTR03145Certification().validityNotBefore);
			tlsConfig.setInformationOnTR03145Certification(infoOnCertification);
		}

		// Max. Session Lifetime
		tlsConfig.setTlsSessionLifetime(mics.getSessionLifetime().toString());

		// TLS Certificates
		if (mics.getCertificateChain() != null && !mics.getCertificateChain().isEmpty()) {
			var tlsCertificates = new TlsConfiguration.TLSCertificates();
			var tlsCertChain = new TlsConfiguration.TLSCertificates.CertificateChain();
			var certChainList = tlsCertChain.getCertificate();
			for (var cert : mics.getCertificateChain()) {
				var certItem = new TlsConfiguration.TLSCertificates.CertificateChain.Certificate();
				certItem.setFingerprint(cert.getFingerprint());
				certItem.setSubject(cert.getSubject());
				certItem.setType(cert.getCertType().getText());
				certChainList.add(certItem);
			}
			tlsCertificates.setCertificateChain(tlsCertChain);

			var tlsDomainList = new TlsConfiguration.TLSCertificates.ServerDomains();
			tlsDomainList.getSubDomain().addAll(mics.getDomainNameList());
			tlsCertificates.setServerDomains(tlsDomainList);

			tlsConfig.setTLSCertificates(tlsCertificates);
		}

		return tlsConfig;
	}
}
