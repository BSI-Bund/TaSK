package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls12;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;


/**
 * Representation of a TLS Signature Algorithms extension.
 *
 * @see "https://tools.ietf.org/html/rfc5246#section-7.4.1.4.1"
 */
public class TlsExtSignatureAlgorithms extends TlsExtension {

	private final List<TlsSignatureAlgorithmWithHash> supportedSignatureAlgorithms
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
	 * @param signatureAlgorithmWithHash Signature Algorithm with Hash algorithm
	 */
	public final void addSupportedSignatureAlgorithm(TlsSignatureAlgorithmWithHash signatureAlgorithmWithHash) {
		supportedSignatureAlgorithms.add(signatureAlgorithmWithHash);
	}


	@Override
	protected final byte[] getData() {
		final int length = 2 * supportedSignatureAlgorithms.size();
		final ByteBuffer buffer = ByteBuffer.allocate(2 + length);
		buffer.putShort((short) length);
		for (TlsSignatureAlgorithmWithHash supportedSignatureAlgorithm : supportedSignatureAlgorithms) {
			buffer.put(supportedSignatureAlgorithm.convertToBytes());
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
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha512));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha384));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha256));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha224));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha1));


		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha512));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha384));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha256));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha224));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha1));

		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA256));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA384));
		extension.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA512));

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
						extSignatureAlgorithms.addSupportedSignatureAlgorithm(new TlsSignatureAlgorithmWithHashTls12(signature, hash));
						return extSignatureAlgorithms;
					}
				} catch (Exception e) {
					// Ignore
				}

			}
		}
		return null;

	}
}
