package com.achelos.task.commons.helper;

import com.achelos.task.commons.enums.TlsNamedCurves;


/**
 * Representation of a TLS Key Share entry.
 *
 * @see https://tools.ietf.org/html/rfc8446#section-4.2.8
 */
public class TlsKeyShareEntry {
	private TlsNamedCurves group;
	private byte[] keyExchange = null;

	/**
	 * Default constructor.
	 *
	 * @param group the group.
	 */
	public TlsKeyShareEntry(final TlsNamedCurves group) {
		this.group = group;
		// buffer = createKey(group);
	}


	/**
	 * Constructor with group and key exchange.
	 *
	 * @param group the group.
	 * @param key the key.
	 */
	public TlsKeyShareEntry(final TlsNamedCurves group, final byte[] key) {
		this.group = group;
		keyExchange = key.clone();
	}


	/**
	 * Get the named curve of this instance.
	 *
	 * @return the group.
	 */
	public final TlsNamedCurves getGroup() {
		return group;
	}


	/**
	 * Set the named curve.
	 *
	 * @param group the group.
	 */
	public final void setGroup(final TlsNamedCurves group) {
		this.group = group;
	}


	/**
	 * Get the key change.
	 *
	 * @return the key.
	 */
	public final byte[] getKey() {
		return keyExchange.clone();
	}


	/**
	 * Gets the key length.
	 *
	 * @return the key length or 0 if key is null.
	 */
	public final short getKeySize() {
		if (null == keyExchange) {
			return 0;
		}
		return (short) keyExchange.length;
	}


	/**
	 * Sets the key.
	 *
	 * @param key the key to set.
	 */
	public final void setKey(final byte[] key) {
		keyExchange = key.clone();
	}

}
