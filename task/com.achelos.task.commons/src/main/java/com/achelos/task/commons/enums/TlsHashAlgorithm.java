package com.achelos.task.commons.enums;

import java.util.ArrayList;
import java.util.List;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all Hash Algorithm level descriptions.
 */
public enum TlsHashAlgorithm {
	/** ((byte) 0x00, "none"). */
	none((byte) 0x00, "none"),
	/** ((byte) 0x01, "md5"). */
	md5((byte) 0x01, "md5"),
	/** ((byte) 0x02, "sha1"). */
	sha1((byte) 0x02, "sha1"),
	/** ((byte) 0x03, "sha224"). */
	sha224((byte) 0x03, "sha224"),
	/** ((byte) 0x04, "sha256"). */
	sha256((byte) 0x04, "sha256"),
	/** ((byte) 0x05, "sha384"). */
	sha384((byte) 0x05, "sha384"),
	/** ((byte) 0x06, "sha512"). */
	sha512((byte) 0x06, "sha512"),
	/** ((byte) 0x07, "reserved"). */
	reserved((byte) 0x07, "reserved"),
	/** ((byte) 0x08, "intrinsic"). */
	intrinsic((byte) 0x08, "intrinsic");

	private final byte value;
	private final String hashAlgorithmValueDescription;

	/**
	 * @param value The hash algorithm value.
	 * @param hashAlgorithmDescription The hash algorithm value description.
	 */
	TlsHashAlgorithm(final byte value, final String hashAlgorithmDescription) {
		this.value = value;
		hashAlgorithmValueDescription = hashAlgorithmDescription;
	}


	/**
	 * @return value of this hash algorithm.
	 */
	public byte getValue() {
		return value;
	}


	/**
	 * @return the string representation of a given {@link TlsHashAlgorithm} Level element.
	 */
	public String getHashAlgorithmLevelValue() {
		return String.format("%02x", value);
	}


	/**
	 * @return the string representation of a given {@link TlsHashAlgorithm} Level Description element.
	 */
	public String getHashAlgorithmDescription() {
		return hashAlgorithmValueDescription;
	}


	@Override
	public String toString() {
		return String.format("%s(%d)", hashAlgorithmValueDescription, value);
	}


	/**
	 * Method searches a given (hash algorithm value) element in string representation and returns the
	 * corresponding {@link TlsHashAlgorithm} enumeration.
	 *
	 * @param value the hash algorithm value.
	 * @return the {@link TlsHashAlgorithm} enumeration matching the given {@link TlsHashAlgorithm} input value.
	 * @throws InvalidAttributeValueException if the given value is not a valid {@link TlsHashAlgorithm}.
	 */
	public static TlsHashAlgorithm getElement(final byte value) throws InvalidAttributeValueException {
		for (TlsHashAlgorithm hash : TlsHashAlgorithm.values()) {
			if (hash.value == value) {
				return hash;
			}
		}
		throw new InvalidAttributeValueException("The given value " + value + " is not a valid hash algorithm");
	}


	/**
	 * Method searches a given hash algorithm value and returns the corresponding {@link TlsHashAlgorithm} enumeration.
	 *
	 * @param value The hash algorithm value byte.
	 * @return The {@link TlsHashAlgorithm} enumeration matching hash algorithm input value or null if no hash algorithm
	 * was found.
	 */
	public static TlsHashAlgorithm valueOf(final byte value) {
		for (TlsHashAlgorithm hashAlgorithm : TlsHashAlgorithm.values()) {
			if (hashAlgorithm.value == value) {
				return hashAlgorithm;
			}
		}
		return null;
	}


	/**
	 * Method searches a given hash algorithm value and returns the corresponding {@link TlsHashAlgorithm} enumeration.
	 *
	 * @param hexString the hash algorithm value.
	 * @return the {@link TlsHashAlgorithm} enumeration matching hash algorithm input value or null if no hash algorithm
	 * was found.
	 */
	public static TlsHashAlgorithm valueOfHexString(final String hexString) {
		final short value = Short.parseShort(hexString, 16);
		final int a255 = 255;
		if (0 > value || a255 < value) {
			return null;
		}
		return valueOf((byte) value);
	}


	/**
	 * Returns all hash algorithms.
	 *
	 * @return a list with all hash algorithms.
	 */
	public static List<TlsHashAlgorithm> getAll() {
		List<TlsHashAlgorithm> result = new ArrayList<>();
		result.add(sha512);
		result.add(sha384);
		result.add(sha256);
		result.add(sha224);
		result.add(sha1);
		result.add(md5);
		return result;
	}

}
