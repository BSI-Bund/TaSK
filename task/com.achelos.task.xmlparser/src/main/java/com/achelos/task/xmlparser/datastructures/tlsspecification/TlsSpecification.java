package com.achelos.task.xmlparser.datastructures.tlsspecification;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.xmlparser.configparsing.StringHelper;
import com.achelos.task.xmlparser.datastructures.tlsspecification.AlgorithmMinimumKeyLengths.KeyLengthSpecifier;

import generated.jaxb.configuration.FurtherSpecifications.TLSVersions;
import generated.jaxb.configuration.Metadata;
import generated.jaxb.configuration.TLS12Parameter;
import generated.jaxb.configuration.TLS13Parameter;
import generated.jaxb.configuration.TLSEllipticCurves;
import generated.jaxb.configuration.TLSMinimumKeyLength;
import generated.jaxb.configuration.TLSSpecification;

/**
 * Internal Data Structure containing Specification data for TLS.
 */
public class TlsSpecification {
	// Basic Data
	private final String id;
	private final String title;
	private final String version;
	private final List<String> references;

	// TLS specification details
	// Supported TLS versions
	// public List<TlsVersion> tlsVersionSupport; // Maybe Hashmap from
	// TLSVersionString to Object?
	private HashMap<String, TlsVersion> tlsVersionSupport;
	// TlsSessionLifetime
	private Duration tlsSessionLifetime;
	// Spec regarding TLS 1.2
	private TLSv1_2Spec tlsv1_2Spec;
	// Spec regarding TLS 1.3
	private TLSv1_3Spec tlsv1_3Spec;
	// Supported Minimum Key Lengths
	private HashMap<String, AlgorithmMinimumKeyLengths> tlsMinimumKeyLength;
	// Supported Elliptic Curves
	private HashMap<String, EllipticCurve> supportedEllipticCurves;

	@Override
	public String toString() {
		var specAsString = new StringBuilder("TLS Specification: " + id + System.lineSeparator());
		StringHelper.appendAttrToStringBuilder(specAsString, "ID", id);
		StringHelper.appendAttrToStringBuilder(specAsString, "Title", title);
		StringHelper.appendAttrToStringBuilder(specAsString, "Version", version);
		StringHelper.appendListToStringBuilder(specAsString, "References", references, 1);
		StringHelper.appendHashMapToStringBuilder(specAsString, "TLS version Support", tlsVersionSupport, 1);
		StringHelper.appendAttrToStringBuilder(specAsString, "TLS SessionLifetime", tlsSessionLifetime.toString());

		specAsString.append("\tSpecifications regarding TLSv1.2" + System.lineSeparator());
		StringHelper.appendHashMapToStringBuilder(specAsString, "CipherSuite Support",
				tlsv1_2Spec.getCipherSuiteSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "DH Group Support", tlsv1_2Spec.getDHGroupSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "Signature Algorithm Support",
				tlsv1_2Spec.getSignAlgorithmSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "TLS Feature Support",
				tlsv1_2Spec.getTlsFeatureSupport(), 2);

		specAsString.append("\tSpecifications regarding TLSv1.3" + System.lineSeparator());
		StringHelper.appendHashMapToStringBuilder(specAsString, "Handshake Mode Support",
				tlsv1_3Spec.getHandshakeModeSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "PSK Mode Support",
				tlsv1_3Spec.getPSKModeSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "CipherSuite Support",
				tlsv1_3Spec.getCipherSuiteSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "DH Group Support", tlsv1_3Spec.getDHGroupSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "Handshake Signature Scheme Support",
				tlsv1_3Spec.getHandshakeSignAlgorithmSupport(), 2);
		StringHelper.appendHashMapToStringBuilder(specAsString, "Certificate Signature Scheme Support",
				tlsv1_3Spec.getCertificateSignAlgorithmSupport(), 2);

		StringHelper.appendHashMapToStringBuilder(specAsString, "Minimum Key Lengths", tlsMinimumKeyLength, 1);
		StringHelper.appendHashMapToStringBuilder(specAsString, "Elliptic Curves Support", supportedEllipticCurves,
				1);

		return specAsString.toString();
	}

	/**
	 * Parse a JAXB generated TLSSpecification data structure from XML into an internal TlsSpecification data structure.
	 *
	 * @param specification JAXB generated TLSSpecification data structure from XML
	 * @return an internal TlsSpecification data structure
	 */
	public static TlsSpecification parseFromJaxb(final TLSSpecification specification) {
		// Empty TLS specification.
		var tlsSpec = new TlsSpecification(specification.getId(), specification.getMetadata());

		// Get base specification information and set as attributes.
		var baseSpec = specification.getBaseSpecification();
		if (baseSpec != null) {
			var innerSpec = parseFromJaxb(baseSpec.getTLSSpecification());

			if (innerSpec.tlsVersionSupport != null) {
				tlsSpec.tlsVersionSupport = innerSpec.tlsVersionSupport;
			}
			if (innerSpec.tlsSessionLifetime != null) {
				tlsSpec.tlsSessionLifetime = innerSpec.tlsSessionLifetime;
			}
			if (innerSpec.supportedEllipticCurves != null) {
				tlsSpec.supportedEllipticCurves = innerSpec.supportedEllipticCurves;
			}
			if (innerSpec.tlsMinimumKeyLength != null) {
				tlsSpec.tlsMinimumKeyLength = innerSpec.tlsMinimumKeyLength;
			}
			if (innerSpec.tlsv1_2Spec != null) {
				tlsSpec.tlsv1_2Spec = innerSpec.tlsv1_2Spec;
			}
			if (innerSpec.tlsv1_3Spec != null) {
				tlsSpec.tlsv1_3Spec = innerSpec.tlsv1_3Spec;
			}
		}

		// Read further specifications and merge with base specification.
		var furtherSpec = specification.getFurtherSpecifications();
		if (furtherSpec != null) {
			// Merge supported TLSVersions
			tlsSpec.mergeTlsVersions(furtherSpec.getTLSVersions());

			// Merge tlsSessionLifetime
			tlsSpec.mergeTlsSessionLifeTime(furtherSpec.getTlsSessionLifetime());

			// Merge TLS 1.2 Spec
			tlsSpec.mergeTls12Spec(furtherSpec.getTLS12Parameter());

			// Merge TLS 1.3 Spec
			tlsSpec.mergeTls13Spec(furtherSpec.getTLS13Parameter());

			// Merge TLS Elliptic Curves
			tlsSpec.mergeTlsEllipticCurves(furtherSpec.getTLSEllipticCurves());

			// Merge TLS Minimum Key Lengths
			tlsSpec.mergeTlsMinimumKeyLength(furtherSpec.getTLSMinimumKeyLength());
		}

		return tlsSpec;
	}

	private TlsSpecification(final String id, final Metadata metadata) {
		this.id = id;
		title = metadata.getTitle();
		version = metadata.getVersion();
		references = metadata.getReferences().getReference();
	}

	/**
	 * Returns the ID of the TLS Specification.
	 * @return the ID of the TLS Specification.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the Title of the TLS Specification.
	 * @return the Title of the TLS Specification.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the Version of the TLS Specification.
	 * @return the Version of the TLS Specification.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns a list of references regarding the TLS Specification.
	 * @return a list of references regarding the TLS Specification.
	 */
	public List<String> getReferences() {
		return new ArrayList<>(references);
	}

	/**
	 * Returns a List of available TLS Versions.
	 * @return A List of available TLS Versions.
	 */
	public HashMap<String, TlsVersion> getTlsVersionSupport() {
		return new HashMap<>(tlsVersionSupport);
	}

	/**
	 * Returns the TLS Session Lifetime time span.
	 * @return TLS Session Lifetime time span.
	 */
	public Duration getTlsSessionLifetime() {
		return tlsSessionLifetime;
	}

	/**
	 * Returns the contained specification for TLSv1.2.
	 * @return the contained specification for TLSv1.2.
	 */
	public TLSv1_2Spec getTlsv1_2Spec() {
		return tlsv1_2Spec;
	}

	/**
	 * Returns the contained specification for TLSv1.3.
	 * @return the contained specification for TLSv1.3.
	 */
	public TLSv1_3Spec getTlsv1_3Spec() {
		return tlsv1_3Spec;
	}

	/**
	 * Returns the contained minimal key lengths.
	 * @return the contained minimal key lengths.
	 */
	public HashMap<String, AlgorithmMinimumKeyLengths> getTlsMinimumKeyLength() {
		return new HashMap<>(tlsMinimumKeyLength);
	}

	/**
	 * Returns the supported elliptic curves.
	 * @return the supported elliptic curves.
	 */
	public HashMap<String, EllipticCurve> getSupportedEllipticCurves() {
		return new HashMap<>(supportedEllipticCurves);
	}

	private void mergeTlsVersions(final TLSVersions tlsVersions) {
		if (tlsVersions == null || tlsVersions.getTLSVersion() == null || tlsVersions.getTLSVersion().isEmpty()) {
			return;
		}
		if (tlsVersionSupport == null) {
			tlsVersionSupport = new HashMap<>();
		}
		var baseRestrictionLevel = RestrictionLevel.fromUseType(tlsVersions.getUse());
		for (var tlsVersionItem : tlsVersions.getTLSVersion()) {
			var restrictionLevel = tlsVersionItem.getUse() != null
					? RestrictionLevel.fromUseType(tlsVersionItem.getUse())
					: baseRestrictionLevel;
			var tlsVersion = new TlsVersion(restrictionLevel, tlsVersionItem.getValue());
			tlsVersionSupport.put(tlsVersion.getTlsVersionEnum().getName(), tlsVersion);
		}
		return;
	}

	private void mergeTls12Spec(final TLS12Parameter tls12Param) {
		if (tls12Param == null) {
			return;
		}
		if (tlsv1_2Spec == null) {
			tlsv1_2Spec = new TLSv1_2Spec();
		}
		tlsv1_2Spec.mergeTls12Param(tls12Param);
		return;
	}

	private void mergeTls13Spec(final TLS13Parameter tls13Param) {
		if (tls13Param == null) {
			return;
		}
		if (tlsv1_3Spec == null) {
			tlsv1_3Spec = new TLSv1_3Spec();
		}
		tlsv1_3Spec.mergeTls13Param(tls13Param);
		return;
	}

	private void mergeTlsMinimumKeyLength(final TLSMinimumKeyLength minKeyLength) {
		if (minKeyLength == null) {
			return;
		}

		if (tlsMinimumKeyLength == null) {
			tlsMinimumKeyLength = new HashMap<>();
		}
		var baseReference = "";
		if (minKeyLength.getMetadata().getReferences() != null
				&& !minKeyLength.getMetadata().getReferences().getReference().isEmpty()) {
			baseReference = minKeyLength.getMetadata().getReferences().getReference().get(0);
		}
		for (var algo : minKeyLength.getMinimumKeyLengths().getAlgorithm()) {
			var algoType = algo.getType();
			var minKeyList = new ArrayList<AlgorithmMinimumKeyLengths.KeyLengthSpecifier>();
			for (var minKeyItem : algo.getMinimumKeyLength()) {
				String useUntil = minKeyItem.getUseUntil() != null ? minKeyItem.getUseUntil() : "";
				var keyLength = minKeyItem.getValue().intValueExact();
				minKeyList.add(new KeyLengthSpecifier(useUntil, keyLength));
			}
			var algoMinKeyLength = new AlgorithmMinimumKeyLengths(baseReference, algoType, minKeyList);
			tlsMinimumKeyLength.put(algoMinKeyLength.algorithmName, algoMinKeyLength);
		}

		return;
	}

	private void mergeTlsEllipticCurves(final TLSEllipticCurves ellipticCurves) {
		if (ellipticCurves == null) {
			return;
		}
		if (supportedEllipticCurves == null) {
			supportedEllipticCurves = new HashMap<>();
		}
		var baseReference = "";
		if (ellipticCurves.getMetadata().getReferences() != null
				&& !ellipticCurves.getMetadata().getReferences().getReference().isEmpty()) {
			baseReference = ellipticCurves.getMetadata().getReferences().getReference().get(0) + " / ";
		}
		for (var eccItem : ellipticCurves.getECC()) {
			var restrictionLevel = RestrictionLevel.fromUseType(eccItem.getUse());
			var priority = eccItem.getPriority() != null ? eccItem.getPriority().intValueExact() : 3;
			var description = eccItem.getDescription() != null ? eccItem.getDescription() : "";
			var reference = eccItem.getReference() != null ? baseReference + eccItem.getReference() : baseReference;

			var useUntil = eccItem.getUseUntil() != null ? eccItem.getUseUntil() : "";
			var identifierValue = eccItem.getValue() != null ? eccItem.getValue() : "";

			var ecc = new EllipticCurve(restrictionLevel, description, identifierValue, reference, useUntil, priority);
			supportedEllipticCurves.put(ecc.description, ecc);
		}
		return;
	}

	private void mergeTlsSessionLifeTime(final String tlsSessionLifetime) {
		if (tlsSessionLifetime == null || tlsSessionLifetime.isBlank()) {
			return;
		}
		try {
			this.tlsSessionLifetime = StringHelper.getDurationFromString(tlsSessionLifetime);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Illegal tlsSessionLifetime: " + tlsSessionLifetime, e);
		}
		return;
	}
}
