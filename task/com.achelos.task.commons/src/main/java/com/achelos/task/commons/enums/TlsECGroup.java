package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all available elliptic curve Key Exchange groups. For more information please visit:
 * https://tools.ietf.org/html/rfc4492
 */
public enum TlsECGroup {
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp192k1("secp192k1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp192r1("secp192r1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp224k1("secp224k1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp224r1("secp224r1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp256k1("secp256k1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp256r1("secp256r1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp384r1("secp384r1"),
	/** RFC-ietf-tls-rfc4492bis-17. */
	secp521r1("secp521r1"),
	/** RFC 7027. */
	brainpoolP256r1("brainpoolP256r1"),
	/** RFC 7027. */
	brainpoolP384r1("brainpoolP384r1"),
	/** RFC 7027. */
	brainpoolP512r1("brainpoolP512r1");

	private final String ecGroup;

	/**
	 * Default constructor.
	 *
	 * @param ecGroup elliptic curve key exchange group.
	 */
	TlsECGroup(final String ecGroup) {
		this.ecGroup = ecGroup;
	}


	/**
	 * Returns the string representation of a given elliptic curve key exchange group.
	 *
	 * @return ecGroup.
	 */
	public String getValue() {
		return ecGroup;
	}


	/**
	 * Method searches a given elliptic curve element in string representation and returns the corresponding elliptic
	 * curve group enumeration.
	 *
	 * @param ellipticCurve the elliptic curve in string.
	 * @return corresponding elliptic curve group enumeration
	 * @throws InvalidAttributeValueException
	 */
	public static TlsECGroup getElement(final String ellipticCurve) throws InvalidAttributeValueException {
		for (TlsECGroup group : TlsECGroup.values()) {
			if (group.getValue().equals(ellipticCurve)) {
				return group;
			}
		}
		throw new InvalidAttributeValueException(
				"The given value: " + ellipticCurve + " is not a valid elliptic curve group!");
	}
}