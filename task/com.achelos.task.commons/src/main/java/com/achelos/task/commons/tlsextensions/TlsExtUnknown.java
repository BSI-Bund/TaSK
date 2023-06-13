package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of an unknown TLS extension that is not defined.
 */
public class TlsExtUnknown extends TlsExtension {
	/**
	 * Default constructor.
	 */
	public TlsExtUnknown() {
		super(TlsExtensionTypes.unassigned);
	}


	@Override
	protected final byte[] getData() {
		return new byte[] { };
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtUnknown();
	}
}
