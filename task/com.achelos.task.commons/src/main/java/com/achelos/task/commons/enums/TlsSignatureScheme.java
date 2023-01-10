package com.achelos.task.commons.enums;

import java.util.ArrayList;

import com.achelos.task.commons.tools.StringTools;


/**
 * Public enumeration holds all Signature Scheme level descriptions (only for TLS 1.3).
 *
 * @see <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-signaturescheme">TLS TLS
 * SignatureScheme Registry</a> Note: This enum is named "SignatureScheme" because there is already a
 * "SignatureAlgorithm" type in TLS 1.2, which this replaces.
 */
public enum TlsSignatureScheme {

	/** RSASSA-PKCS1-v1_5 algorithms. **/

	/** SHA256. **/
	RSA_PKCS1_SHA256((byte) 0x04, (byte) 0x01),
	/** SHA384. **/
	RSA_PKCS1_SHA384((byte) 0x05, (byte) 0x01),
	/** SHA512. **/
	RSA_PKCS1_SHA512((byte) 0x06, (byte) 0x01),

	/** ECDSA algorithms. **/

	/** SHA256. **/
	ECDSA_SECP256R1_SHA256((byte) 0x04, (byte) 0x03),
	/** SHA384. **/
	ECDSA_SECP384R1_SHA384((byte) 0x05, (byte) 0x03),
	/** SHA512. **/
	ECDSA_SECP512R1_SHA512((byte) 0x06, (byte) 0x03),

	/** ECDSA algorithms with brainpool curve. **/

	/** SHA256. **/
	ECDSA_BRAINPOOLP256R1TLS13_SHA256((byte) 0x08, (byte) 0x1a),
	/** SHA384. **/
	ECDSA_BRAINPOOLP384R1TLS13_SHA384((byte) 0x08, (byte) 0x1b),
	/** SHA512. **/
	ECDSA_BRAINPOOLP512R1TLS13_SHA512((byte) 0x08, (byte) 0x1c),

	/** RSASSA-PSS algorithms with public key OID rsaEncryption. */

	/** SHA256. **/
	RSA_PSS_RSAE_SHA256((byte) 0x08, (byte) 0x04),
	/** SHA384. **/
	RSA_PSS_RSAE_SHA384((byte) 0x08, (byte) 0x05),
	/** SHA512. **/
	RSA_PSS_RSAE_SHA512((byte) 0x08, (byte) 0x06),

	/** EdDSA algorithms. **/

	/** 25519. **/
	ED25519((byte) 0x08, (byte) 0x07),
	/** 448. **/
	ED448((byte) 0x08, (byte) 0x08),

	/** RSASSA-PSS algorithms with public key OID RSASSA-PSS. **/

	/** SHA256. **/
	RSA_PSS_PSS_SHA256((byte) 0x08, (byte) 0x09),
	/** SHA384. **/
	RSA_PSS_PSS_SHA384((byte) 0x08, (byte) 0x0a),
	/** SHA512. **/
	RSA_PSS_PSS_SHA512((byte) 0x08, (byte) 0x0b),

	/** Legacy algorithms. **/

	/** RSASSA-PKCS1-v1_5 with SHA1. **/
	RSA_PKCS1_SHA1((byte) 0x02, (byte) 0x01),
	/** ECDSA with SHA1. **/
	ECDSA_SHA1((byte) 0x02, (byte) 0x03);

	private byte upper;
	private byte lower;

	/**
	 * Signature scheme constructor.
	 *
	 * @param upper Upper byte.
	 * @param lower Lower byte.
	 */
	TlsSignatureScheme(final byte upper, final byte lower) {
		this.upper = upper;
		this.lower = lower;
	}


	/**
	 * Return the cipher suite's value as hexadecimal pair.
	 *
	 * @return String with two hexadecimal encoded bytes prefixed with "0x", separated by comma, and enclosed in
	 * parentheses (e.g., "(0x00,0x2F)")
	 */
	public String getValuePair() {
		return String.format("(0x%02x,0x%02x)", upper, lower);
	}


	/**
	 * Return the cipher suite's value as hexadecimal string.
	 *
	 * @return String with two hexadecimal encoded bytes separated by space (e.g., "00 2F")
	 */
	public String getValueHexString() {
		return String.format("%02x %02x", upper, lower);
	}


	@Override
	public String toString() {
		return name() + ' ' + getValuePair();
	}


	/**
	 * Get the value of Tls signature scheme by upper and lower byte.
	 *
	 * @param upper The upper byte.
	 * @param lower The lower byte.
	 * @return TlsSignatureScheme
	 */
	public static TlsSignatureScheme valueOf(final byte upper, final byte lower) {
		for (TlsSignatureScheme signatureScheme : TlsSignatureScheme.values()) {
			if (signatureScheme.upper == upper && signatureScheme.lower == lower) {
				return signatureScheme;
			}
		}
		return null;
	}


	/**
	 * Get the {@link TlsSignatureScheme} object by matching hex string value.
	 *
	 * @param hexString The hex string.
	 * @return TlsSignatureScheme
	 */
	public static TlsSignatureScheme valueOfHexString(final String hexString) {
		if (null == hexString || hexString.isEmpty()) {
			return null;
		}
		final byte[] bytes = StringTools.toByteArray(hexString.replaceAll(" ", ""));

		if (1 == bytes.length) {
			return valueOf((byte) 0x00, bytes[0]);
		}
		if (2 == bytes.length) {
			return valueOf(bytes[0], bytes[1]);
		} else {
			return null;
		}
	}

	/**
	 * Method takes a byte representation of one or more Signature Algorithms With Hash concatenated and finds all
	 * consisting Signature Algorithms With Hash which are returned within object representation.
	 *
	 * @param data The TlsSignatureScheme list in byte representation
	 * @return the List<TlsSignatureScheme>
	 */
	public static ArrayList<TlsSignatureScheme> parsetlsSignatureSchemeWithHashByteList(final byte[] data) {

		ArrayList<TlsSignatureScheme> foundSignatureAlgorithmWithHash
				= new ArrayList<>();
		if (data != null) {
			// First 2 bytes is the length of the buffer
			for (int i = 2; i < data.length; i += 2) {
				TlsSignatureScheme sigAlgo;
				try {
					sigAlgo = TlsSignatureScheme.valueOf(data[i], data[i + 1]);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
				foundSignatureAlgorithmWithHash.add(sigAlgo);
			}
		}
		return foundSignatureAlgorithmWithHash;
	}
}
