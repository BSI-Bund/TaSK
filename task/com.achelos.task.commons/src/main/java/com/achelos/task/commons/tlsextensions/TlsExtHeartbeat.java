package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Heartbeat extension.
 *
 * @see https://tools.ietf.org/html/rfc6520#section-2
 */
public class TlsExtHeartbeat extends TlsExtension {
	private boolean peerAllowedToSend = false;

	/**
	 * Default constructor with TlsExtensionTypes.heartbeat.
	 */
	public TlsExtHeartbeat() {
		super(TlsExtensionTypes.heartbeat);
	}


	/**
	 * Configure if this peer is willing to respond with heartbeat response messages.
	 *
	 * @param peerAllowedToSend {@code true} corresponds to peer_allowed_to_send(1) and {@code false} corresponds to
	 * peer_not_allowed_to_send(2).
	 */
	public final void setPeerAllowedToSend(final boolean peerAllowedToSend) {
		this.peerAllowedToSend = peerAllowedToSend;
	}


	@Override
	protected final byte[] getData() {
		return new byte[] {(byte) (peerAllowedToSend ? 1/* peer_allowed_to_send */ : 2 /* peer_not_allowed_to_send */)};
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtHeartbeat();
	}
}
