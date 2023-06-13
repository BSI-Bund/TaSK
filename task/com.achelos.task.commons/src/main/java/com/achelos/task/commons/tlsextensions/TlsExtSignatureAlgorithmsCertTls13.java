package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHash;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls12;
import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
import com.achelos.task.commons.enums.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Representation of a TLS Signature Algorithms extension.
 *
 * @see "https://tools.ietf.org/html/rfc5246#section-7.4.1.4.1"
 */
public class TlsExtSignatureAlgorithmsCertTls13 extends TlsExtension {

	private final List<TlsSignatureAlgorithmWithHashTls13> supportedSignatureAlgorithmsTls13
			= new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public TlsExtSignatureAlgorithmsCertTls13() {
		super(TlsExtensionTypes.signature_algorithms_cert);
	}


	/**
	 * Add a supported signature algorithm.
	 *
	 * @param signatureAlgorithmWithHash Signature Algorithm with Hash algorithm
	 */
	public final void addSupportedSignatureAlgorithmCert(TlsSignatureAlgorithmWithHashTls13 signatureAlgorithmWithHash) {
		supportedSignatureAlgorithmsTls13.add(signatureAlgorithmWithHash);
	}


	@Override
	protected final byte[] getData() {
		final int length = 2 * supportedSignatureAlgorithmsTls13.size();
		final ByteBuffer buffer = ByteBuffer.allocate(2 + length);
		buffer.putShort((short) length);
		for (TlsSignatureAlgorithmWithHash supportedSignatureAlgorithm : supportedSignatureAlgorithmsTls13) {
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
	public static final TlsExtSignatureAlgorithmsCertTls13 createDefault() {
		final TlsExtSignatureAlgorithmsCertTls13 extension = new TlsExtSignatureAlgorithmsCertTls13();

		extension.addSupportedSignatureAlgorithmCert(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA256));
		extension.addSupportedSignatureAlgorithmCert(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA384));
		extension.addSupportedSignatureAlgorithmCert(new TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme.RSA_PSS_RSAE_SHA512));

		return extension;
	}

	@Override
	public final TlsExtension createExtension(final byte[] data) {

		var sigAlgsCert = TlsSignatureAlgorithmWithHash.parseSignatureAlgorithmWithHashByteList(data, TlsVersion.TLS_V1_3 );

		TlsExtSignatureAlgorithmsCertTls13 sigAlgCertExtension = new TlsExtSignatureAlgorithmsCertTls13();

		for(var sigAlgCert: sigAlgsCert){
			sigAlgCertExtension.addSupportedSignatureAlgorithmCert((TlsSignatureAlgorithmWithHashTls13) sigAlgCert);
		}
		return sigAlgCertExtension;
	}
}
