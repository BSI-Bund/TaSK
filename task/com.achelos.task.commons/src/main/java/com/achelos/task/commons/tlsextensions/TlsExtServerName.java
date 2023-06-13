package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.enums.TlsExtensionTypes;


/**
 * Representation of a TLS Server Name Indication extension.
 *
 * @see https://tools.ietf.org/html/rfc6066#section-3 host_name: HostName; } name; } ServerName; enum { host_name(0),
 * (255) } NameType; opaque HostName<1..2^16-1>; struct { ServerName server_name_list<1..2^16-1> } ServerNameList;
 */
public class TlsExtServerName extends TlsExtension {
	private final List<String> serverNameList = new ArrayList<>();

	/**
	 * The TLS server name extension constructor.
	 */
	public TlsExtServerName() {
		super(TlsExtensionTypes.server_name);
	}


	/**
	 * The TLS server name extension constructor.
	 *
	 * @param names The host names.
	 */
	public TlsExtServerName(final String... names) {
		this();
		for (String name : names) {
			addHostName(name);
		}
	}


	/**
	 * Add a server name.
	 *
	 * @param name Server name to add
	 */
	public void addHostName(final String name) {
		serverNameList.add(name);
	}


	@Override
	protected final byte[] getData() {
		if (null == serverNameList || serverNameList.isEmpty()) {
			return new byte[] { };
		}
		// 2 byte entry length
		// 1 byte type (== 0)
		// 2 byte server name length
		// server name
		// Only the first list element is provided, since, currently only "host-type"
		// is defined and there must not be multiple entries of one type.
		byte[] serverNameBytes = serverNameList.get(0).getBytes(StandardCharsets.UTF_8);
		int totalLength = 2 + 1 + 2 + serverNameBytes.length;
		final ByteBuffer buffer = ByteBuffer.allocate(totalLength);
		buffer.putShort((short) (totalLength - 2)); // number of bytes of first list entry
		buffer.put((byte) 0); // NameType host_name(0)
		buffer.putShort((short) serverNameBytes.length); // number of bytes of server name
		buffer.put(serverNameBytes, 0, serverNameBytes.length);
		buffer.flip();
		return buffer.array();
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		return new TlsExtServerName();
	}
}
