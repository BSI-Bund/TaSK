package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;


/**
 * Representation of a TLS Signature Algorithms extension.
 *
 * @see https://tools.ietf.org/html/rfc5246#section-7.4.1.4.1
 */
public class TlsExtSignatureAlgorithms extends TlsExtension {
	/**
	 * Signature And HashAlgorithm Representation.
	 */
	private static final class SignatureAndHashAlgorithm {
		private final TlsHashAlgorithm hash;
		private final TlsSignatureAlgorithm signature;

		/**
		 * Default constructor.
		 *
		 * @param hash The hash.
		 * @param signature The signature.
		 */
		private SignatureAndHashAlgorithm(final TlsHashAlgorithm hash, final TlsSignatureAlgorithm signature) {
			this.hash = hash;
			this.signature = signature;
		}


		/**
		 * @return Algorithm hash and signature value in byte array.
		 */
		private byte[] getData() {
			return new byte[] {hash.getValue(), signature.getValue()};
		}
	}

	private final List<SignatureAndHashAlgorithm> supportedSignatureAlgorithms
			= new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public TlsExtSignatureAlgorithms() {
		super(TlsExtensionTypes.signature_algorithms);
	}


	/**
	 * Add a supported signature algorithm.
	 *
	 * @param hash Hash algorithm
	 * @param signature Signature algorithm
	 */
	public final void addSupportedSignatureAlgorithm(final TlsHashAlgorithm hash,
			final TlsSignatureAlgorithm signature) {
		supportedSignatureAlgorithms.add(new SignatureAndHashAlgorithm(hash, signature));
	}


	@Override
	protected final byte[] getData() {
		final int length = 2 * supportedSignatureAlgorithms.size();
		final ByteBuffer buffer = ByteBuffer.allocate(2 + length);
		buffer.putShort((short) length);
		for (SignatureAndHashAlgorithm supportedSignatureAlgorithm : supportedSignatureAlgorithms) {
			buffer.put(supportedSignatureAlgorithm.getData());
		}
		buffer.flip();
		return buffer.array();
	}


	/**
	 * Creates default supported Signature Algorithms.
	 *
	 * @return Representation of a TLS Signature Algorithms extension.
	 */
	public static final TlsExtSignatureAlgorithms createDefault() {
		final TlsExtSignatureAlgorithms extension = new TlsExtSignatureAlgorithms();
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha512, TlsSignatureAlgorithm.ecdsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha512, TlsSignatureAlgorithm.rsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha384, TlsSignatureAlgorithm.ecdsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha384, TlsSignatureAlgorithm.rsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha256, TlsSignatureAlgorithm.ecdsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha256, TlsSignatureAlgorithm.rsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha224, TlsSignatureAlgorithm.ecdsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha224, TlsSignatureAlgorithm.rsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha1, TlsSignatureAlgorithm.ecdsa);
		extension.addSupportedSignatureAlgorithm(TlsHashAlgorithm.sha1, TlsSignatureAlgorithm.rsa);
		return extension;
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {

		final byte[] signatureAlgorithms = data;

		if (signatureAlgorithms != null) {
			// First 2 bytes is the length of the buffer
			for (int i = 2; i < signatureAlgorithms.length; i += 2) {
				try {
					TlsHashAlgorithm hash = TlsHashAlgorithm.getElement(signatureAlgorithms[i]);
					TlsSignatureAlgorithm signature = TlsSignatureAlgorithm.getElement(signatureAlgorithms[i + 1]);
					if (hash != null && signature != null) {
						TlsExtSignatureAlgorithms extSignatureAlgorithms = new TlsExtSignatureAlgorithms();
						extSignatureAlgorithms.addSupportedSignatureAlgorithm(hash, signature);
						return extSignatureAlgorithms;
					}
				} catch (Exception e) {
					// Ignore
				}

			}
		}
		return null;

	}


	/**
	 * Create the desired signature algorithm extension.
	 *
	 * @param hashAlgorithms The hash algorithms.
	 * @param signatureAlgorithm The signature algorithms.
	 * @return the desired signature algorithm extension.
	 */
	public static TlsExtSignatureAlgorithms create(final List<TlsHashAlgorithm> hashAlgorithms,
			final TlsSignatureAlgorithm signatureAlgorithm) {
		final TlsExtSignatureAlgorithms extension = new TlsExtSignatureAlgorithms();
		for (TlsHashAlgorithm hashAlgorithm : hashAlgorithms) {
			extension.addSupportedSignatureAlgorithm(hashAlgorithm, signatureAlgorithm);
		}
		return extension;
	}
}
