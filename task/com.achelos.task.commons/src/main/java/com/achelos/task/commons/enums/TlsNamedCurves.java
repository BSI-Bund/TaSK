package com.achelos.task.commons.enums;

import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.tools.StringTools;


/**
 * Enumeration class with named curves and their respective byte values.
 */
public enum TlsNamedCurves {
	/** #rfc8422. (byte) 0x00, (byte) 0x01) */
	sect163k1((byte) 0x00, (byte) 0x01, "sect163k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x02) */
	sect163r1((byte) 0x00, (byte) 0x02, "sect163r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x03) */
	sect163r2((byte) 0x00, (byte) 0x03, "sect163r2"),
	/** #rfc8422. (byte) 0x00, (byte) 0x04) */
	sect193r1((byte) 0x00, (byte) 0x04, "sect193r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x05) */
	sect193r2((byte) 0x00, (byte) 0x05, "sect193r2"),
	/** #rfc8422. (byte) 0x00, (byte) 0x06) */
	sect233k1((byte) 0x00, (byte) 0x06, "sect233k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x07) */
	sect233r1((byte) 0x00, (byte) 0x07, "sect233r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x08) */
	sect239k1((byte) 0x00, (byte) 0x08, "sect239k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x09) */
	sect283k1((byte) 0x00, (byte) 0x09, "sect283k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0A) */
	sect283r1((byte) 0x00, (byte) 0x0A, "sect283r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0B) */
	sect409k1((byte) 0x00, (byte) 0x0B, "sect409k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0C) */
	sect409r1((byte) 0x00, (byte) 0x0C, "sect409r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0D) */
	sect571k1((byte) 0x00, (byte) 0x0D, "sect571k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0E) */
	sect571r1((byte) 0x00, (byte) 0x0E, "sect571r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x0F) */
	secp160k1((byte) 0x00, (byte) 0x0F, "secp160k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x10) */
	secp160r1((byte) 0x00, (byte) 0x10, "secp160r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x11) */
	secp160r2((byte) 0x00, (byte) 0x11, "secp160r2"),
	/** #rfc8422. (byte) 0x00, (byte) 0x12) */
	secp192k1((byte) 0x00, (byte) 0x12, "secp192k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x13) */
	secp192r1((byte) 0x00, (byte) 0x13, "secp192r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x14) */
	secp224k1((byte) 0x00, (byte) 0x14, "secp224k1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x15) */
	secp224r1((byte) 0x00, (byte) 0x15, "secp224r1"),
	/** #rfc8422. (byte) 0x00, (byte) 0x16) */
	secp256k1((byte) 0x00, (byte) 0x16, "secp256k1"),
	/** #rfc8422. ((byte) 0x00, (byte) 0x17) */
	secp256r1((byte) 0x00, (byte) 0x17, "secp256r1"),
	/** #rfc8422. ((byte) 0x00, (byte) 0x18) */
	secp384r1((byte) 0x00, (byte) 0x18, "secp384r1"),
	/** #rfc8422. ((byte) 0x00, (byte) 0x19) */
	secp521r1((byte) 0x00, (byte) 0x19, "secp521r1"),
	/** #rfc5639. ((byte) 0x00, (byte) 0x1A) */
	brainpoolP256r1((byte) 0x00, (byte) 0x1A, "brainpoolP256r1"),
	/** #rfc5639. ((byte) 0x00, (byte) 0x1B) */
	brainpoolP384r1((byte) 0x00, (byte) 0x1B, "brainpoolP384r1"),
	/** #rfc5639. ((byte) 0x00, (byte) 0x1C) */
	brainpoolP512r1((byte) 0x00, (byte) 0x1C, "brainpoolP512r1"),
	/** #draft-ietf-tls-rfc4492bis-10. ((byte) 0x00, (byte) 0x1D) */
	x25519((byte) 0x00, (byte) 0x1D, "x25519"),
	/** #RFC7748. ((byte) 0x00, (byte) 0x1E) */
	x448((byte) 0x00, (byte) 0x1E, "x448"),
	/** ((byte) 0x00, (byte) 0x1F). */
	brainpoolP256r1tls13((byte) 0x00, (byte) 0x1F, "brainpoolP256r1tls13"),
	/** ((byte) 0x00, (byte) 0x20). */
	brainpoolP384r1tls13((byte) 0x00, (byte) 0x20, "brainpoolP384r1tls13"),
	/** ((byte) 0x00, (byte) 0x21). */
	brainpoolP512r1tls13((byte) 0x00, (byte) 0x21, "brainpoolP512r1tls13"),
	/** ((byte) 0x01, (byte) 0x00). */
	ffdhe2048((byte) 0x01, (byte) 0x00, "ffdhe2048"),
	/** https://tools.ietf.org/html/rfc7919#appendix-A.1. */
	ffdhe3072((byte) 0x01, (byte) 0x01, "ffdhe3072"),
	/** https://tools.ietf.org/html/rfc7919#appendix-A.1. ((byte) 0x01, (byte) 0x02) */
	ffdhe4096((byte) 0x01, (byte) 0x02, "ffdhe4096"),
	/** https://tools.ietf.org/html/rfc7919#appendix-A.1. */
	ffdhe6144((byte) 0x01, (byte) 0x03, "ffdhe6144"),
	/** https://tools.ietf.org/html/rfc7919#appendix-A.1. */
	ffdhe8192((byte) 0x01, (byte) 0x04, "ffdhe8192"),
	/** #rfc4492. ((byte) 0xFF, (byte) 0x01) */
	arbitrary_explicit_prime_curves((byte) 0xFF, (byte) 0x01, "arbitrary_explicit_prime_curves"),
	/** #rfc4492. ((byte) 0xFF, (byte) 0x02) */
	arbitrary_explicit_char2_curves((byte) 0xFF, (byte) 0x02, "arbitrary_explicit_char2_curves");

	private final byte upper;
	private final byte lower;
	private final String name;

	/**
	 * Default constructor.
	 *
	 * @param upper upper bound
	 * @param lower lower bound
	 * @param name
	 */
	TlsNamedCurves(final byte upper, final byte lower, final String name) {
		this.upper = upper;
		this.lower = lower;
		this.name = name;
	}


	/**
	 * Get the named group's value.
	 *
	 * @return Value as integer
	 */
	public final int getValue() {
		final int a0xff = 0xff;
		return (upper & a0xff) << 8 | lower & a0xff;
	}


	/**
	 * @return string formatted command value.
	 */
	public String getCommandValue() {
		return String.format("%02x %02x", upper, lower);
	}


	@Override
	public String toString() {
		return String.format("%s(0x%02x%02x)", name(), upper, lower);
	}

	public boolean isFFDHEGroup() {
		if (name().contains("ffdhe")) {
			return true;
		}
		return false;
	}


	/**
	 * Gets the named curved matching upper and lower bounds.
	 *
	 * @param upper the upper bound.
	 * @param lower the lower bound.
	 * @return matching named curve or null if nothing was found.
	 */
	public static TlsNamedCurves valueOf(final byte upper, final byte lower) {
		for (TlsNamedCurves namedCurve : TlsNamedCurves.values()) {
			if (namedCurve.upper == upper && namedCurve.lower == lower) {
				return namedCurve;
			}
		}
		return null;
	}


	/**
	 * Returns the equivalent named curve matching the hex string input. e.g. Converts a hexadecimal string into a byte
	 * array then finds and returns the matched {@link TlsNamedCurves}
	 *
	 * @param hexString the hex string.
	 * @return Matched named curve or null.
	 */
	public static TlsNamedCurves valueOfHexString(final String hexString) {
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

	public String getName() {
		return name;
	}


	/**
	 * Method takes a byte representation of one or more Supported Groups and finds and returns all consisting supported
	 * groups within object representation.
	 *
	 * @param data The Supported Groups list in byte representation
	 * @return the List<TlsNamedCurves>
	 */
	public static List<TlsNamedCurves> parseSupportedGroupsFromByteToList(final byte[] data) {
		List<TlsNamedCurves> foundNamedCurves = new ArrayList<>();
		if (data != null) {
			// First 2 bytes is the length of the buffer
			for (int i = 0; i < data.length; i += 2) {
				foundNamedCurves.add(TlsNamedCurves.valueOf(data[i], data[i + 1]));
			}
		}
		return foundNamedCurves;
	}
}
