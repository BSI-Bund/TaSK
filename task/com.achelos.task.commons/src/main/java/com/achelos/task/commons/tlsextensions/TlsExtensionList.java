package com.achelos.task.commons.tlsextensions;

import java.util.ArrayList;
import java.util.List;


/**
 * List of TLS extensions.
 */
public class TlsExtensionList {
	private final List<TlsExtension> list = new ArrayList<>();

	/**
	 * Add a TLS extension to the end of the list.
	 *
	 * @param extension TLS extension to add
	 */
	public final void add(final TlsExtension extension) {
		list.add(extension);
	}


	/**
	 * Return this TLS extension list as hexadecimal string.
	 *
	 * @return Hexadecimal encoded string
	 */
	public final String toHexString() {
		final StringBuilder result = new StringBuilder();
		for (TlsExtension extension : list) {
			result.append(extension.toHexString());
			// always add seperator between extensions except after the last one
			if (list.indexOf(extension) < list.size() - 1) {
				result.append(":");
			}
		}
		return result.toString();
	}


	/**
	 * Create a default list of TLS client extensions.
	 *
	 * @return TLS extension list for a ClientHello message
	 */
	public static TlsExtensionList createDefaultClientExtensions() {
		final TlsExtensionList extensionList = new TlsExtensionList();
		extensionList.add(TlsExtSignatureAlgorithms.createDefault());
		extensionList.add(TlsExtSupportedGroups.createDefault());
		extensionList.add(TlsExtEcPointFormats.createDefault());
		extensionList.add(new TlsExtEncryptThenMac());
		extensionList.add(new TlsExtExtendedMasterSecret());
		extensionList.add(new TlsExtSessionTicket());
		return extensionList;
	}

	/**
	 * Create a default list of TLS client extensions for TLS 1.3
	 *
	 * @return TLS extension list for a ClientHello message
	 */
	public static TlsExtensionList createDefaultClientExtensionsTls13(boolean minimumExtensions) {
		final TlsExtensionList extensionList = new TlsExtensionList();
		extensionList.add(TlsExtSupportedGroups.createDefault());
		extensionList.add(TlsExtSignatureAlgorithms.createDefault());
		extensionList.add(TlsExtSupportedVersions.createDefault());

		if(minimumExtensions){
			extensionList.add(TlsExtPskExchangeModesTls13.createDefault());
			extensionList.add(new TlsExtEncryptThenMac());
			extensionList.add(new TlsExtExtendedMasterSecret());
			extensionList.add(TlsExtEcPointFormats.createDefault());
		}
		return extensionList;
	}

	/**
	 * Creates an empty list of TLS client extensions for a ClientHello message.
	 *
	 * @return TLS extension list
	 */
	public static TlsExtensionList emptyList() {
		final TlsExtensionList extensionList = new TlsExtensionList();
		return extensionList;
	}

	/**
	 * Returns {@code true} if this list contains no elements.
	 *
	 * @return {@code true} if this list contains no elements
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Return a comma separated String with the names of all extensions in this list.
	 *
	 * @return names of these extensions
	 */
	public String getExtensionNames() {
		StringBuilder b = new StringBuilder();
		list.stream().forEach(e -> b.append(e.getType() + ", "));
		if (b.length() > 0) {
			b.delete(b.lastIndexOf(", "), b.length() - 1);
		}

		return b.toString();
	}

}
