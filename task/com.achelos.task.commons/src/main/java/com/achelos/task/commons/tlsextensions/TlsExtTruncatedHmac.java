package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Truncated HMAC extension.
 *
 * @see https://tools.ietf.org/html/rfc6066#section-7
 */
public class TlsExtTruncatedHmac extends TlsExtension {

	/**
	 * Default constructor.
	 */
	public TlsExtTruncatedHmac() {
		super(TlsExtensionTypes.truncated_hmac);
	}


	@Override
	protected final byte[] getData() {
		return new byte[] { };
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtTruncatedHmac();
	}
}
