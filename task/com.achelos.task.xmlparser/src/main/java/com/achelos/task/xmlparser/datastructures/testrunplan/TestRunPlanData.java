package com.achelos.task.xmlparser.datastructures.testrunplan;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsDHGroup;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.xmlparser.datastructures.common.CertificateIdentifier;
import com.achelos.task.xmlparser.datastructures.common.TR03145CertificationInfo;

import generated.jaxb.testrunplan.TestRunPlan;


/**
 * Data Structure class containing the information included in an TestRunPlan XML file.
 */
public class TestRunPlanData {

	private String generationTime;
	private List<String> testCases;
	private TestConfiguration testConfiguration;
	private RunPlanMicsInfo micsInfo;
	private RunPlanTlsConfiguration tlsConfiguration;

	/**
	 * Hidden Constructor
	 */
	private TestRunPlanData() {
		// Empty.
	}

	/**
	 * Parse the provided TestRunPlan object into a TestRunPlanData structure, which is provided as a singleton
	 * instance.
	 *
	 * @param trp The parsed TestRunPlan object, an instance of the JAXB generated Data Structures representing the XML
	 * Structure of the TestRunPlan.
	 * @return The generated TestRunPlanData structure object.
	 */
	public static TestRunPlanData parseFromJaxb(final TestRunPlan trp) {
		// Empty Object.
		var runPlan = new TestRunPlanData();

		// Generation Time
		runPlan.generationTime = trp.getTestRunPlanGenerationTime();

		// TestCases
		runPlan.testCases = trp.getTestCases().getTestCase();

		// TestConfiguration
		runPlan.setTestConfiguration(trp.getTestConfiguration());

		// MICSInfo
		runPlan.setMicsInfo(trp.getMICSInfo());

		// TLS Configuration
		runPlan.setTlsConfig(trp.getTlsConfiguration());


		return runPlan;
	}

	/**
	 * Returns the generation time
	 *
	 * @return the generationTime
	 */
	public String getGenerationTime() {
		return generationTime;
	}

	/**
	 * Return the list of test Cases which was contained in the TestRunPlan XML file.
	 *
	 * @return The included list of test cases.
	 */
	public List<String> getTestCases() {
		return new ArrayList<>(testCases);
	}

	/**
	 * Return the list of supported TLS versions corresponding to the Table 4 of the TR-03116-TS ICS document.
	 *
	 * @return list of supported TLS versions.
	 */
	public List<TlsVersion> getSupportedTLSVersions() {
		var listOfSupported = new ArrayList<TlsVersion>();
		// for (var tlsVersion : this.tlsConfiguration.getTlsVersions().keySet()) {
		// if (this.tlsConfiguration.getTlsVersions().get(tlsVersion)) {
		// listOfSupported.add(tlsVersion);
		// }
		// }
		// According to the BSI this should be TLS 1.2 for now
		listOfSupported.add(TlsVersion.TLS_V1_2);

		return listOfSupported;
	}

	/**
	 * Using a list of all TLS version, which comes from a Configuration File corresponding to Table 20 of the
	 * TR-03116-TS ICS document, generates a list of all TLS versions which are not supported by the DUT.
	 *
	 * @return A list of not supported TLS versions.
	 */
	public List<TlsVersion> getNotSupportedTLSVersions() {
		var listOfNotSupported = new ArrayList<TlsVersion>();
		for (var tlsVersion : tlsConfiguration.getTlsVersions().keySet()) {
			if (!tlsConfiguration.getTlsVersions().get(tlsVersion) && tlsVersion != TlsVersion.TLS_V1_3) {
				listOfNotSupported.add(tlsVersion);
			}
		}
		return listOfNotSupported;
	}

	/**
	 * Looks for the highest supported TLS version of the DUT according to the MICS file and returns it.
	 *
	 * @return The highest supported TLS version of the DUT.
	 */
	public TlsVersion getHighestSupportedTlsVersion() {
		// var supportedTlsVersions = getSupportedTLSVersions();
		// var currentHighestVersion = supportedTlsVersions.get(0);
		// for (var version : supportedTlsVersions) {
		// if (version.compareTo(currentHighestVersion) > 0) {
		// currentHighestVersion = version;
		// }
		// }
		// According to the BSI this should be TLS 1.2 for now
		var currentHighestVersion = TlsVersion.TLS_V1_2;
		return currentHighestVersion;
	}

	/**
	 * Generates a list of all supported cipher suites for the specified TlsVersion, according to Table 5 of the
	 * TR-03116-TS ICS document, which contain the "_CBC_" tag in the identifier. The order of the list shall correspond
	 * to the preferences of the DUT.
	 *
	 * @param tlsVersion the TLS version the cipher suites should be applicable for.
	 * @return a list of all supported cipher suites for the specified Tls version which contain the "_CBC_" tag in the
	 * identifier.
	 */
	public List<TlsCipherSuite> getCBCBasedSupportedCipherSuites(final TlsVersion tlsVersion) {
		var cBCBasedSupportedCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (tlsCipherSuite.name().contains("_CBC_")) {
				cBCBasedSupportedCipherSuites.add(tlsCipherSuite);
			}
		}
		return cBCBasedSupportedCipherSuites;
	}

	/**
	 * Generates a list of all supported cipher suites for the specified TLS version according to Table 5 of the
	 * TR-03116-TS ICS document. The order of the list shall correspond to the preferences of the DUT.
	 *
	 * @param tlsVersion The TLS version the cipher suites should be applicable for.
	 * @return a list of all supported cipher suites for the specified TLS version
	 */
	public List<TlsCipherSuite> getSupportedCipherSuites(final TlsVersion tlsVersion) {
		// Order determines the preference
		// PSK cipher suites are only returned for getSupportedPSKCipherSuites?
		return tlsConfiguration.getSupportedCipherSuites(tlsVersion);
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> getSupportedEllipticCurvesAndFFDHE(final TlsVersion tlsVersion) {
		return tlsConfiguration.getSupportedGroups(tlsVersion);
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> filterSupportedGroupsToEllipticCurveGroups(final TlsVersion tlsVersion) {
		List<TlsNamedCurves> eccGroups = new ArrayList<>();

		for (TlsNamedCurves supportedGroup : tlsConfiguration.getSupportedGroups(tlsVersion)) {
			if (!supportedGroup.isFFDHEGroup()) {
				eccGroups.add(supportedGroup);
			}
		}
		return eccGroups;
	}

	public List<TlsDHGroup> getInsufficientDHEKeyLengths(final TlsVersion tlsVersion) {
		return getDHEKeyLengths(tlsVersion, false);
	}

	public List<TlsDHGroup> getSufficientDHEKeyLengths(final TlsVersion tlsVersion) {
		return getDHEKeyLengths(tlsVersion, true);
	}
	
	private List<TlsDHGroup> getDHEKeyLengths(final TlsVersion tlsVersion, final boolean isSupported) {
		List<TlsDHGroup> dhGroupsInsufficientKeyLength = new ArrayList<>();
		var keyLengths = tlsConfiguration.getKeyLengthsSupport().getDheKeyLengths().get(tlsVersion);
		for (RunPlanTlsKeyLengths.KeyLengths keyLength : keyLengths) {
			if (isSupported == keyLength.isSupported()) {
				TlsDHGroup group = TlsDHGroup.findMatchingKeyLengthGroup(keyLength.getKeyLength());
				if (group == null) {
					throw new RuntimeException("No matching DH Group found for key length: "
							+ keyLength.getKeyLength());
				}
				dhGroupsInsufficientKeyLength.add(group);
			}
		}
		return dhGroupsInsufficientKeyLength;
	}

	/**
	 * Generates a list of all supported Elliptic Curves and DH Groups according to Table 7 of TR-03116-TS ICS document
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The TLS version the Named Curves should be applicable for.
	 * @return A list of all supported Elliptic Curves and DH Groups for the specified TlsVersion.
	 */
	public List<TlsNamedCurves> filterSupportedGroupsToFFDHEGroups(final TlsVersion tlsVersion) {
		List<TlsNamedCurves> ffdheGroups = new ArrayList<>();

		for (TlsNamedCurves supportedGroup : tlsConfiguration.getSupportedGroups(tlsVersion)) {
			if (supportedGroup.isFFDHEGroup()) {
				ffdheGroups.add(supportedGroup);
			}
		}
		return ffdheGroups;
	}

	/**
	 * Using a list of all Elliptic Curves, which comes from a Configuration File corresponding to Table 21 of the
	 * TR-03116-TS ICS document, generates a list of all Elliptic Curves which are not supported by the DUT for the
	 * specified TlsVersion.
	 *
	 * @param tlsVersion The TlsVersion the list should be generated for.
	 * @return A list of all Elliptic Curves which are not supported by the DUT for the specified TlsVersion
	 */
	public List<TlsNamedCurves> getNotSupportedEllipticCurves(final TlsVersion tlsVersion) {
		return tlsConfiguration.getNotSupportedGroups(tlsVersion).stream().filter(group -> !(group.isFFDHEGroup())).collect(Collectors.toList());
	}

	public List<TlsNamedCurves> getNotSupportedDHEGroups(TlsVersion tlsVersion) {
		return tlsConfiguration.getNotSupportedGroups(tlsVersion).stream().filter(TlsNamedCurves::isFFDHEGroup).collect(Collectors.toList());
	}

	/**
	 * Generates a list of all TlsSignatureAlgorithmWithHash which are supported by the DUT for the specified TLS version.
	 * @param tlsVersion TLS Version which shall be used.
	 *
	 * @return A list of all TlsSignatureAlgorithmWithHash which are supported by the DUT.
	 */
	public List<TlsSignatureAlgorithmWithHash> getSupportedSignatureAlgorithms(final TlsVersion tlsVersion) {
		return tlsConfiguration.getSupportedSignatureAlgorithms(tlsVersion);
	}

	/**
	 * Generates a list of all Signature Algorithms in certificates which are supported by the DUT, if the DUT supports
	 * TLSv1.3. Note: Currently, this only makes sense for TLS version 1.3, hence, this returns the list of supported
	 * Signature Algorithms for Certificates for TLSv1.3.
	 *
	 * @return A list of all Signature Algorithms in certificates which are supported by the DUT, if the DUT supports
	 * TLSv1.3.
	 */
	public List<TlsSignatureScheme> getSupportedSignatureAlgorithmsForCertificates() {
		return tlsConfiguration.getSupportedSignatureAlgorithmsForCertificate();
	}

	/**
	 * Return a single by the DUT supported cipher suite for the specified TlsVersion.
	 *
	 * @param tlsVersion The TlsVersion to get a single supported cipher suite for.
	 * @return a single by the DUT supported cipher suite for the specified TlsVersion or 'null' if no cipher suites are
	 * supported for the provided TlsVersion.
	 */
	public TlsCipherSuite getSingleSupportedCipherSuite(final TlsVersion tlsVersion) {
		if (getSupportedCipherSuites(tlsVersion).isEmpty()) {
			return null;
		}
		return getSupportedCipherSuites(tlsVersion).get(0);
	}

	/**
	 * Return a single by the DUT supported ECC CipherSuite, i.e. cipher suites containing either '_ECDH_' or '_ECDHE_',
	 * for the specified TlsVersion.
	 *
	 * @param tlsVersion The {@link TlsVersion} to get a single supported ECC cipher suite for.
	 * @return a single supported ECC cipher suite by the DUT for the specified TlsVersion or 'null' if no such
	 * cipher suites are supported for the provided {@link TlsVersion}.
	 */
	public TlsCipherSuite getSingleSupportedECCCipherSuite(final TlsVersion tlsVersion) {
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (isECCCipherSuite(tlsCipherSuite)) {
				return tlsCipherSuite;
			}

		}
		return null;
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedECCCipherSuites(final TlsVersion tlsVersion) {
		var eccCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (isECCCipherSuite(tlsCipherSuite)) {
				eccCipherSuites.add(tlsCipherSuite);
			}
		}
		return eccCipherSuites;
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedEcdsaCipherSuites(final TlsVersion tlsVersion) {
		var ecdsaCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (isEcdsaCipherSuite(tlsCipherSuite)) {
				ecdsaCipherSuites.add(tlsCipherSuite);
			}
		}
		return ecdsaCipherSuites;
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedRsaCipherSuites(final TlsVersion tlsVersion) {
		var ecdsaCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (isRsaCipherSuite(tlsCipherSuite)) {
				ecdsaCipherSuites.add(tlsCipherSuite);
			}
		}
		return ecdsaCipherSuites;
	}

	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedDsaCipherSuites(final TlsVersion tlsVersion) {
		var ecdsaCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (isDsaCipherSuite(tlsCipherSuite)) {
				ecdsaCipherSuites.add(tlsCipherSuite);
			}
		}
		return ecdsaCipherSuites;
	}


	/**
	 * Filters the cipher suites, which are supported by the DUT. Returns only those, that contain either '_ECDH_' or
	 * '_ECDHE_' and that are valid for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion A TlsVersion as filter.
	 * @return A list of ecc cipher suites, which are supported by the DUT.
	 */
	public List<TlsCipherSuite> getSupportedNonECCCipherSuites(final TlsVersion tlsVersion) {
		var nonECCCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (!isECCCipherSuite(tlsCipherSuite)) {
				nonECCCipherSuites.add(tlsCipherSuite);
			}
		}
		return nonECCCipherSuites;
	}

	/**
	 * Checks the name of the cipher suite for support of elliptic curves.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports elliptic curve algorithms
	 */
	public boolean isECCCipherSuite(final TlsCipherSuite cipherSuite) {
		return cipherSuite.name().contains("_ECDH");
	}

	/**
	 * Checks the name of the cipher suite for support of ECDSA certificates
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports ECDSA
	 */
	public boolean isEcdsaCipherSuite(final TlsCipherSuite cipherSuite) {
		return cipherSuite.name().contains("_ECDSA");
	}

	/**
	 * Checks the name of the cipher suite for support of RSA certicates.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports RSA
	 */
	public boolean isRsaCipherSuite(final TlsCipherSuite cipherSuite) {
		return cipherSuite.name().contains("_RSA");
	}

	/**
	 * Checks the name of the cipher suite for support of DSA certicates.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports DSA
	 */
	public boolean isDsaCipherSuite(final TlsCipherSuite cipherSuite) {
		return cipherSuite.name().contains("_DSA");
	}

	/**
	 * Checks whether at least one of the cipher suites in the list supports elliptic curve.
	 * 
	 * @param cipherSuites
	 * @return
	 */
	public boolean containsECCCipherSuite(final List<TlsCipherSuite> cipherSuites) {
		for (var cipherSuite : cipherSuites) {
			if (isECCCipherSuite(cipherSuite)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks the name of the cipher suite for support of elliptic curves.
	 *
	 * @param cipherSuite
	 * @return true, if the given cipher suite supports elliptic curve algorithms
	 */
	public boolean isPFSCipherSuite(final TlsCipherSuite cipherSuite) {
		return cipherSuite.name().contains("_ECDHE_") || cipherSuite.name().contains("_DHE_");
	}

	public boolean containsPFSCipherSuite(final List<TlsCipherSuite> cipherSuites) {
		for (var cipherSuite : cipherSuites) {
			if (isPFSCipherSuite(cipherSuite)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Generates a list of all supported cipher suites by the DUT which provide perfect forward secrecy, i.e. the
	 * identifier contain '_ECDHE_' or '_DHE_', for the specified TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion the TlsVersion to get the list of supported PFS cipher suites for.
	 * @return a list of by the DUT supported PFS cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedPFSCipherSuites(final TlsVersion tlsVersion) {
		var pFSSupportedCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (tlsCipherSuite.name().contains("_DHE_") || tlsCipherSuite.name().contains("_ECDHE_")) {
				pFSSupportedCipherSuites.add(tlsCipherSuite);
			}
		}
		return pFSSupportedCipherSuites;
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_DHE_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion the TlsVersion to get the list of supported FFDHE cipher suites for.
	 * @return a list of by the DUT supported FFDHE cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedFFDHECipherSuites(final TlsVersion tlsVersion) {
		var ffdheBasedSupportedCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (tlsCipherSuite.name().contains("_DHE_")) {
				ffdheBasedSupportedCipherSuites.add(tlsCipherSuite);
			}
		}
		return ffdheBasedSupportedCipherSuites;
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_DHE_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion The TlsVersion to get the list of supported FFDHE cipher suites for.
	 * @return A list of by the DUT supported FFDHE cipher suites for the specified TlsVersion.
	 */
	public TlsCipherSuite getSingleSupportedFFDHECipherSuite(final TlsVersion tlsVersion) {
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (tlsCipherSuite.name().contains("_DHE_")) {
				return tlsCipherSuite;
			}
		}
		return null;
	}

	/**
	 * Generates a list of all supported cipher suites by the DUT whose identifier contains '_PSK_', for the specified
	 * TlsVersion.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion The TlsVersion to get the list of supported PSK cipher suites for.
	 * @return A list of by the DUT supported PSK cipher suites for the specified TlsVersion.
	 */
	public List<TlsCipherSuite> getSupportedPSKCipherSuites(final TlsVersion tlsVersion) {
		var pskBasedSupportedCipherSuites = new ArrayList<TlsCipherSuite>();
		for (var tlsCipherSuite : getSupportedCipherSuites(tlsVersion)) {
			if (tlsCipherSuite.name().contains("_PSK_")) {
				pskBasedSupportedCipherSuites.add(tlsCipherSuite);
			}
		}
		return pskBasedSupportedCipherSuites;
	}

	/**
	 * Generates a list of all cipher suites, that are not supported by the DUT.
	 * <p>
	 * Reference: BSI TR-03116-TS, Table 5
	 *
	 * @param tlsVersion a {@link TlsVersion} as filter.
	 * @return a list of cipher suites which are not supported by the DUT
	 */
	public List<TlsCipherSuite> getNotSupportedCipherSuites(final TlsVersion tlsVersion) {
		ArrayList<TlsCipherSuite> allCipherSuites
				= new ArrayList<>(TlsCipherSuite.getCipherSuitesByVersions(tlsVersion));
		allCipherSuites.removeAll(getSupportedCipherSuites(tlsVersion));
		allCipherSuites.remove(TlsCipherSuite.TLS_NULL_WITH_NULL_NULL);
		return allCipherSuites;
	}

	/**
	 * Returns the information, whether a PSK Hint is required by a client, according to Table 13 of the
	 * TR-03116-TS ICS document. Note: Only applicable for TLSv1.2.
	 *
	 * @return Information, whether a PSK Hint is required by a client, according to Table 13 of the TR-03116-TS
	 * ICS document.
	 */
	public Boolean isPSKHintRequired() {
		return tlsConfiguration.isPSKIdentityHintRequired();
	}

	/**
	 * Returns the information, what value the PSK Identity Hint must contain, if it is applicable. According to Table
	 * 13 of the TR-03116-TS ICS document. Note: Only applicable for TLSv1.2.
	 *
	 * @return Information, what value the PSK Identity Hint must contain, if it is applicable. According to Table 13 of
	 * the TR-03116-TS ICS document.
	 */
	public String getPSKIdentityHint() {
		return tlsConfiguration.getPSKIdentityHint();
	}

	/**
	 * The PSK Value to use when establishing a connection to the DUT. According to Table 12 of the TR-03116-TS ICS
	 * document.
	 *
	 * @return PSK Value to use when establishing a connection to the DUT.
	 */
	public byte[] getPSKValue() {
		return tlsConfiguration.getPSKValue();
	}

	/**
	 * Generates a list containing the supported TLS extensions of the DUT for the specified TlsVersion according to
	 * Table 10 of the TR-03116-TS ICS document.
	 *
	 * @param tlsVersion The TlsVersion to get the supported TLS extensions for.
	 * @return A list containing the supported TLS extensions of the DUT for the specified TlsVersion.
	 */
	public List<TlsExtensionTypes> getSupportedExtensions(final TlsVersion tlsVersion) {
		return tlsConfiguration.getSupportedExtensions(tlsVersion);
	}

	/**
	 * Returns a data structure containing information on TR-03145 certification according to Table 11 of the
	 * TR-03116-TS ICS document.
	 *
	 * @return Data structure containing information on TR-03145 certification according to Table 11 of the TR-03116-TS
	 * ICS document.
	 */
	public TR03145CertificationInfo getTR03145CertificationInfo() {
		return tlsConfiguration.getTR03CertificationInfo();
	}

	/**
	 * Returns information whether the DUT makes use of early data according to Table 15 of the TR-03116-TS ICS
	 * document. Note: Only applicable for TLSv1.3.
	 *
	 * @return Whether the DUT makes use of early data according to Table 15 of the TR-03116-TS ICS document.
	 */
	public Boolean is0RTTSupported() {
		return tlsConfiguration.getIs0RTTSupported();
	}

	/**
	 * Returns a list of Certificate Identifier objects according to Table 16 of the TR-03116-TS ICS document.
	 *
	 * @return A list of Certificate Identifier objects according to Table 16 of the TR-03116-TS ICS document.
	 */
	public List<CertificateIdentifier> getCertificateChain() {
		return tlsConfiguration.getCertificateChain();
	}

	/**
	 * In case of a TLS server returns a list of (sub-)domain names, which the TLS server certificate is used for.
	 *
	 * @return A list of (sub-)domain names according to Table 17 of the TR-03116-TS ICS document.
	 */
	public List<String> getSubDomains() {
		return tlsConfiguration.getDomainNameList();
	}

	/**
	 * In case of a TLS server returns the address (i.e. the IP Address or the URL) under which the DUT can be found.
	 *
	 * @return the address (i.e. the IP Address or the URL) under which the DUT can be found.
	 */
	public String getDutAddress() {
		return testConfiguration.getDutURL();
	}

	/**
	 * Returns the ApplicationType of the DUT.
	 *
	 * @return tthe ApplicationType of the DUT.
	 */
	public String getDUTApplicationType() {
		return testConfiguration.getDutApplicationType();
	}

	/**
	 * In case of a TLS server returns the port under which the DUT can be found.
	 *
	 * @return the port under which the DUT can be found.
	 */
	public String getDutPort() {
		return testConfiguration.getDutPort();
	}

	/**
	 * In case of a TLS client returns the executable by which the DUT can be executed.
	 *
	 * @return the executable by which the DUT can be executed.
	 */
	public String getDUTExecutable() {
		return testConfiguration.getDutExecutable();
	}

	/**
	 * In case of a TLS client returns the call arguments which shall be used to execute the DUT for a simple
	 * connection.
	 *
	 * @return the call arguments which shall be used to execute the DUT.
	 */
	public String getDUTCallArgumentsConnect() {
		return testConfiguration.getDutCallArgumentsConnect();
	}

	/**
	 * In case of a TLS client returns the call arguments which shall be used to execute the DUT for a session
	 * resumption.
	 *
	 * @return the call arguments which shall be used to execute the DUT.
	 */
	public String getDUTCallArgumentsResume() {
		return testConfiguration.getDutCallArgumentsReconnect();
	}

	/**
	 * Returns the port on which the eID-Client DUT listens.
	 * @return the port on which the eID-Client DUT listens.
	 */
	public Integer getDutEIDClientPort() {
		return testConfiguration.getDutEIDClientPort();
	}

	/**
	 * Returns the maximum TLS session lifetime of the DUT according to Table 14 of the TR-03116-TS ICS document.
	 *
	 * @return the maximum TLS session lifetime of the DUT according to Table 14 of the TR-03116-TS ICS document.
	 */
	public Duration getMaximumTLSSessionTime() {
		return tlsConfiguration.getTlsSessionLifetime();
	}


	/**
	 * Return the Generation Time of the TestRunPlan, which was written in the TestRunPlan XML file.
	 *
	 * @return The Generation Time of the TestRunPlan, which was written in the TestRunPlan XML file.
	 */
	public String getTRPCreationTime() {
		return generationTime;
	}

	/**
	 * Return a single by the DUT unsupported TLS Signature Algorithm. Note: This always returns SHA1WithECDSA, as it
	 * should no longer be supported by any system.
	 *
	 * @return a single by the DUT unsupported TLS Signature Algorithm.
	 */
	public TlsSignatureAlgorithmWithHash getNotSupportedSignatureAlgorithm() {
		return new TlsSignatureAlgorithmWithHash(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha1);
	}

	/**
	 * In case if the DUT is a TLS Server, returns the test client certificate file, used to test the mutual
	 * authentication to the server.
	 *
	 * @return the Client Certificate file, used to test the mutual authentication to the server.
	 */
	public File getClientCertificate() {
		if (testConfiguration.getApplicationSpecificData().isTestClientCertSet()) {
			return new File(testConfiguration.getApplicationSpecificData().getTestClientCertPath());
		}
		throw new NullPointerException("Trying to access test TLS client certificate path, which is not set.");
	}

	/**
	 * In case if the DUT is a TLS Server, returns the private key of the test client certificate, used to test the
	 * mutual authentication to the server.
	 *
	 * @return the private key of the client, used to test the mutual authentication to the server.
	 */
	public File getClientPrivateKey() {
		if (testConfiguration.getApplicationSpecificData().isTestClientCertSet()) {
			return new File(testConfiguration.getApplicationSpecificData().getTestClientPrivateKeyPath());
		}
		throw new NullPointerException(
				"Trying to access test TLS client certificate private key path, which is not set.");
	}

	/**
	 * Returns a Data Structure containing general information about the MICS file which was used to generate the
	 * TestRunPlan.
	 *
	 * @return Data Structure containing general information about the MICS file which was used to generate the TestRunPlan.
	 */
	public RunPlanMicsInfo getMicsInfo() {
		return micsInfo;
	}

	private void setTestConfiguration(final TestRunPlan.TestConfiguration rawTestConfiguration) {
		testConfiguration = TestConfiguration.parseFromTRPJaxb(rawTestConfiguration);
	}

	private void setMicsInfo(final TestRunPlan.MICSInfo micsInfo) {
		String micsName = micsInfo.getName();
		String micsDescription = micsInfo.getDescription();
		String pathToFile = micsInfo.getPathToFile();

		this.micsInfo = new RunPlanMicsInfo(micsName, micsDescription, pathToFile);
	}

	private void setTlsConfig(final TestRunPlan.TlsConfiguration tlsConfig) {
		if (tlsConfig == null) {
			throw new IllegalArgumentException("Test Run Plans TLS configuration is \"null\"!");
		}
		tlsConfiguration = RunPlanTlsConfiguration.parseFromJaxb(tlsConfig);
	}

	/**
	 * Returns the information, whether the DUT supports the use of SNI. This is a stub for now.
	 * @return true if the DUT supports use of SNI
	 */
	public boolean getTlsUseSni() {
		return false;
	}

	/**
	 * Return the stored DUT Capabilities.
	 * @return the stored DUT Capabilities.
	 */
	public List<DUTCapabilities> getDutCapabilities(){
		return testConfiguration.getDutCapabilities();
	}
}
