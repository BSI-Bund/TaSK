package com.achelos.task.commons.helper;

/**
 * Constants of the TLS record layer. All lengths are given as a number of bytes.
 *
 * @see https://tools.ietf.org/html/rfc5246#section-6.2
 */
public final class TlsRecordLayerConstants {
	private static final int BIT_SHIFT_14 = 14;

	/**
	 * Default constructor. not called.
	 */
	private TlsRecordLayerConstants() {
		// Not called.
	}

	/**
	 * Length of the TLS record layer header (fields type (1 byte), version (2 bytes), and length (2 bytes)).
	 */
	public static final int HEADER_LENGTH = 5;

	/**
	 * Length of TLSPlaintext.fragment must not exceed 2^14.
	 */
	public static final int TLSPLAINTEXT_MAX_LENGTH = 1 << BIT_SHIFT_14;

	/**
	 * Length of TLSCompressed.fragment must not exceed 2^14 + 1024.
	 */
	public static final int TLSCOMPRESSED_MAX_LENGTH = (1 << BIT_SHIFT_14) + 1024;

	/**
	 * Length of TLSCiphertext.fragment must not exceed 2^14 + 2048.
	 */
	public static final int TLSCIPHERTEXT_MAX_LENGTH = (1 << BIT_SHIFT_14) + 2048;
}
