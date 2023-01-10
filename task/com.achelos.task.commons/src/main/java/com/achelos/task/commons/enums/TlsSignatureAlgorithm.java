package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all Signature Algorithm level descriptions.
 */
public enum TlsSignatureAlgorithm {
	/** ((byte) 0x00, "anonymous". */
	anonymous((byte) 0x00, "anonymous"),
	/** ((byte) 0x01, "rsa"). */
	rsa((byte) 0x01, "rsa"),
	/** ((byte) 0x02, "dsa"). */
	dsa((byte) 0x02, "dsa"),
	/** ((byte) 0x03, "ecdsa"). */
	ecdsa((byte) 0x03, "ecdsa"),
	/** ((byte) 0x04, "reserved"). */
	reserved_4((byte) 0x04, "reserved"),
	/** ((byte) 0x05, "reserved"). */
	reserved_5((byte) 0x05, "reserved"),
	/** ((byte) 0x06, "reserved"). */
	reserved_6((byte) 0x06, "reserved"),
	/** ((byte) 0x07, "ed25519"). */
	ed25519((byte) 0x07, "ed25519"),
	/** ((byte) 0x08, "ed448"). */
	ed448((byte) 0x08, "ed448"),
	/** ((byte) 0x09, "reserved"). */
	reserved_9((byte) 0x09, "reserved"),
	/** ((byte) 0x0a, "reserved"). */
	reserved_a((byte) 0x0a, "reserved"),
	/** ((byte) 0x0b, "reserved"). */
	reserved_b((byte) 0x0b, "reserved"),
	/** ((byte) 0x0c, "reserved"). */
	reserved_c((byte) 0x0c, "reserved"),
	/** ((byte) 0x40, "gostr34102012_256"). */
	gostr34102012_256((byte) 0x40, "gostr34102012_256"),
	/** ((byte) 0x41, "gostr34102012_512"). */
	gostr34102012_512((byte) 0x41, "gostr34102012_512");

	private final byte value;
	private final String signatureAlgorithmValueDescription;

	/**
	 * Default Constructor.
	 *
	 * @param value Signature algorithm value.
	 * @param signatureAlgorithmDescription Signature algorithm value description.
	 */
	TlsSignatureAlgorithm(final byte value, final String signatureAlgorithmDescription) {
		this.value = value;
		signatureAlgorithmValueDescription = signatureAlgorithmDescription;
	}


	/**
	 * @return Value of this signature algorithm.
	 */
	public final byte getValue() {
		return value;
	}


	/**
	 * @return the string representation of a given {@link TlsSignatureAlgorithm} element.
	 */
	public final String getSignatureAlgorithmLevelValue() {
		return String.format("%02x", value);
	}


	/**
	 * @return the string representation of a given {@link TlsSignatureAlgorithm} description element.
	 */
	public final String getSignatureAlgorithmValueDescription() {
		return signatureAlgorithmValueDescription;
	}


	@Override
	public final String toString() {
		return String.format("%s(%d)", signatureAlgorithmValueDescription, value);
	}


	/**
	 * Method searches a given element representation and returns the corresponding
	 * {@link TlsSignatureAlgorithm} enumeration.
	 *
	 * @param value The signature algorithm value.
	 * @return signature algorithm that matches the given value.
	 * @throws InvalidAttributeValueException if the given value is not found.
	 */
	public static TlsSignatureAlgorithm getElement(final byte value) throws InvalidAttributeValueException {
		for (TlsSignatureAlgorithm signature : TlsSignatureAlgorithm.values()) {
			if (signature.value == value) {
				return signature;
			}
		}
		throw new InvalidAttributeValueException("The given value " + value + " is not a valid signature algorithm");
	}


	/**
	 * Method searches a given signature algorithm value returns the corresponding {@link TlsSignatureAlgorithm}
	 * enumeration.
	 *
	 * @param value The signature algorithm value.
	 * @return the {@link TlsSignatureAlgorithm} enumeration if found, null otherwise.
	 * @throws InvalidAttributeValueException if the given value is not a valid {@link TlsSignatureAlgorithm}.
	 */
	public static TlsSignatureAlgorithm valueOf(final byte value) {
		for (TlsSignatureAlgorithm signatureAlgorithm : TlsSignatureAlgorithm.values()) {
			if (signatureAlgorithm.value == value) {
				return signatureAlgorithm;
			}
		}
		return null;
	}


	/**
	 * Method searches a given signature algorithm value returns the corresponding TlsSignatureAlgorithm enumeration.
	 *
	 * @param hexString The signature algorithm value as hexadecimal string.
	 * @return the {@link TlsSignatureAlgorithm} enumeration if found, null otherwise.
	 */
	public static TlsSignatureAlgorithm valueOfHexString(final String hexString) {
		final short value = Short.parseShort(hexString, 16);
		final int a255 = 255;
		if (0 > value || a255 < value) {
			return null;
		}
		return valueOf((byte) value);
	}
}
