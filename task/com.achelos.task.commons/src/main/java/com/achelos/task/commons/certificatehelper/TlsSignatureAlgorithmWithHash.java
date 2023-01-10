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
public class TlsSignatureAlgorithmWithHash {
	private TlsSignatureAlgorithm signatureAlgorithm;
	private TlsHashAlgorithm hashAlgorithm;
	private final boolean isSignatureScheme;
	private TlsSignatureScheme signatureScheme;

	/**
	 * Constructor using an instance of {@link TlsSignatureAlgorithm} and {@link TlsHashAlgorithm}.
	 * @param signatureAlgorithm The TLS Signature Algorithm to store.
	 * @param hashAlgorithm The Hash Algorithm to store.
	 */
	public TlsSignatureAlgorithmWithHash(final TlsSignatureAlgorithm signatureAlgorithm,
			final TlsHashAlgorithm hashAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
		this.hashAlgorithm = hashAlgorithm;
		isSignatureScheme = false;
	}

	/**
	 * Constructor using an instance of {@link TlsSignatureScheme}.
	 * @param signatureScheme The TLS Signature Scheme to store.
	 */
	public TlsSignatureAlgorithmWithHash(final TlsSignatureScheme signatureScheme) {
		this.signatureScheme = signatureScheme;
		isSignatureScheme = true;
	}

	/**
	 * If the Object is not of type "Signature Scheme", returns the stored TLS Signature Algorithm.
	 * @return The stored TLS Signature Algorithm
	 * @throws NullPointerException If the Object is of type "Signature Scheme".
	 */
	public TlsSignatureAlgorithm getSignatureAlgorithm() {
		if (isSignatureScheme) {
			throw new NullPointerException(
					"SignatureAlgorithmWithHash is of type \"Signature Scheme\". The requested parameter is not available.");
		}
		return signatureAlgorithm;
	}

	/**
	 * If the Object is not of type "Signature Scheme", returns the stored Hash Algorithm.
	 * @return The stored Hash Algorithm
	 * @throws NullPointerException If the Object is of type "Signature Scheme".
	 */
	public TlsHashAlgorithm getHashAlgorithm() {
		if (isSignatureScheme) {
			throw new NullPointerException(
					"SignatureAlgorithmWithHash is of type \"Signature Scheme\". The requested parameter is not available.");
		}
		return hashAlgorithm;
	}

	/**
	 * If the Object is of type "Signature Scheme", returns the stored TLS Signature Scheme.
	 * @return The stored TLS Signature Scheme
	 * @throws NullPointerException If the Object is not of type "Signature Scheme".
	 */
	public TlsSignatureScheme getSignatureScheme() {
		if (!isSignatureScheme) {
			throw new NullPointerException(
					"SignatureAlgorithmWithHash is of type \"Signature Algorithm\". The requested parameter is not available.");
		}
		return signatureScheme;
	}

	/**
	 * Returns the information whether the Object is of type "Signature Scheme".
	 * @return the information whether the Object is of type "Signature Scheme".
	 */
	public boolean isSignatureScheme() {
		return isSignatureScheme;
	}

	/**
	 * Method takes a byte representation of one or more Signature Algorithms With Hash concatenated and finds all
	 * consisting Signature Algorithms With Hash which are returned within object representation.
	 *
	 * @param data The TlsSignatureAlgorithmWithHash list in byte representation
	 * @return the List<TlsSignatureAlgorithmWithHash>
	 */
	public static List<TlsSignatureAlgorithmWithHash> parseSignatureAlgorithmWithHashByteList(final byte[] data) {
		List<TlsSignatureAlgorithmWithHash> foundSignatureAlgorithmWithHash
				= new ArrayList<>();
		if (data != null) {
			// First 2 bytes is the length of the buffer
			for (int i = 2; i < data.length; i += 2) {
				TlsSignatureAlgorithmWithHash sigAlgo;
				try {
					sigAlgo = new TlsSignatureAlgorithmWithHash(TlsSignatureAlgorithm.getElement(data[i + 1]),
							TlsHashAlgorithm.getElement(data[i]));
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
				foundSignatureAlgorithmWithHash.add(sigAlgo);
			}
		}
		return foundSignatureAlgorithmWithHash;
	}

	/**
	 * Returns a String representation of this object.
	 * @return a String representation of this object.
	 */
	@Override
	public final String toString() {
		if (!isSignatureScheme) {
			return "[" + signatureAlgorithm.toString() + " " + hashAlgorithm.toString() + "]";
		} else {
			return "[" + signatureScheme.toString() + "]";
		}
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
				&& hashAlgorithm.equals(other.hashAlgorithm)
				&& isSignatureScheme == other.isSignatureScheme
				&& (signatureScheme == other.signatureScheme || signatureScheme != null
						&& other.signatureScheme != null && signatureScheme.equals(other.signatureScheme));

	}


	/**
	 * This function outputs all TlsSignatureAlgorithmWithHash which should be supported according to the TR-02102-2
	 * and for which we have created a server certificate
	 * @return
	 */
	public static List<TlsSignatureAlgorithmWithHash> getSupportedCertificateTypesTls12(){
		List<TlsSignatureAlgorithmWithHash> signatureAlgorithmWithHashes = new LinkedList<>();
		List<TlsHashAlgorithm> supportedHashList = new LinkedList<>(Arrays.asList(TlsHashAlgorithm.sha256, TlsHashAlgorithm.sha384, TlsHashAlgorithm.sha512));
		List<TlsSignatureAlgorithm> supportedSignatureAlgorithmList =
				new LinkedList<>(Arrays.asList( TlsSignatureAlgorithm.ecdsa, TlsSignatureAlgorithm.rsa, TlsSignatureAlgorithm.dsa));

		for (TlsSignatureAlgorithm sigAlg: supportedSignatureAlgorithmList){
			for(TlsHashAlgorithm hashAlg: supportedHashList) {
				signatureAlgorithmWithHashes.add(new TlsSignatureAlgorithmWithHash(sigAlg, hashAlg));
			}
		}
		return signatureAlgorithmWithHashes;
	}

}