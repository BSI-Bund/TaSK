package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Extended Master Secret extension.
 *
 * @see https://tools.ietf.org/html/rfc7627
 */
public class TlsExtExtendedMasterSecret extends TlsExtension {
	/**
	 * Default constructor.
	 */
	public TlsExtExtendedMasterSecret() {
		super(TlsExtensionTypes.extended_master_secret);
	}


	@Override
	protected final byte[] getData() {
		return new byte[] { };
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtExtendedMasterSecret();
	}
}
