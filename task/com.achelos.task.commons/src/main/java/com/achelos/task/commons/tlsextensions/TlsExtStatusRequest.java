package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Certificate Status Request extension.
 *
 * @see https://tools.ietf.org/html/rfc6066#section-8
 */
public class TlsExtStatusRequest extends TlsExtension {
	/**
	 * Default constructor.
	 */
	public TlsExtStatusRequest() {
		super(TlsExtensionTypes.status_request);
	}


	@Override
	protected final byte[] getData() {
		final ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 2);

		// CertificateStatusType = ocsp(1)
		buffer.put((byte) 1);

		// OCSPStatusRequest
		// Empty responder_id_list
		buffer.putShort((short) 0);
		// Empty request_extensions
		buffer.putShort((short) 0);

		buffer.flip();
		return buffer.array();
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtStatusRequest();
	}
}
