package com.achelos.task.xmlparser.datastructures.testrunplan;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.management.InvalidAttributeValueException;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls12;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.constants.Constants;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.xmlparser.configparsing.StringHelper;
import com.achelos.task.xmlparser.datastructures.common.CertificateIdentifier;
import com.achelos.task.xmlparser.datastructures.common.TR03145CertificationInfo;

import generated.jaxb.testrunplan.TestRunPlan.TlsConfiguration;

/**
 * Internal data structure containing TLS configuration stored in a Test Run Plan file.
 */
public class RunPlanTlsConfiguration {

	private HashMap<TlsVersion, Boolean> tlsVersions;
	private HashMap<TlsVersion, List<TlsCipherSuite>> supportedCipherSuites;
	private HashMap<TlsVersion, List<TlsSignatureAlgorithmWithHash>> supportedSignatureAlgorithms;
	private List<TlsSignatureAlgorithmWithHashTls13> tls13SupportedSignatureAlgorithmsForCertificate;
	private HashMap<TlsVersion, List<TlsNamedCurves>> supportedGroups;
	private HashMap<TlsVersion, List<TlsNamedCurves>> notSupportedGroups;
	private HashMap<TlsVersion, List<TlsExtensionTypes>> supportedExtensions;
	private List<CertificateIdentifier> certificateChain;
	private List<String> domainNameList;
	private TR03145CertificationInfo tr03145CertificationInfo;
	private RunPlanTlsKeyLengths keyLengthsSupport;
	private Duration tlsSessionLifetime;
	private byte[] pSKValue;
	private String pSKIdentityHint;
	private String pSKIdentity;
	private boolean is0RTTSupported;

	private RunPlanTlsConfiguration() {
		// Empty Constructor.
	}

	/**
	 * Parse the Tls Configuration stored in a Test Run Plan file from its JAXB representation into an internal data structure.
	 * @return an internal data structure containing the Tls Configuration information of a Test Run Plan.
	 */
	public static RunPlanTlsConfiguration parseFromJaxb(final TlsConfiguration tlsConfig) {
		var runPlanConfig = new RunPlanTlsConfiguration();

		if (tlsConfig == null) {
			throw new IllegalArgumentException("The test run plans TLS configuration is \"null\"!");
		}

		// TLS versions
		if (tlsConfig.getTlsVersions() == null || tlsConfig.getTlsVersions().getTlsVersion() == null) {
			throw new IllegalArgumentException("List of the TLS versions in TLS configuration is \"null\"!");
		}
		var tlsVersions = new HashMap<TlsVersion, Boolean>();
		for (var tlsVersion : tlsConfig.getTlsVersions().getTlsVersion()) {
			try {
				tlsVersions.put(TlsVersion.getElement(tlsVersion.getValue()), tlsVersion.isSupported());
			} catch (InvalidAttributeValueException e) {
				throw new IllegalArgumentException("Unknown TLS version: " + tlsVersion.getValue(), e);
			}
		}
		runPlanConfig.tlsVersions = tlsVersions;

		// CipherSuites
		if (tlsConfig.getSupportedCipherSuites() == null
				|| tlsConfig.getSupportedCipherSuites().getCipherSuite() == null) {
			throw new IllegalArgumentException("List of supported cipher suites in the TLS configuration is \"null\"!");
		}
		var supportedCipherSuites = new HashMap<TlsVersion, List<TlsCipherSuite>>();
		for (var tlsVersion : tlsVersions.keySet()) {
			supportedCipherSuites.put(tlsVersion, new ArrayList<TlsCipherSuite>());
		}
		for (var cipherSuite : tlsConfig.getSupportedCipherSuites().getCipherSuite()) {
			try {
				var list = supportedCipherSuites.get(TlsVersion.getElement(cipherSuite.getTlsVersion()));
				var cipherSuiteElem = TlsCipherSuite.valueOf(cipherSuite.getName());
				if (cipherSuiteElem == null) {
					throw new IllegalArgumentException(
							"No cipher suite enumeration value for the cipher suite: " + cipherSuite.getName());
				}
				list.add(cipherSuiteElem);
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse the TLS cipher suite: " + cipherSuite.getName()
						+ " for TLS version: " + cipherSuite.getTlsVersion(), e);
			}
		}
		runPlanConfig.supportedCipherSuites = supportedCipherSuites;

		// TLS Session Lifetime
		if (tlsConfig.getTlsSessionLifetime() != null) {
			runPlanConfig.tlsSessionLifetime = StringHelper.getDurationFromString(tlsConfig.getTlsSessionLifetime());
		} else {
			runPlanConfig.tlsSessionLifetime = Duration.ZERO;
		}


		// TLS Signature Algorithms
		var supportedSignatureAlgorithms = new HashMap<TlsVersion, List<TlsSignatureAlgorithmWithHash>>();
		for (var tlsVersion : tlsVersions.keySet()) {
			supportedSignatureAlgorithms.put(tlsVersion, new ArrayList<TlsSignatureAlgorithmWithHash>());
		}
		for (var signAlg : tlsConfig.getSupportedSignatureAlgorithms().getSignatureAlgorithm()) {
			try {
				var tlsVersion = TlsVersion.getElement(signAlg.getTlsVersion());
				var list = supportedSignatureAlgorithms.get(tlsVersion);
				if (tlsVersion == TlsVersion.TLS_V1_3) {
					var tlsSignatureScheme = TlsSignatureScheme.valueOf(signAlg.getName().toUpperCase());
					list.add(new TlsSignatureAlgorithmWithHashTls13(tlsSignatureScheme));
				} else {
					var tlsSignatureAlgorithm = TlsSignatureAlgorithm.valueOf(signAlg.getName());
					var tlsHashAlgorithm = TlsHashAlgorithm.valueOf(signAlg.getHashName());
					list.add(new TlsSignatureAlgorithmWithHashTls12(tlsSignatureAlgorithm, tlsHashAlgorithm));
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse TLS SignatureAlgorithm: " + signAlg.getName(), e);
			}
		}
		runPlanConfig.supportedSignatureAlgorithms = supportedSignatureAlgorithms;

		// TLS Signature Schemes for Certificate (TLSv1.3 only)
		var signAlgoCertList = new ArrayList<TlsSignatureAlgorithmWithHashTls13>();
		if (tlsConfig.getSupportedSignatureAlgorithmsCertificate() != null
				&& !tlsConfig.getSupportedSignatureAlgorithmsCertificate().getSignatureAlgorithm().isEmpty()) {
			for (var signAlg : tlsConfig.getSupportedSignatureAlgorithmsCertificate().getSignatureAlgorithm()) {
				try {
					var tlsVersion = TlsVersion.getElement(signAlg.getTlsVersion());
					if (tlsVersion != TlsVersion.TLS_V1_3) {
						throw new IllegalArgumentException(
								"Signature Scheme for Certificate only allowed for TLS version: TLSv1.3.");
					}
					signAlgoCertList.add(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.valueOf(signAlg.getName().toUpperCase())));
				} catch (Exception e) {
					throw new IllegalArgumentException("Unable to parse TLS Signature Scheme: " + signAlg.getName()
							+ " for TLS version: " + signAlg.getTlsVersion(), e);
				}
			}
		}
		runPlanConfig.tls13SupportedSignatureAlgorithmsForCertificate = signAlgoCertList;

		// DH/EC Supported Groups
		var supportedGroups = new HashMap<TlsVersion, List<TlsNamedCurves>>();
		var notSupportedGroups = new HashMap<TlsVersion, List<TlsNamedCurves>>();
		for (var tlsVersion : tlsVersions.keySet()) {
			supportedGroups.put(tlsVersion, new ArrayList<TlsNamedCurves>());
			notSupportedGroups.put(tlsVersion, new ArrayList<TlsNamedCurves>());
		}
		for (var group : tlsConfig.getTlsSupportedGroups().getGroup()) {
			try {
				var tlsVersion = TlsVersion.getElement(group.getTlsVersion());
				List<TlsNamedCurves> list = null;
				if (group.isSupported()) {
					list = supportedGroups.get(tlsVersion);
				} else {
					list = notSupportedGroups.get(tlsVersion);
				}
				list.add(TlsNamedCurves.valueOf(group.getName()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse TLS SupportedGroup: " + group.getName()
						+ " for TLS version: " + group.getTlsVersion(), e);
			}
		}
		runPlanConfig.supportedGroups = supportedGroups;
		runPlanConfig.notSupportedGroups = notSupportedGroups;

		// Extensions
		var supportedExtensions = new HashMap<TlsVersion, List<TlsExtensionTypes>>();
		for (var tlsVersion : tlsVersions.keySet()) {
			supportedExtensions.put(tlsVersion, new ArrayList<TlsExtensionTypes>());
		}
		for (var extension : tlsConfig.getSupportedExtensions().getExtension()) {
			try {
				var tlsVersion = TlsVersion.getElement(extension.getTlsVersion());
				var list = supportedExtensions.get(tlsVersion);
				list.add(TlsExtensionTypes.getValueByName(extension.getValue()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse TLS Extension: " + extension.getValue()
						+ " for TLS version: " + extension.getTlsVersion(), e);
			}
		}
		runPlanConfig.supportedExtensions = supportedExtensions;

		// Information on TR-03145 Certification
		TR03145CertificationInfo tr03145Certification = null;
		if (tlsConfig.getInformationOnTR03145Certification() != null) {
			tr03145Certification = TR03145CertificationInfo
					.parseFromTRPJaxb(tlsConfig.getInformationOnTR03145Certification());
		}
		runPlanConfig.tr03145CertificationInfo = tr03145Certification;

		// Key Lengths
		var rsaKeyLengths = new HashMap<TlsVersion, List<RunPlanTlsKeyLengths.KeyLengths>>();
		var dsaKeyLengths = new HashMap<TlsVersion, List<RunPlanTlsKeyLengths.KeyLengths>>();
		var dheKeyLengths = new HashMap<TlsVersion, List<RunPlanTlsKeyLengths.KeyLengths>>();
		for (var tlsVersion : tlsVersions.keySet()) {
			rsaKeyLengths.put(tlsVersion, new ArrayList<RunPlanTlsKeyLengths.KeyLengths>());
			dsaKeyLengths.put(tlsVersion, new ArrayList<RunPlanTlsKeyLengths.KeyLengths>());
			dheKeyLengths.put(tlsVersion, new ArrayList<RunPlanTlsKeyLengths.KeyLengths>());
		}
		for (var keyLength : tlsConfig.getKeyLengths().getRSA().getKeyLength()) {
			try {
				var tlsVersion = TlsVersion.getElement(keyLength.getTlsVersion());
				var list = rsaKeyLengths.get(tlsVersion);
				list.add(new RunPlanTlsKeyLengths.KeyLengths(keyLength.getValue(), keyLength.isSupported()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse KeyLength: " + keyLength.getValue()
						+ " for TLS version: " + keyLength.getTlsVersion(), e);
			}
		}
		for (var keyLength : tlsConfig.getKeyLengths().getDSA().getKeyLength()) {
			try {
				var tlsVersion = TlsVersion.getElement(keyLength.getTlsVersion());
				var list = dsaKeyLengths.get(tlsVersion);
				list.add(new RunPlanTlsKeyLengths.KeyLengths(keyLength.getValue(), keyLength.isSupported()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse KeyLength: " + keyLength.getValue()
						+ " for TLS version: " + keyLength.getTlsVersion(), e);
			}
		}
		for (var keyLength : tlsConfig.getKeyLengths().getDHE().getKeyLength()) {
			try {
				var tlsVersion = TlsVersion.getElement(keyLength.getTlsVersion());
				var list = dheKeyLengths.get(tlsVersion);
				list.add(new RunPlanTlsKeyLengths.KeyLengths(keyLength.getValue(), keyLength.isSupported()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse KeyLength: " + keyLength.getValue()
						+ " for TLS version: " + keyLength.getTlsVersion(), e);
			}
		}
		runPlanConfig.keyLengthsSupport = new RunPlanTlsKeyLengths(rsaKeyLengths, dsaKeyLengths, dheKeyLengths);

		// PSK
		byte[] pskValue = new byte[] { };
		String pskIdentityHint = "";
		String pskIdentity = Constants.getPSKIdentityDefault();
		if (tlsConfig.getTlsWithPSK() != null) {
			pskValue = tlsConfig.getTlsWithPSK().getPSKValue() != null
					&& tlsConfig.getTlsWithPSK().getPSKValue().length != 0 ? tlsConfig.getTlsWithPSK().getPSKValue()
							: pskValue;
			pskIdentityHint = tlsConfig.getTlsWithPSK().getPSKIdentityHintValue() != null
					&& !tlsConfig.getTlsWithPSK().getPSKIdentityHintValue().isBlank()
							? tlsConfig.getTlsWithPSK().getPSKIdentityHintValue()
							: pskIdentityHint;
			pskIdentity = tlsConfig.getTlsWithPSK().getPSKIdentity() != null
					&& !tlsConfig.getTlsWithPSK().getPSKIdentity().isBlank() ?
					tlsConfig.getTlsWithPSK().getPSKIdentity()
					: pskIdentity;
		}
		runPlanConfig.pSKValue = pskValue;
		runPlanConfig.pSKIdentityHint = pskIdentityHint;
		runPlanConfig.pSKIdentity = pskIdentity;

		// Is 0-RTT Supported
		runPlanConfig.is0RTTSupported = tlsConfig.isZeroRTTSupport() != null ? tlsConfig.isZeroRTTSupport() : false;

		// TLS Certificates
		runPlanConfig.domainNameList = new ArrayList<>();
		runPlanConfig.certificateChain = new ArrayList<>();
		if (tlsConfig.getTLSCertificates() != null) {
			var tlsCertificates = tlsConfig.getTLSCertificates();
			if (tlsCertificates.getCertificateChain() != null) {
				for (var cert : tlsCertificates.getCertificateChain().getCertificate()) {
					runPlanConfig.certificateChain.add(CertificateIdentifier.parseFromTRPJaxb(cert));
				}
			}
			if (tlsCertificates.getServerDomains() != null) {
				for (var domain : tlsCertificates.getServerDomains().getSubDomain()) {
					runPlanConfig.domainNameList.add(domain);
				}
			}
		}

		return runPlanConfig;
	}

	/**
	 * Returns a Map of Tls Versions and a boolean indicating whether it is supported or not.
	 * @return Map of Tls Versions and a boolean indicating whether it is supported or not.
	 */
	public HashMap<TlsVersion, Boolean> getTlsVersions() {
		return new HashMap<>(tlsVersions);
	}

	/**
	 * Returns a list of supported Cipher Suites for the specified Tls Version.
	 * @param tlsVersion The Tls Version to use.
	 * @return a list of supported Cipher Suites for the specified Tls Version.
	 */
	public List<TlsCipherSuite> getSupportedCipherSuites(final TlsVersion tlsVersion) {
		return supportedCipherSuites.get(tlsVersion);
	}

	/**
	 * Returns the TLS Session Lifetime value from this Test Run Plan TLS Configuration.
	 * @returns the TLS Session Lifetime value
	 */
	public Duration getTlsSessionLifetime() {
		return tlsSessionLifetime;
	}

	/**
	 * Returns a list of supported signature algorithms for the specified TLS version.
	 * @param tlsVersion the TLS Version to use.
	 * @return a list of supported signature algorithms for the specified TLS version.
	 */
	public List<TlsSignatureAlgorithmWithHash> getSupportedSignatureAlgorithms(final TlsVersion tlsVersion) {
		return supportedSignatureAlgorithms.get(tlsVersion);
	}

	/**
	 * Returns a list of supported signature algorithms for certificates for the TLS Version TLSv1.3.
	 * @return a list of supported signature algorithms for certificates for the TLS Version TLSv1.3.
	 */
	public List<TlsSignatureAlgorithmWithHashTls13> getSupportedSignatureAlgorithmsForCertificate() {
		return new ArrayList<>(tls13SupportedSignatureAlgorithmsForCertificate);
	}

	/**
	 * Returns a list of supported groups for the specified tls version.
	 * @param tlsVersion the tls version to use.
	 * @return a list of supported groups for the specified tls version.
	 */
	public List<TlsNamedCurves> getSupportedGroups(final TlsVersion tlsVersion) {
		return supportedGroups.get(tlsVersion);
	}

	/**
	 * Returns a list of not supported groups for the specified tls version.
	 * @param tlsVersion the tls version to use.
	 * @return a list of not supported groups for the specified tls version.
	 */
	public List<TlsNamedCurves> getNotSupportedGroups(final TlsVersion tlsVersion) {
		return notSupportedGroups.get(tlsVersion);
	}

	/**
	 * Returns information about the Key Length support.
	 * @return information about the Key Length support.
	 */
	public RunPlanTlsKeyLengths getKeyLengthsSupport() {
		return keyLengthsSupport;
	}

	/**
	 * Returns the PSK Value.
	 * @return the PSK Value.
	 */
	public byte[] getPSKValue() {
		return pSKValue.clone();
	}

	/**
	 * Returns the PSK Identity Hint.
	 * @return the PSK Identity Hint.
	 */
	public String getPSKIdentityHint() {
		return pSKIdentityHint;
	}

	/**
	 * Returns the PSK Identity.
	 * @return the PSK Identity.
	 */
	public String getPSKIdentity() {
		return pSKIdentity;
	}

	/**
	 * Returns a list of supported TLS Extensions for the specified TLS version.
	 * @param tlsVersion the TLS version to use
	 * @return a list of supported TLS Extensions for the specified TLS version.
	 */
	public List<TlsExtensionTypes> getSupportedExtensions(final TlsVersion tlsVersion) {
		return supportedExtensions.get(tlsVersion);
	}

	/**
	 * Returns the information whether a PSK Identity Hint is required.
	 * @return the information whether a PSK Identity Hint is required.
	 */
	public boolean isPSKIdentityHintRequired() {
		return !pSKIdentityHint.isBlank();
	}

	/**
	 * Returns the information whether TR03145 Certification Information is set.
	 * @return the information whether TR03145 Certification Information is set.
	 */
	public boolean isTR03145InformationSet() {
		return tr03145CertificationInfo != null;
	}

	/**
	 * Returns the information about TR03145 Certification.
	 * @return the information whether TR03145 Certification.
	 */
	public TR03145CertificationInfo getTR03CertificationInfo() {
		return tr03145CertificationInfo;
	}

	/**
	 * Returns information whether 0-RTT data is supported.
	 * @return whether 0-RTT data is supported.
	 */
	public boolean getIs0RTTSupported() {
		return is0RTTSupported;
	}

	/**
	 * Returns Information about the Certificate Chain.
	 * @return Information about the Certificate Chain.
	 */
	public List<CertificateIdentifier> getCertificateChain() {
		return new ArrayList<>(certificateChain);
	}

	/**
	 * Returns the list of domain names.
	 * @return the list of domain names.
	 */
	public List<String> getDomainNameList() {
		return new ArrayList<>(domainNameList);
	}

}
