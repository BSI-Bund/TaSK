package com.achelos.task.xmlparser.datastructures.mics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.xmlparser.configparsing.StringHelper;

import generated.jaxb.input.SupportedCryptography.SupportedTLSVersion;

/**
 * Internal Data Structure containing the information regarding a single TlsVersion and its supported algorithms and extensions.
 */
public class TlsVersionSupport {

	private TlsVersion tlsVersion;
	private List<String> supportedCipherSuites;
	private List<String> supportedGroups;
	private List<String> supportedSignAlgorithms;
	private List<String> supportedSignAlgorithmsCert;
	private List<String> supportedTlsExtensions;
	private HashMap<String, Integer> supportedKeyLengths;

	/**
	 * Hidden Constructor.
	 *
	 * @param tlsVersion String
	 */
	private TlsVersionSupport(final String tlsVersion) {
		try {
			this.tlsVersion = TlsVersion.getElement(tlsVersion);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid TLS version string: " + tlsVersion, e);
		}
	}

	/**
	 * Hidden Constructor.
	 *
	 * @param tlsVersion enumeration
	 */
	private TlsVersionSupport(final TlsVersion tlsVersion) {
		this.tlsVersion = tlsVersion;
	}

	/**
	 * Parse a {@link TlsVersionSupport} Object out of an raw JAXB internal class.
	 *
	 * @param suppTlsVersion raw JAXB internal object.
	 * @return the {@link TlsVersionSupport} Object containing the information of the supported TLS version object.
	 */
	public static TlsVersionSupport parseFromJaxb(final SupportedTLSVersion suppTlsVersion) {
		// TLS version String
		var tlsVersion = new TlsVersionSupport(suppTlsVersion.getVersion());

		// Cipher suites
		if (suppTlsVersion.getSupportedCipherSuites() != null) {
			tlsVersion.supportedCipherSuites = suppTlsVersion.getSupportedCipherSuites().getCipherSuite();
		} else {
			tlsVersion.supportedCipherSuites = new ArrayList<>();
		}

		// Elliptic Curves and DH Groups for SUPP_GROUPS Extension.
		if (suppTlsVersion.getSupportedEllipticCurvesDHGroups() != null) {
			tlsVersion.supportedGroups = suppTlsVersion.getSupportedEllipticCurvesDHGroups().getGroup();
		} else {
			tlsVersion.supportedGroups = new ArrayList<>();
		}

		// Signature Algorithms
		if (suppTlsVersion.getSupportedSignatureAlgorithms() != null) {
			tlsVersion.supportedSignAlgorithms = new ArrayList<>();
			for (var signAlg : suppTlsVersion.getSupportedSignatureAlgorithms().getSignatureAlgorithm()) {
				if (signAlg.getHashName() != null && !signAlg.getHashName().isBlank()) {
					tlsVersion.supportedSignAlgorithms
							.add(StringHelper.combineSignAndHashAlgorithm(signAlg.getName(), signAlg.getHashName()));
				} else {
					tlsVersion.supportedSignAlgorithms.add(signAlg.getName());
				}
			}
		} else {
			tlsVersion.supportedSignAlgorithms = new ArrayList<>();
		}

		// Signature Algorithms for Certificates
		if (suppTlsVersion.getSupportedSignatureAlgorithmsForCertificates() != null) {
			tlsVersion.supportedSignAlgorithmsCert = suppTlsVersion.getSupportedSignatureAlgorithmsForCertificates()
					.getSignatureScheme();
		} else {
			tlsVersion.supportedSignAlgorithmsCert = new ArrayList<>();
		}

		// TLS Extensions
		if (suppTlsVersion.getSupportedTLSExtensions() != null) {
			tlsVersion.supportedTlsExtensions = suppTlsVersion.getSupportedTLSExtensions().getExtension();
		} else {
			tlsVersion.supportedTlsExtensions = new ArrayList<>();
		}

		// Supported Key Lengths
		tlsVersion.supportedKeyLengths = new HashMap<>();
		if (suppTlsVersion.getSupportedKeyLengths() != null) {
			for (var minKeyLen : suppTlsVersion.getSupportedKeyLengths().getMinimalKeyLength()) {
				tlsVersion.supportedKeyLengths.put(minKeyLen.getAlgorithm(), minKeyLen.getMinimalSupportedKeyLength());
			}
		}

		return tlsVersion;
	}

	/**
	 * Returns the TLS Version.
	 * @return the TLS version
	 */
	public TlsVersion getTlsVersion() {
		return tlsVersion;
	}

	/**
	 * Returns a list of supported cipher suites.
	 * @return the supported cipher suites
	 */
	public List<String> getSupportedCipherSuites() {
		return new ArrayList<>(supportedCipherSuites);
	}

	/**
	 * Returns a list of supported groups.
	 * @return the Supported Groups.
	 */
	public List<String> getSupportedGroups() {
		return new ArrayList<>(supportedGroups);
	}

	/**
	 * Returns a list of supported signature algorithms.
	 * @return the supported signature algorithms.
	 */
	public List<String> getSupportedSignAlgorithms() {
		return new ArrayList<>(supportedSignAlgorithms);
	}

	/**
	 * Returns a list of supported signature algorithms for certificates. (Used in the TLSv1.3 case)
	 * @return the supported signature algorithms.
	 */
	public List<String> getSupportedSignAlgorithmsCert() {
		return new ArrayList<>(supportedSignAlgorithmsCert);
	}

	/**
	 * Returns a list of supported TLS extensions.
	 * @return the supported TLS extensions.
	 */
	public List<String> getSupportedTlsExtensions() {
		return new ArrayList<>(supportedTlsExtensions);
	}

	/**
	 * Returns a list of supported key lengths.
	 * @return the supported key lengths.
	 */
	public HashMap<String, Integer> getSupportedKeyLengths() {
		return new HashMap<>(supportedKeyLengths);
	}

}
