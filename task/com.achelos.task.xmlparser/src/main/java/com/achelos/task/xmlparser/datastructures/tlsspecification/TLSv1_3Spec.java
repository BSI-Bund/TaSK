package com.achelos.task.xmlparser.datastructures.tlsspecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import generated.jaxb.configuration.CipherSuites;
import generated.jaxb.configuration.TLS13Parameter;
import generated.jaxb.configuration.TLS13Parameter.HandshakeModes;
import generated.jaxb.configuration.TLS13Parameter.PSKModes;
import generated.jaxb.configuration.TLS13Parameter.SignatureAlgorithmsCertificate;
import generated.jaxb.configuration.TLS13Parameter.SignatureAlgorithmsHandshake;
import generated.jaxb.configuration.TLS13Parameter.SupportedGroups;

/**
 * Internal data structure representing the specifications regarding TLSv1.3 in an Application Specification.
 */
public class TLSv1_3Spec {
	private String id;
	private String title;
	private String version;
	private List<String> references;

	private final HashMap<String, HandshakeMode> handshakeModeSupport;
	private final HashMap<String, PSKMode> pSKModeSupport;
	private final HashMap<String, CipherSuite> cipherSuiteSupport;
	private final HashMap<String, DiffHellGroup> dHGroupSupport;
	private final HashMap<String, SignatureAlgorithm> handshakeSignAlgorithmSupport;
	private final HashMap<String, SignatureAlgorithm> certificateSignAlgorithmSupport;

	/**
	 * Default Constructor creating an empty TLSv1.3 Specification.
	 */
	public TLSv1_3Spec() {
		id = "";
		title = "";
		version = "";

		handshakeModeSupport = new HashMap<>();
		pSKModeSupport = new HashMap<>();
		references = new ArrayList<>();
		cipherSuiteSupport = new HashMap<>();
		dHGroupSupport = new HashMap<>();
		handshakeSignAlgorithmSupport = new HashMap<>();
		certificateSignAlgorithmSupport = new HashMap<>();
	}

	/**
	 * Merge the TLSv1.3 Parameter object into the stored one.
	 */
	public void mergeTls13Param(final TLS13Parameter tls13Param) {
		if (tls13Param == null) {
			return;
		}
		id = tls13Param.getId() != null ? tls13Param.getId() : id;
		var metadata = tls13Param.getMetadata();
		if (metadata != null) {
			title = metadata.getTitle() != null ? metadata.getTitle() : title;
			version = metadata.getVersion() != null ? metadata.getVersion() : version;
			references = metadata.getReferences() != null ? metadata.getReferences().getReference()
					: references;
		}

		// Merge contained elements from TLS13Param.
		mergeHandshakeModes(tls13Param.getHandshakeModes());
		mergePSKModes(tls13Param.getPSKModes());
		for (var cipherSuites : tls13Param.getCipherSuites()) {
			mergeCipherSuites(cipherSuites);
		}
		mergeSupportedGroups(tls13Param.getSupportedGroups());

		mergeSignatureAlgorithmsHandshake(tls13Param.getSignatureAlgorithmsHandshake());
		mergeSignatureAlgorithmsCertificate(tls13Param.getSignatureAlgorithmsCertificate());
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
	 * Returns information about Handshake Mode support.
	 * @return information about Handshake Mode support.
	 */
	public HashMap<String, HandshakeMode> getHandshakeModeSupport() {
		return new HashMap<>(handshakeModeSupport);
	}

	/**
	 * Returns information about PSK Mode support.
	 * @return information about PSK Mode support.
	 */
	public HashMap<String, PSKMode> getPSKModeSupport() {
		return new HashMap<>(pSKModeSupport);
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
	 * Returns information about Handshake Signature Algorithm support.
	 * @return information about Handshake Signature Algorithm  support.
	 */
	public HashMap<String, SignatureAlgorithm> getHandshakeSignAlgorithmSupport() {
		return new HashMap<>(handshakeSignAlgorithmSupport);
	}

	/**
	 * Returns information about Certificate Signature Algorithm support.
	 * @return information about Certificate Signature Algorithm support.
	 */
	public HashMap<String, SignatureAlgorithm> getCertificateSignAlgorithmSupport() {
		return new HashMap<>(certificateSignAlgorithmSupport);
	}

	private void mergeHandshakeModes(final HandshakeModes handshakeModes) {
		if (handshakeModes == null) {
			return;
		}
		var baseRestriction = RestrictionLevel.fromUseType(handshakeModes.getUse());
		var baseReference = getBaseReference();
		baseReference = handshakeModes.getReference() != null ? baseReference + handshakeModes.getReference() + " / "
				: baseReference;

		for (var handshakeModeItem : handshakeModes.getHandshakeMode()) {
			var support = handshakeModeItem.getSupport() != null ? handshakeModeItem.getSupport() : "";
			var restriction = handshakeModeItem.getUse() != null
					? RestrictionLevel.fromUseType(handshakeModeItem.getUse())
					: baseRestriction;
			var handshakeIdentifier = handshakeModeItem.getValue() != null ? handshakeModeItem.getValue() : "";

			var handshakeMode = new HandshakeMode(handshakeIdentifier, restriction, support, baseReference);
			handshakeModeSupport.put(handshakeMode.handshakeIdentifier, handshakeMode);
		}
	}

	private void mergePSKModes(final PSKModes pskModes) {
		if (pskModes == null) {
			return;
		}
		var baseRestriction = RestrictionLevel.fromUseType(pskModes.getUse());
		var baseReference = getBaseReference();
		baseReference = pskModes.getReference() != null ? baseReference + pskModes.getReference() + " / "
				: baseReference;

		for (var pskModeItem : pskModes.getPSKMode()) {
			var description = pskModeItem.getDescription() != null ? pskModeItem.getDescription() : "";
			var identifierValue = pskModeItem.getValue() != null ? pskModeItem.getValue() : "";
			var useUntil = pskModeItem.getUseUntil() != null ? pskModeItem.getUseUntil() : "";
			var priority = pskModeItem.getPriority() != null ? pskModeItem.getPriority().intValueExact() : 3;
			var reference = pskModeItem.getReference() != null ? baseReference + pskModeItem.getReference()
					: baseReference;
			var restriction = pskModeItem.getUse() != null ? RestrictionLevel.fromUseType(pskModeItem.getUse())
					: baseRestriction;

			var pskMode = new PSKMode(restriction, description, identifierValue, reference, useUntil, priority);

			pSKModeSupport.put(pskMode.description, pskMode);
		}
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
			var reference = cipherSuite.getReference() != null ? baseReference + cipherSuite.getReference()
					: baseReference;
			var priority = cipherSuite.getPriority() != null ? cipherSuite.getPriority().intValueExact() : 3;

			var cipherSuiteItem = new CipherSuite(restriction, description, identifierValue, reference, useUntil, priority,
					type);
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
			var restriction = dhGroup.getUse() != null ? RestrictionLevel.fromUseType(dhGroup.getUse())
					: baseRestriction;

			var dhGroupItem = new DiffHellGroup(restriction, description, identifierValue, reference, useUntil, priority);

			dHGroupSupport.put(dhGroupItem.description, dhGroupItem);
		}
	}

	private void mergeSignatureAlgorithmsHandshake(final SignatureAlgorithmsHandshake signatureAlgorithmsHandshake) {
		if (signatureAlgorithmsHandshake == null) {
			return;
		}
		var baseReference = getBaseReference();
		baseReference = signatureAlgorithmsHandshake.getReference() != null
				? baseReference + signatureAlgorithmsHandshake.getReference() + " / "
				: baseReference;
		var baseRestriction = RestrictionLevel.fromUseType(signatureAlgorithmsHandshake.getUse());

		for (var signAlgo : signatureAlgorithmsHandshake.getSignatureScheme()) {
			var description = signAlgo.getDescription() != null ? signAlgo.getDescription() : "";
			var identifierValue = signAlgo.getValue() != null ? signAlgo.getValue() : "";
			var useUntil = signAlgo.getUseUntil() != null ? signAlgo.getUseUntil() : "";
			var priority = signAlgo.getPriority() != null ? signAlgo.getPriority().intValueExact() : 3;
			var reference = signAlgo.getReference() != null ? baseReference + signAlgo.getReference() : baseReference;
			var restriction = signAlgo.getUse() != null ? RestrictionLevel.fromUseType(signAlgo.getUse())
					: baseRestriction;

			var signAlgoItem = new SignatureAlgorithm(restriction, description, identifierValue, reference, useUntil,
					priority);

			handshakeSignAlgorithmSupport.put(signAlgoItem.description, signAlgoItem);
		}
	}

	private void mergeSignatureAlgorithmsCertificate(final SignatureAlgorithmsCertificate signAlgorithmsCert) {
		if (signAlgorithmsCert == null) {
			return;
		}
		var baseReference = getBaseReference();
		baseReference = signAlgorithmsCert.getReference() != null ? baseReference + signAlgorithmsCert.getReference() + " / "
				: baseReference;
		var baseRestriction = RestrictionLevel.fromUseType(signAlgorithmsCert.getUse());

		for (var signAlgo : signAlgorithmsCert.getSignatureScheme()) {
			var description = signAlgo.getDescription() != null ? signAlgo.getDescription() : "";
			var identifierValue = signAlgo.getValue() != null ? signAlgo.getValue() : "";
			var useUntil = signAlgo.getUseUntil() != null ? signAlgo.getUseUntil() : "";
			var priority = signAlgo.getPriority() != null ? signAlgo.getPriority().intValueExact() : 3;
			var reference = signAlgo.getReference() != null ? baseReference + signAlgo.getReference() : baseReference;
			var restriction = signAlgo.getUse() != null ? RestrictionLevel.fromUseType(signAlgo.getUse())
					: baseRestriction;

			var signAlgoItem = new SignatureAlgorithm(restriction, description, identifierValue, reference, useUntil,
					priority);

			certificateSignAlgorithmSupport.put(signAlgoItem.description, signAlgoItem);
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
