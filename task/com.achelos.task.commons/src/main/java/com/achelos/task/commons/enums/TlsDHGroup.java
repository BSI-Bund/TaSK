package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all available Diffie-Hellmann KeyExchange groups. For more information please visit:
 * https://tools.ietf.org/html/rfc3526 https://tools.ietf.org/html/rfc5114
 */
public enum TlsDHGroup {
	/** RFC 3526 � 1536-bit MODP Group. */
	rfc3526_1536("rfc3526_1536", 1536),
	/** RFC 3526 � 2048-bit MODP Group. */
	rfc3526_2048("rfc3526_2048", 2048),
	/** RFC 3526 � 3072-bit MODP Group. */
	rfc3526_3072("rfc3526_3072", 3072),
	/** RFC 3526 � 4096-bit MODP Group. */
	rfc3526_4096("rfc3526_4096", 4096),
	/** RFC 3526 � 6144-bit MODP Group. */
	rfc3526_6144("rfc3526_6144", 6144),
	/** RFC 3526 � 8192-bit MODP Group. */
	rfc3526_8192("rfc3526_8192", 8192),
	/** RFC 5114 � 1024-bit MODP Group with 160-bit Prime Order Subgroup. */
	rfc5114_1024_160("rfc5114_1024_160", 1024),
	/** RFC 5114 � 2048-bit MODP Group with 224-bit Prime Order Subgroup. */
	rfc5114_2048_224("rfc5114_2048_224", 2048),
	/** RFC 5114 � 2048-bit MODP Group with 256-bit Prime Order Subgroup. */
	rfc5114_2048_256("rfc5114_2048_256", 2048);

	private final String dhGroup;
	private final int keyLength;


	/**
	 * Default constructor.
	 *
	 * @param dhGroup
	 */
	TlsDHGroup(final String dhGroup, final int keyLength) {
		this.dhGroup = dhGroup;
		this.keyLength = keyLength;
	}


	/**
	 * Returns the string representation of a given Diffie-Hellmann KeyExchange group.
	 *
	 * @return dhGroup
	 */
	public String getValue() {
		return dhGroup;
	}

	public int getKeyLength() {
		return keyLength;
	}


	/**
	 * Method searches a given Diffie-Hellmann element in string representation and returns the corresponding
	 * Diffie-Hellmann group enumeration.
	 *
	 * @param dhElement Diffie-Hellmann element
	 * @return Corresponding Diffie-Hellmann group enumeration.
	 * @throws InvalidAttributeValueException
	 */
	public static TlsDHGroup getElement(final String dhElement) throws InvalidAttributeValueException {
		for (TlsDHGroup group : TlsDHGroup.values()) {
			if (group.getValue().equals(dhElement)) {
				return group;
			}
		}
		throw new InvalidAttributeValueException(
				"The given value: " + dhElement + " is not a valid Diffie-Hellmann group!");
	}

	public static TlsDHGroup findMatchingKeyLengthGroup(final int keyLength) {
		for (TlsDHGroup group : TlsDHGroup.values()) {
			if (group.getKeyLength() == keyLength) {
				return group;
			}
		}
		return null;
	}
}