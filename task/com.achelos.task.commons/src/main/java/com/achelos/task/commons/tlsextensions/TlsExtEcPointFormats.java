package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsPointFormats;


/**
 * Representation of a TLS Supported Point Formats extension.
 *
 * @see https://tools.ietf.org/html/rfc4492#section-5.1.2
 */
public class TlsExtEcPointFormats extends TlsExtension {
	private final List<TlsPointFormats> ecPointFormatList = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public TlsExtEcPointFormats() {
		super(TlsExtensionTypes.ec_point_formats);
	}


	/**
	 * Add a supported point format.
	 *
	 * @param pointFormat point format to add
	 */
	public final void addPointFormat(final TlsPointFormats pointFormat) {
		ecPointFormatList.add(pointFormat);
	}


	@Override
	protected final byte[] getData() {
		final int length = ecPointFormatList.size();
		final ByteBuffer buffer = ByteBuffer.allocate(1 + length);
		buffer.put((byte) length);
		for (TlsPointFormats pointFormat : ecPointFormatList) {
			buffer.put(pointFormat.getValue());
		}
		buffer.flip();
		return buffer.array();
	}


	/**
	 * @return Default supported point format.
	 */
	public static TlsExtEcPointFormats createDefault() {
		final TlsExtEcPointFormats extension = new TlsExtEcPointFormats();
		extension.addPointFormat(TlsPointFormats.uncompressed);
		return extension;
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {

		// First byte is the length of the buffer
		for (int i = 2; i < data.length; i++) {
			TlsPointFormats ecPointFormat = TlsPointFormats.valueOf(data[i]);
			if (ecPointFormat != null) {
				TlsExtEcPointFormats extEcPointFormats = new TlsExtEcPointFormats();
				extEcPointFormats.addPointFormat(ecPointFormat);
				return extEcPointFormats;
			}
		}
		return null;
	}
}
