package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Encrypt-then-MAC extension.
 *
 * @see https://tools.ietf.org/html/rfc7366
 */
public class TlsExtEncryptThenMac extends TlsExtension {
	/**
	 * Default constructor.
	 */
	public TlsExtEncryptThenMac() {
		super(TlsExtensionTypes.encrypt_then_mac);
	}


	@Override
	protected final byte[] getData() {
		return new byte[] { };
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtEncryptThenMac();
	}
}
