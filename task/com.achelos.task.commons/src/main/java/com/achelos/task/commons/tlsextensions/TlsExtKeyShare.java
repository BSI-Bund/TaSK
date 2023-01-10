package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.helper.TlsKeyShareEntry;


/**
 * Representation of a TLS Key Share extension.
 *
 * @see https://tools.ietf.org/html/rfc8446#section-4.2.8
 */
public class TlsExtKeyShare extends TlsExtension {
	private final List<TlsKeyShareEntry> keyShares = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public TlsExtKeyShare() {
		super(TlsExtensionTypes.key_share);
	}


	/**
	 * Add a key share entry.
	 *
	 * @param keyShareEntry key share entry to add
	 */
	public final void addkeyShareEntry(final TlsKeyShareEntry keyShareEntry) {
		keyShares.add(keyShareEntry);
	}


	/**
	 * Remove a key share entry.
	 *
	 * @param keyShareEntry Key share entry to remove
	 */
	public final void removeKeyShareEntry(final TlsKeyShareEntry keyShareEntry) {
		keyShares.remove(keyShareEntry);
	}


	@Override
	protected final byte[] getData() {
		final int length = 2 * 2 * keyShares.size();
		final ByteBuffer buffer = ByteBuffer.allocate(2 + length);
		buffer.putShort((short) length);
		for (TlsKeyShareEntry keyShare : keyShares) {
			buffer.putShort((short) keyShare.getGroup().getValue());
			buffer.putShort(keyShare.getKeySize());
			if (null != keyShare.getKey()) {
				buffer.put(keyShare.getKey());
			}
		}
		buffer.flip();
		return buffer.array();
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return null;
	}

}
