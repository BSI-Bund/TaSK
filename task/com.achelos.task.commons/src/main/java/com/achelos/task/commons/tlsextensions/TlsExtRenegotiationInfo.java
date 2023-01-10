package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Renegotiation Indication extension.
 *
 * @see https://tools.ietf.org/html/rfc5746#section-3.2
 */
public class TlsExtRenegotiationInfo extends TlsExtension {
	private byte[] renegotiatedConnection = null;

	/**
	 * Default constructor with {@link TlsExtension} type renegotiation_info.
	 */
	public TlsExtRenegotiationInfo() {
		super(TlsExtensionTypes.renegotiation_info);
	}


	/**
	 * Set the renegotiated_connection data.
	 *
	 * @param renegotiatedConnectiondData renegotiated_connection data to send (client_verify_data for ClientHello, or
	 * concatenation of client_verify_data and server_verify_data for ServerHello)
	 */
	public final void setSessionTicket(final byte[] renegotiatedConnectiondData) {
		renegotiatedConnection = renegotiatedConnectiondData.clone();
	}


	@Override
	protected final byte[] getData() {
		final int size = null == renegotiatedConnection ? 0 : renegotiatedConnection.length;
		final ByteBuffer buffer = ByteBuffer.allocate(1 + size);
		buffer.put((byte) size);
		if (null != renegotiatedConnection) {
			buffer.put(renegotiatedConnection);
		}
		buffer.flip();
		return buffer.array();
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtRenegotiationInfo();
	}
}
