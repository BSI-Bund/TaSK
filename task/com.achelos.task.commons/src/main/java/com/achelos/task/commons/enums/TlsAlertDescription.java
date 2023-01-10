package com.achelos.task.commons.enums;

import java.util.ArrayList;
import java.util.List;


/**
 * Enumeration for all known AlertDescription values.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.2">TLS 1.2 - Alert Protocol</a>
 */
public enum TlsAlertDescription {
	/** (byte) 0. */
	close_notify((byte) 0),
	/** (byte) 10. */
	unexpected_message((byte) 10),
	/** (byte) 20. */
	bad_record_mac((byte) 20),
	/** (byte) 21. */
	decryption_failed_RESERVED((byte) 21),
	/** (byte) 22. */
	record_overflow((byte) 22),
	/** (byte) 30. */
	decompression_failure((byte) 30),
	/** (byte) 40. */
	handshake_failure((byte) 40),
	/** (byte) 41. */
	no_certificate_RESERVED((byte) 41),
	/** (byte) 42. */
	bad_certificate((byte) 42),
	/** (byte) 43. */
	unsupported_certificate((byte) 43),
	/** (byte) 44. */
	certificate_revoked((byte) 44),
	/** (byte) 45. */
	certificate_expired((byte) 45),
	/** (byte) 46. */
	certificate_unknown((byte) 46),
	/** (byte) 47. */
	illegal_parameter((byte) 47),
	/** (byte) 48. */
	unknown_ca((byte) 48),
	/** (byte) 49. */
	access_denied((byte) 49),
	/** (byte) 50. */
	decode_error((byte) 50),
	/** (byte) 51. */
	decrypt_error((byte) 51),
	/** (byte) 60. */
	export_restriction_RESERVED((byte) 60),
	/** (byte) 70. */
	protocol_version((byte) 70),
	/** (byte) 71. */
	insufficient_security((byte) 71),
	/** (byte) 80. */
	internal_error((byte) 80),
	/** (byte) 90. */
	user_canceled((byte) 90),
	/** (byte) 100. */
	no_renegotiation((byte) 100),
	/** (byte) 110. */
	unsupported_extension((byte) 110);

	private byte description;

	/**
	 * Default constructor.
	 *
	 * @param description the description.
	 */
	TlsAlertDescription(final byte description) {
		this.description = description;
	}


	/**
	 * Return the description's value.
	 *
	 * @return Value
	 */
	public byte toNumber() {
		return description;
	}


	/**
	 * Return the description's value as hexadecimal string.
	 *
	 * @return String with one hexadecimal encoded byte (e.g., "1e")
	 */
	public String toHexString() {
		return String.format("%02x", description);
	}


	@Override
	public String toString() {
		return String.format("%s(%d)", name(), description);
	}


	/**
	 * Method returns list of {@link TlsAlertDescription} except of the close notify description.
	 *
	 * @return the list of {@link TlsAlertDescription} containing error alerts.
	 */
	public static List<TlsAlertDescription> getAllErrorAlertDescriptions() {
		List<TlsAlertDescription> errorAlerts = new ArrayList<>();

		for (TlsAlertDescription alert : TlsAlertDescription.values()) {
			if (!alert.equals(TlsAlertDescription.close_notify)) {
				errorAlerts.add(alert);
			}
		}
		return errorAlerts;
	}


	/**
	 * Method searches for a given {@link TlsAlertDescription} on a given byte value or NULL if nothing was found.
	 *
	 * @param value the given byte value
	 * @return {@link TlsAlertDescription} for a given byte value
	 */
	public static TlsAlertDescription converteValue(final byte value) {
		for (TlsAlertDescription enumValue : TlsAlertDescription.values()) {
			if (enumValue.toNumber() == value) {
				return enumValue;
			}
		}
		return null;
	}
}
