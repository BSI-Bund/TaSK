package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.tools.StringTools;


/**
 * Representation of a TLS extension.
 *
 * @see https://tools.ietf.org/html/rfc5246#section-7.4.1.4
 */
public abstract class TlsExtension {
	private final TlsExtensionTypes type;

	/**
	 * @param type the Tls extension type.
	 */
	protected TlsExtension(final TlsExtensionTypes type) {
		this.type = type;
	}


	/**
	 * Extension-specific implementation providing the extension's data.
	 *
	 * @return TLS extension data
	 */
	protected abstract byte[] getData();


	/**
	 * Return this TLS extension encoded as a structured extension.
	 *
	 * @return hexadecimal encoded string
	 */
	public final String toHexString() {
		final byte[] data = getData();
		if (null == data) {
			return "";
		}
		return String.format("%04x%04x%s", type.getValue(), data.length, StringTools.toHexString(data));
	}

	public abstract TlsExtension createExtension(byte[] data);

	/**
	 * Returns the name of the extension type.
	 *
	 * @return the extension type
	 */
	public String getType() {
		return type.getExtensionDescriptionValue();
	}

}
