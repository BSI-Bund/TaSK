package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS SessionTicket extension.
 *
 * @see https://tools.ietf.org/html/rfc4507#section-3.2
 */
public class TlsExtSessionTicket extends TlsExtension {
	private byte[] ticket = null;

	/**
	 * Default constructor.
	 */
	public TlsExtSessionTicket() {
		super(TlsExtensionTypes.SessionTicket_TLS);
	}


	/**
	 * Set a session ticket.
	 *
	 * @param sessionTicket Session ticket to send
	 */
	public final void setSessionTicket(final byte[] sessionTicket) {
		ticket = sessionTicket.clone();
	}


	@Override
	protected final byte[] getData() {
		if (null == ticket) {
			return new byte[] { };
		}
		final int size = ticket.length;
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(ticket);
		buffer.flip();
		return buffer.array();
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtSessionTicket();
	}
}
