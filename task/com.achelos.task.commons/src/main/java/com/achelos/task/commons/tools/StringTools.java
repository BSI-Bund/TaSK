/**
 * Copyright Â© 2009 achelos GmbH All rights reserved.
 */
package com.achelos.task.commons.tools;

/**
 * This class is intended to bundle several standard methods, used to process and convert {@link String}s.
 */
public final class StringTools {

	/**
	 * Decimal radix (10).
	 */
	public static final int RADIX_DEC_10 = 10;
	/**
	 * Hexadecimal radix (16).
	 */
	public static final int RADIX_HEX_16 = 16;
	/**
	 * All possible characters used in a hex string (upper case).
	 */
	private static final char[] HEX_CHARACTER = {'0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private static final int HIGH_NIBBLE = 0xf0;

	private static final int LOW_NIBBLE = 0x0f;

	/**
	 * Helper class is not intended to be instanced.
	 */
	private StringTools() {

	}

	/**
	 * Makes the string even length.
	 *
	 * @param s the string
	 * @return The String s, with an even length (therefore, a "0" will be added at the beginning, in case of need).
	 */

	public static String makeEven(final String s) {
		return (s.length() % 2 == 0 ? "" : "0") + s;
	}

	/**
	 * Converts an integer into a hex string.<br />
	 * Negative values will be converted to a 32 bit value in two's complement
	 *
	 * @param input as integer value
	 * @return The hexadecimal octet string of input value.
	 */
	public static String toHexString(final int input) {
		String hexOctetString = Integer.toHexString(input);
		return makeEven(hexOctetString).toUpperCase();
	}

	/**
	 * Converts the byte array to to a hex string.
	 *
	 * @param b the byte array
	 * @return The hexadecimal representation of the byte array.
	 */
	public static String toHexString(final byte[] b) {
		return toHexString(b, false);
	}

	/**
	 * Converts the byte array to to a hex string.
	 *
	 * @param b the byte array
	 * @param stripLeadingZeroes true, if leading zeroes are removed from the Hex-String
	 * @return The hexadecimal representation of the byte array.
	 */

	public static String toHexString(final byte[] b, final boolean stripLeadingZeroes) {
		if (b == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer(b.length * 2);
		boolean zeroIsLeading = stripLeadingZeroes;
		for (int i = 0; i < b.length; i++) {
			if (b[i] == 0x00 && !zeroIsLeading || b[i] != 0x00) {
				zeroIsLeading = false;
				// look up high nibble char
				sb.append(HEX_CHARACTER[(b[i] & HIGH_NIBBLE) >>> 4]); // fill
																		// left
																		// with
																		// zero
																		// bits

				// look up low nibble char
				sb.append(HEX_CHARACTER[b[i] & LOW_NIBBLE]);
			}
		}
		return sb.toString();
	}


	/**
	 * Converts a hexadecimal string into a byte array.
	 *
	 * @param hexString the hex string
	 * @return the byte array
	 */
	public static byte[] toByteArray(final String hexString) {
		String hexStr = filterHexString(hexString);
		byte[] bArray = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			byte firstNibble = Byte.parseByte(
					hexStr.substring(2 * i, 2 * i + 1), RADIX_HEX_16); // [x,y)
			byte secondNibble = Byte.parseByte(
					hexStr.substring(2 * i + 1, 2 * i + 2), RADIX_HEX_16);
			int finalByte = secondNibble | firstNibble << 4; // bit-operations
																// only with
																// numbers,
																// not
																// bytes.
			bArray[i] = (byte) finalByte;
		}
		return bArray;
	}

	/**
	 * Filters a given string, so that only its hexadecimal components will pass.
	 *
	 * @param s the string
	 * @return the filtered string<br>
	 * E.g.: "0x1234af" &rarr; "01234AF"<br>
	 * "klmn" &rarr; ""<br>
	 * "hello world" &rarr; "ED"<br>
	 */
	public static String filterHexString(final String s) {
		return s.toUpperCase().replaceAll("[^\\p{XDigit}]", "");
	}


	/**
	 * Converts an integer into a hex value and prints that as a string with length <code>length</code> if the converted
	 * value's length is lower than <code>length</code>, the value will be filled up with preceding zeros.
	 *
	 * @param input input value
	 * @param length length of the output
	 * @return e.g.
	 */
	public static String toHexString(final int input, final int length) {
		return String.format("%0" + length + "X", input);
	}

}
