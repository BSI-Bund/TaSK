package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Multiple Certificate Status Request extension.
 *
 * @see https://tools.ietf.org/html/rfc6961
 */
public class TlsExtStatusRequestV2 extends TlsExtension {
	/**
	 * Default constructor.
	 */
	public TlsExtStatusRequestV2() {
		super(TlsExtensionTypes.status_request_v2);
	}


	@Override
	protected final byte[] getData() {
		final int requestSize = 4;
		final int itemSize = 1 + 2 + requestSize;
		final int listSize = 2 + itemSize;
		final ByteBuffer buffer = ByteBuffer.allocate(listSize);

		// CertificateStatusRequestListV2
		// Size of certificate_status_req_list
		buffer.putShort((short) itemSize);

		// CertificateStatusRequestItemV2
		// CertificateStatusType status_type = ocsp(1)
		buffer.put((byte) 1);

		// uint16 request_length
		buffer.putShort((short) requestSize);

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
