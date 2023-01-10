package com.achelos.task.xmlparser.datastructures.tlsspecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.xmlparser.configparsing.StringHelper;

import generated.jaxb.configuration.CipherSuites;
import generated.jaxb.configuration.TLS12Parameter;
import generated.jaxb.configuration.TLS12Parameter.MiscellaneousTLSFeatures;
import generated.jaxb.configuration.TLS12Parameter.SignatureAlgorithms;
import generated.jaxb.configuration.TLS12Parameter.SupportedGroups;

/**
 * Internal data structure representing the specifications regarding TLSv1.2 in an Application Specification.
 */
public class TLSv1_2Spec {
	private String id;
	private String title;
	private String version;
	private List<String> references;
	private final HashMap<String, CipherSuite> cipherSuiteSupport;
	private final HashMap<String, DiffHellGroup> dHGroupSupport;
	private final HashMap<String, SignatureAlgorithm> signAlgorithmSupport;
	private final HashMap<String, TlsFeature> tlsFeatureSupport;

	/**
	 * Default Constructor creating an empty TLSv1.2 Specification.
	 */
	public TLSv1_2Spec() {
		id = "";
		title = "";
		version = "";
		references = new ArrayList<>();
		cipherSuiteSupport = new HashMap<>();
		dHGroupSupport = new HashMap<>();
		signAlgorithmSupport = new HashMap<>();
		tlsFeatureSupport = new HashMap<>();
	}

	/**
	 * Merge the TLSv1.2 Parameter object into the stored one.
	 */
	public void mergeTls12Param(final TLS12Parameter tls12Param) {
		if (tls12Param == null) {
			return;
		}
		id = tls12Param.getId() != null ? tls12Param.getId() : id;
		var metadata = tls12Param.getMetadata();
		if (metadata != null) {
			title = metadata.getTitle() != null ? metadata.getTitle() : title;
			version = metadata.getVersion() != null ? metadata.getVersion() : version;
			references
					= metadata.getReferences() != null ? metadata.getReferences().getReference() : references;
		}

		// Merge contained elements from TLS12Param.
		for (var cipherSuites : tls12Param.getCipherSuites()) {
			mergeCipherSuites(cipherSuites);
		}
		mergeSupportedGroups(tls12Param.getSupportedGroups());
		mergeSignatureAlgorithms(tls12Param.getSignatureAlgorithms());
		mergeMiscellaneousTLSFeatures(tls12Param.getMiscellaneousTLSFeatures());
	}

	/**
	 * Return the id of the specification.
	 * @return the id of the specification.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the title of the specification.
	 * @return the title of the specification.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return the version of the specification.
	 * @return the version of the specification.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns a list of references contained in the specification.
	 * @return a list of references contained in the specification.
	 */
	public List<String> getReferences() {
		return new ArrayList<>(references);
	}

	/**
	 * Returns information about CipherSuite support.
	 * @return information about CipherSuite support.
	 */
	public HashMap<String, CipherSuite> getCipherSuiteSupport() {
		return new HashMap<>(cipherSuiteSupport);
	}

	/**
	 * Returns information about DH Group support.
	 * @return information about DH Group support.
	 */
	public HashMap<String, DiffHellGroup> getDHGroupSupport() {
		return new HashMap<>(dHGroupSupport);
	}

	/**
	 * Returns information about SignatureAlgorithm support.
	 * @return information about SignatureAlgorithm support.
	 */
	public HashMap<String, SignatureAlgorithm> getSignAlgorithmSupport() {
		return new HashMap<>(signAlgorithmSupport);
	}

	/**
	 * Returns information about other TLS feature support.
	 * @return information about other TLS feature support.
	 */
	public HashMap<String, TlsFeature> getTlsFeatureSupport() {
		return new HashMap<>(tlsFeatureSupport);
	}

	private void mergeCipherSuites(final CipherSuites cipherSuites) {
		if (cipherSuites == null) {
			return;
		}
		if (cipherSuites.isFullList()) {
			cipherSuiteSupport.clear();
		}
		var baseReference = getBaseReference();
		baseReference = cipherSuites.getReference() != null ? baseReference + cipherSuites.getReference() + " / "
				: baseReference;
		var type = cipherSuites.getType() != null ? cipherSuites.getType() : "";
		var baseRestriction = RestrictionLevel.fromUseType(cipherSuites.getUse());

		for (var cipherSuite : cipherSuites.getCipherSuite()) {
			var description = cipherSuite.getDescription() != null ? cipherSuite.getDescription() : "";
			var identifierValue = cipherSuite.getValue() != null ? cipherSuite.getValue() : "";
			var useUntil = cipherSuite.getUseUntil() != null ? cipherSuite.getUseUntil() : "";
			var restriction = cipherSuite.getUse() != null ? RestrictionLevel.fromUseType(cipherSuite.getUse())
					: baseRestriction;
			var reference
					= cipherSuite.getReference() != null ? baseReference + cipherSuite.getReference() : baseReference;
			var priority = cipherSuite.getPriority() != null ? cipherSuite.getPriority().intValueExact() : 3;

			var cipherSuiteItem
					= new CipherSuite(restriction, description, identifierValue, reference, useUntil, priority, type);
			cipherSuiteSupport.put(cipherSuiteItem.description, cipherSuiteItem);
		}

	}

	private void mergeSupportedGroups(final SupportedGroups supportedGroups) {
		if (supportedGroups == null) {
			return;
		}
		var baseReference = getBaseReference();
		baseReference = supportedGroups.getReference() != null ? baseReference + supportedGroups.getReference() + " / "
				: baseReference;
		var baseRestriction = RestrictionLevel.fromUseType(supportedGroups.getUse());

		for (var dhGroup : supportedGroups.getDHGroup()) {
			var description = dhGroup.getDescription() != null ? dhGroup.getDescription() : "";
			var identifierValue = dhGroup.getValue() != null ? dhGroup.getValue() : "";
			var useUntil = dhGroup.getUseUntil() != null ? dhGroup.getUseUntil() : "";
			var priority = dhGroup.getPriority() != null ? dhGroup.getPriority().intValueExact() : 3;
			var reference = dhGroup.getReference() != null ? baseReference + dhGroup.getReference() : baseReference;
			var restriction
					= dhGroup.getUse() != null ? RestrictionLevel.fromUseType(dhGroup.getUse()) : baseRestriction;

			var dhGroupItem = new DiffHellGroup(restriction, description, identifierValue, reference, useUntil, priority);

			dHGroupSupport.put(dhGroupItem.description, dhGroupItem);
		}
	}

	private void mergeSignatureAlgorithms(final SignatureAlgorithms signAlgorithms) {
		if (signAlgorithms == null) {
			return;
		}
		var baseReference = getBaseReference();
		baseReference = signAlgorithms.getReference() != null ? baseReference + signAlgorithms.getReference() + " / "
				: baseReference;
		var baseRestriction = RestrictionLevel.fromUseType(signAlgorithms.getUse());

		for (var signAlgo : signAlgorithms.getSignatureAlgorithm()) {
			String description = "";
			description = StringHelper.combineSignAndHashAlgorithm(signAlgo.getSignatureAlgorithmName(),
					signAlgo.getDigestAlgorithmName());
			var identifierValue = signAlgo.getSignatureAlgorithmValue() + ";" + signAlgo.getDigestAlgorithmValue();
			var useUntil = signAlgo.getUseUntil() != null ? signAlgo.getUseUntil() : "";
			var priority = signAlgo.getPriority() != null ? signAlgo.getPriority().intValueExact() : 3;
			var reference = signAlgo.getReference() != null ? baseReference + signAlgo.getReference() : baseReference;
			var restriction
					= signAlgo.getUse() != null ? RestrictionLevel.fromUseType(signAlgo.getUse()) : baseRestriction;

			var signAlgoItem
					= new SignatureAlgorithm(restriction, description, identifierValue, reference, useUntil, priority);

			signAlgorithmSupport.put(signAlgoItem.description, signAlgoItem);
		}
	}

	private void mergeMiscellaneousTLSFeatures(final MiscellaneousTLSFeatures tlsFeatures) {
		if (tlsFeatures == null) {
			return;
		}
		var baseReference = getBaseReference();
		for (var tlsFeatureItem : tlsFeatures.getTLSFeature()) {
			var reference = tlsFeatureItem.getReference() != null ? baseReference + tlsFeatureItem.getReference()
					: baseReference;
			var restriction = RestrictionLevel.fromUseType(tlsFeatureItem.getUse());
			var featureId = tlsFeatureItem.getValue() != null ? tlsFeatureItem.getValue() : "";

			var tlsFeature = new TlsFeature(reference, featureId, restriction);

			tlsFeatureSupport.put(tlsFeature.identifier, tlsFeature);
		}
	}

	private String getBaseReference() {
		var baseReference = "";
		if (!references.isEmpty()) {
			baseReference = references.get(0) + " / ";
		}
		return baseReference;
	}

}
