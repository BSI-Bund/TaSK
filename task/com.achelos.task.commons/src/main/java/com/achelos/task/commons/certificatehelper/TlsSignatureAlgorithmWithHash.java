package com.achelos.task.commons.certificatehelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;


/**
 * Wrapper for either {@link TlsSignatureAlgorithm} with {@link TlsHashAlgorithm} or a single {@link TlsSignatureScheme}
 * instance.
 */
public abstract class TlsSignatureAlgorithmWithHash {
	private final TlsSignatureAlgorithm signatureAlgorithm;
	private final TlsHashAlgorithm hashAlgorithm;

	// abstract class
	/**
	 * Constructor using an instance of {@link TlsSignatureAlgorithm} and {@link TlsHashAlgorithm}.
	 * @param signatureAlgorithm The TLS Signature Algorithm to store.
	 * @param hashAlgorithm The Hash Algorithm to store.
	 */
	public TlsSignatureAlgorithmWithHash(final TlsSignatureAlgorithm signatureAlgorithm,
			final TlsHashAlgorithm hashAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
		this.hashAlgorithm = hashAlgorithm;
	}


	/**
	 * If the Object is not of type "Signature Scheme", returns the stored TLS Signature Algorithm.
	 * @return The stored TLS Signature Algorithm
	 * @throws NullPointerException If the Object is of type "Signature Scheme".
	 */
	public TlsSignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	/**
	 * If the Object is not of type "Signature Scheme", returns the stored Hash Algorithm.
	 * @return The stored Hash Algorithm
	 * @throws NullPointerException If the Object is of type "Signature Scheme".
	 */
	public TlsHashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	/**
	 * Returns the information whether the Object is of type "Signature Scheme".
	 * @return the information whether the Object is of type "Signature Scheme".
	 */
	public abstract boolean isSignatureScheme();


	/**
	 * Returns a String representation of this object.
	 * @return a String representation of this object.
	 */
	@Override
	public final String toString() {
		return "[" + signatureAlgorithm.toString() + " " + hashAlgorithm.toString() + "]";
	}

	/**
	 * Returns the hashCode of this object.
	 * @return the hashCode of this object.
	 */
	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns true, if the obj is an instance of TlsSignatureAlgorithmWithHash and the stored values are the same.
	 * @param obj Instance of TlsSignatureAlgorithmWithHash
	 * @return true, if the obj is an instance of TlsSignatureAlgorithmWithHash and the stored values are the same.
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (!(obj instanceof TlsSignatureAlgorithmWithHash)) {
			return false;
		}
		TlsSignatureAlgorithmWithHash other = (TlsSignatureAlgorithmWithHash) obj;
		return signatureAlgorithm.equals(other.signatureAlgorithm)
				&& hashAlgorithm.equals(other.hashAlgorithm);
	}

	public abstract byte[] convertToBytes();

}