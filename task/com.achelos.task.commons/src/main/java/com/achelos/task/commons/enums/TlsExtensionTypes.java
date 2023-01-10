package com.achelos.task.commons.enums;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.achelos.task.commons.tlsextensions.TlsExtEcPointFormats;
import com.achelos.task.commons.tlsextensions.TlsExtEncryptThenMac;
import com.achelos.task.commons.tlsextensions.TlsExtExtendedMasterSecret;
import com.achelos.task.commons.tlsextensions.TlsExtHeartbeat;
import com.achelos.task.commons.tlsextensions.TlsExtRenegotiationInfo;
import com.achelos.task.commons.tlsextensions.TlsExtServerName;
import com.achelos.task.commons.tlsextensions.TlsExtSignatureAlgorithms;
import com.achelos.task.commons.tlsextensions.TlsExtStatusRequest;
import com.achelos.task.commons.tlsextensions.TlsExtStatusRequestV2;
import com.achelos.task.commons.tlsextensions.TlsExtSupportedGroups;
import com.achelos.task.commons.tlsextensions.TlsExtTruncatedHmac;
import com.achelos.task.commons.tlsextensions.TlsExtUnknown;
import com.achelos.task.commons.tlsextensions.TlsExtension;


/**
 * Public enumeration holds all known TLS Extension types (Hex string presentation as well as the description). CH -
 * Client Hello, SH - Server Hello, EE - Encrypted Extensions, CT - Certificate, CR - Certificate Request, NST - New
 * Session Ticket, HRR - Hello Retry Request
 */
public enum TlsExtensionTypes {
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x00, "server_name"). */
	server_name((byte) 0x00, (byte) 0x00, "server_name", new TlsExtServerName(), "CH, EE"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x01, "max_fragment_length"). */
	max_fragment_length((byte) 0x00, (byte) 0x01, "max_fragment_length", null, "CH, EE"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x02, "client_certificate_url"). */
	client_certificate_url((byte) 0x00, (byte) 0x02, "client_certificate_url", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x03, "trusted_ca_keys"). */
	trusted_ca_keys((byte) 0x00, (byte) 0x03, "trusted_ca_keys", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x04, "truncated_hmac"). */
	truncated_hmac((byte) 0x00, (byte) 0x04, "truncated_hmac", new TlsExtTruncatedHmac(), "CH, SH"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x05, "status_request"). */
	status_request((byte) 0x00, (byte) 0x05, "status_request", new TlsExtStatusRequest(), "CH, CR, CT"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x06, "user_mapping"). */
	user_mapping((byte) 0x00, (byte) 0x06, "user_mapping", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x07, "client_authz"). */
	client_authz((byte) 0x00, (byte) 0x07, "client_authz", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x08, "server_authz"). */
	server_authz((byte) 0x00, (byte) 0x08, "server_authz", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x09, "cert_type"). */
	cert_type((byte) 0x00, (byte) 0x09, "cert_type", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0A, "supported_groups"). */
	supported_groups((byte) 0x00, (byte) 0x0A, "supported_groups", new TlsExtSupportedGroups(), "CH, SH, EE"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0B, "ec_point_formats"). */
	ec_point_formats((byte) 0x00, (byte) 0x0B, "ec_point_formats", new TlsExtEcPointFormats(), "CH, SH"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0C, "srp"). */
	srp((byte) 0x00, (byte) 0x0C, "srp", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0D, "signature_algorithms"). */
	signature_algorithms((byte) 0x00, (byte) 0x0D, "signature_algorithms", new TlsExtSignatureAlgorithms(), "CH, CR"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0E, "use_srtp"). */
	use_srtp((byte) 0x00, (byte) 0x0E, "use_srtp", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x0F, "heartbeat"). */
	heartbeat((byte) 0x00, (byte) 0x0F, "heartbeat", new TlsExtHeartbeat(), "CH, SH, EE"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x10, "application_layer_protocol_negotiation"). */
	application_layer_protocol_negotiation((byte) 0x00, (byte) 0x10, "application_layer_protocol_negotiation", null,
			""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x11, "status_request_v2"). */
	status_request_v2((byte) 0x00, (byte) 0x11, "status_request_v2", new TlsExtStatusRequestV2(), "CH, SH"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x12, "signed_certificate_timestamp"). */
	signed_certificate_timestamp((byte) 0x00, (byte) 0x12, "signed_certificate_timestamp", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x13, "client_certificate_type"). */
	client_certificate_type((byte) 0x00, (byte) 0x13, "client_certificate_type", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x14, "server_certificate_type"). */
	server_certificate_type((byte) 0x00, (byte) 0x14, "server_certificate_type", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x15, "padding"). */
	padding((byte) 0x00, (byte) 0x15, "padding", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x16, "encrypt_then_mac"). */
	encrypt_then_mac((byte) 0x00, (byte) 0x16, "encrypt_then_mac", new TlsExtEncryptThenMac(), "CH, SH"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x17, "extended_master_secret"). */
	extended_master_secret((byte) 0x00, (byte) 0x17, "extended_master_secret", new TlsExtExtendedMasterSecret(),
			"CH, SH"),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x18, "token_binding"). */
	token_binding((byte) 0x00, (byte) 0x18, "token_binding", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x19, "cached_info"). */
	cached_info((byte) 0x00, (byte) 0x19, "cached_info", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x23, "SessionTicket TLS"). */
	SessionTicket_TLS((byte) 0x00, (byte) 0x23, "SessionTicket TLS", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x23, "session_ticket (renamed from SessionTicket TLS)"). */
	SessionTicket((byte) 0x00, (byte) 0x23, "session_ticket", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x29, "early_data"). */
	pre_shared_key((byte) 0x00, (byte) 0x29, "early_data", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x2a, "early_data"). */
	early_data((byte) 0x00, (byte) 0x2a, "early_data", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x2b, "supported_versions"). */
	supported_versions((byte) 0x00, (byte) 0x2b, "supported_versions", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x2c, "cookie"). */
	cookie((byte) 0x00, (byte) 0x2c, "cookie", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x2d, "psk_key_exchange_modes"). */
	psk_key_exchange_modes((byte) 0x00, (byte) 0x2d, "psk_key_exchange_modes", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x2f, "certificate_authorities"). */
	certificate_authorities((byte) 0x00, (byte) 0x2f, "certificate_authorities", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x30, "oid_filters"). */
	oid_filters((byte) 0x00, (byte) 0x30, "oid_filters", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x31, "post_handshake_auth"). */
	post_handshake_auth((byte) 0x00, (byte) 0x31, "post_handshake_auth", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x32, "signature_algorithms_cert"). */
	signature_algorithms_cert((byte) 0x00, (byte) 0x32, "signature_algorithms_cert", null, ""),
	/** (Hex string , Description): ((byte) 0x00, (byte) 0x33, "key_share"). */
	key_share((byte) 0x00, (byte) 0x33, "key_share", null, ""),
	/** (Hex string , Description): ((byte) 0xFF, (byte) 0x01, "renegotiation_info"). */
	renegotiation_info((byte) 0xFF, (byte) 0x01, "renegotiation_info", new TlsExtRenegotiationInfo(), "CH, SH"),
	/**
	 * All remaining values not listed from 00 00 till FF FF are not assigned Work with placeholder to generate the
	 * unassigned enum. (Hex string , Description): ((byte) 0xFF, (byte) 0x01, "renegotiation_info").
	 */
	unassigned((byte) 0xFF, (byte) 0xFF, "unassigned", new TlsExtUnknown(), "CH");

	private static final int A_0XFF = 0xff;
	private final byte upper;
	private final byte lower;
	private final String description;
	private final TlsExtension tlsExtension;
	private final String messageAcronym;

	/**
	 * Default constructor.
	 *
	 * @param upper Upper byte value
	 * @param lower lower byte value
	 * @param description the description of the extension type.
	 * @param tlsExtension the tls extension class object.
	 * @param messageAcronym the message acronym e.g. SH for (ServerHello), CH for ClientHello etc.
	 */
	TlsExtensionTypes(final byte upper, final byte lower, final String description, final TlsExtension tlsExtension,
			final String messageAcronym) {
		this.upper = upper;
		this.lower = lower;
		this.description = description;
		this.tlsExtension = tlsExtension;
		this.messageAcronym = messageAcronym;
	}


	/**
	 * Get the extension type's value.
	 *
	 * @return Value as integer
	 */
	public final int getValue() {
		return (upper & A_0XFF) << 8 | lower & A_0XFF;
	}


	/**
	 * Return the extension type's value as hexadecimal string.
	 *
	 * @return String with two hexadecimal encoded bytes separated by space (e.g., "00 0D")
	 */
	public final String getValueHexString() {
		return String.format("%02x %02x", upper, lower);
	}


	/**
	 * Returns the description of a given TLS extension element.
	 *
	 * @return the description
	 */
	public final String getExtensionDescriptionValue() {
		return description;
	}


	@Override
	public final String toString() {
		return String.format("%s(%d)", description, getValue());
	}


	/**
	 * Try to find a extension type enumerator based on the extension type's value.
	 *
	 * @param upper Upper byte of the extension type's value
	 * @param lower Lower byte of the extension type's value
	 * @return Valid enumerator, if found. {@code null}, otherwise.
	 */
	public static TlsExtensionTypes valueOf(final byte upper, final byte lower) {
		for (TlsExtensionTypes extensionType : TlsExtensionTypes.values()) {
			if (extensionType.upper == upper && extensionType.lower == lower) {
				return extensionType;
			}
		}
		return null;
	}

	/**
	 * Try to find a extension type enumerator based on the extension name.
	 *
	 * @param extensionName IANA Name of the Extension
	 * @return Valid enumerator, if found. {@code null}, otherwise.
	 */
	public static TlsExtensionTypes getValueByName(final String extensionName) {
		for (var extType : TlsExtensionTypes.values()) {
			if (extType.description.toLowerCase().equals(extensionName.toLowerCase())) {
				return extType;
			}
		}
		return null;
	}

	public TlsExtension getTlsExtension() {
		return tlsExtension;

	}


	public static List<TlsExtensionTypes> getClientHelloExtensions() {

		return EnumSet.allOf(TlsExtensionTypes.class).stream().filter(e -> e.messageAcronym.contains("CH"))
				.collect(Collectors.toList());
	}


	public static List<TlsExtensionTypes> getServerHelloExtensions() {
		return EnumSet.allOf(TlsExtensionTypes.class).stream().filter(e -> e.messageAcronym.contains("SH"))
				.collect(Collectors.toList());
	}

}
