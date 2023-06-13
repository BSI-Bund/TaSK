package com.achelos.task.commons.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.commons.certificatehelper.TlsSignatureAlgorithmWithHashTls13;
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
	RSA_PKCS1_SHA256((byte) 0x04, (byte) 0x01, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha256, null),
	/** SHA384. **/
	RSA_PKCS1_SHA384((byte) 0x05, (byte) 0x01, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha384, null),
	/** SHA512. **/
	RSA_PKCS1_SHA512((byte) 0x06, (byte) 0x01, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha512,null),

	/** ECDSA algorithms. **/

	/** SHA256. **/
	ECDSA_SECP256R1_SHA256((byte) 0x04, (byte) 0x03, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha256, TlsNamedCurves.secp256r1),
	/** SHA384. **/
	ECDSA_SECP384R1_SHA384((byte) 0x05, (byte) 0x03, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha384 , TlsNamedCurves.secp384r1),
	/** SHA512. **/
	ECDSA_SECP521R1_SHA512((byte) 0x06, (byte) 0x03, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha512 , TlsNamedCurves.secp521r1),

	/** ECDSA algorithms with brainpool curve. **/

	/** SHA256. **/
	ECDSA_BRAINPOOLP256R1TLS13_SHA256((byte) 0x08, (byte) 0x1a, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha256, TlsNamedCurves.brainpoolP256r1),
	/** SHA384. **/
	ECDSA_BRAINPOOLP384R1TLS13_SHA384((byte) 0x08, (byte) 0x1b, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha384, TlsNamedCurves.brainpoolP384r1),
	/** SHA512. **/
	ECDSA_BRAINPOOLP512R1TLS13_SHA512((byte) 0x08, (byte) 0x1c, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha512, TlsNamedCurves.brainpoolP512r1),

	/** RSASSA-PSS algorithms with public key OID rsaEncryption. */

	/** SHA256. **/
	RSA_PSS_RSAE_SHA256((byte) 0x08, (byte) 0x04, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha256, null),
	/** SHA384. **/
	RSA_PSS_RSAE_SHA384((byte) 0x08, (byte) 0x05, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha384, null),
	/** SHA512. **/
	RSA_PSS_RSAE_SHA512((byte) 0x08, (byte) 0x06, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha512, null),

	/** EdDSA algorithms. **/

	/** 25519. **/
	ED25519((byte) 0x08, (byte) 0x07, TlsSignatureAlgorithm.ed25519, TlsHashAlgorithm.intrinsic, null),
	/** 448. **/
	ED448((byte) 0x08, (byte) 0x08, TlsSignatureAlgorithm.ed448, TlsHashAlgorithm.intrinsic, null),

	/** RSASSA-PSS algorithms with public key OID RSASSA-PSS. **/

	/** SHA256. **/
	RSA_PSS_PSS_SHA256((byte) 0x08, (byte) 0x09, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha256, null),
	/** SHA384. **/
	RSA_PSS_PSS_SHA384((byte) 0x08, (byte) 0x0a, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha384, null),
	/** SHA512. **/
	RSA_PSS_PSS_SHA512((byte) 0x08, (byte) 0x0b, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha512, null),

	/** Legacy algorithms. **/

	/** RSASSA-PKCS1-v1_5 with SHA1. **/
	RSA_PKCS1_SHA1((byte) 0x02, (byte) 0x01, TlsSignatureAlgorithm.rsa, TlsHashAlgorithm.sha256, null),
	/** ECDSA with SHA1. **/
	ECDSA_SHA1((byte) 0x02, (byte) 0x03, TlsSignatureAlgorithm.ecdsa, TlsHashAlgorithm.sha1, null);

	private final byte upper;
	private final byte lower;
	private final TlsSignatureAlgorithm signatureAlgorithm;
	private final TlsHashAlgorithm hashAlgorithm;
	private final TlsNamedCurves ellipticCurveGroup;

	/**
	 * Signature scheme constructor.
	 *
	 * @param upper Upper byte.
	 * @param lower Lower byte.
	 */
	TlsSignatureScheme(final byte upper, final byte lower, TlsSignatureAlgorithm signatureAlgorithm, TlsHashAlgorithm hashAlgorithm, TlsNamedCurves ellipticCurveGroup) {
		this.upper = upper;
		this.lower = lower;
		this.signatureAlgorithm = signatureAlgorithm;
		this.hashAlgorithm = hashAlgorithm;
		this.ellipticCurveGroup = ellipticCurveGroup;
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

	public byte getUpper() {
		return upper;
	}

	public byte getLower() {
		return lower;
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


	public TlsSignatureAlgorithm getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public TlsHashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	public TlsNamedCurves getEllipticCurveGroup() { return ellipticCurveGroup; }

	public boolean isEllipticCurveScheme() { return ellipticCurveGroup!=null; }


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

	public static boolean isSignatureScheme(final byte upper, final byte lower){
		return TlsSignatureScheme.valueOf(upper, lower)!=null;
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

	public boolean isRsaPssPss(){
		return this.toString().contains("RSA_PSS_PSS");
	}

	/**
	 * Get the {@link TlsSignatureScheme} object by matching hex string value.
	 *
	 * @return TlsSignatureScheme
	 */
	public static List<TlsSignatureAlgorithmWithHashTls13> getTls13SignatureSchemes() {
			return Arrays.asList(
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_SECP256R1_SHA256),
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_SECP384R1_SHA384),
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_SECP521R1_SHA512),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PKCS1_SHA256),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PKCS1_SHA384),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PKCS1_SHA512),
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_BRAINPOOLP256R1TLS13_SHA256),
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_BRAINPOOLP384R1TLS13_SHA384),
					new TlsSignatureAlgorithmWithHashTls13(ECDSA_BRAINPOOLP512R1TLS13_SHA512 ),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_RSAE_SHA256),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_RSAE_SHA384),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_RSAE_SHA512 ),
					/*new TlsSignatureAlgorithmWithHashTls13(ED25519),
					new TlsSignatureAlgorithmWithHashTls13(ED448),*/
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_PSS_SHA256),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_PSS_SHA384),
					new TlsSignatureAlgorithmWithHashTls13(RSA_PSS_PSS_SHA512));
	}
}
