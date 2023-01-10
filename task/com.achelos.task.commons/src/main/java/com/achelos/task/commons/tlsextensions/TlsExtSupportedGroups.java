package com.achelos.task.commons.tlsextensions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsNamedCurves;


/**
 * Representation of a TLS Supported Groups extension.
 *
 * @see https://tools.ietf.org/html/rfc4492#section-5.1.1
 */
public class TlsExtSupportedGroups extends TlsExtension {
	private final List<TlsNamedCurves> ellipticCurveList = new ArrayList<>();

	/**
	 * Default constructor.
	 */
	public TlsExtSupportedGroups() {
		super(TlsExtensionTypes.supported_groups);
	}


	/**
	 * Add a named group.
	 *
	 * @param namedGroup Named group to add
	 */
	public final void addNamedGroup(final TlsNamedCurves namedGroup) {
		ellipticCurveList.add(namedGroup);
	}


	/**
	 * Remove a named group.
	 *
	 * @param namedGroup Named group to remove
	 */
	public final void removeNamedGroup(final TlsNamedCurves namedGroup) {
		ellipticCurveList.remove(namedGroup);
	}


	/**
	 * Find a named group.
	 *
	 * @param namedGroup Named group to search for
	 * @return elliptic curve if this list contains the specified element
	 */
	public final boolean findNamedGroup(final TlsNamedCurves namedGroup) {
		return ellipticCurveList.contains(namedGroup);
	}


	/**
	 * Get the number of elements in the extension.
	 *
	 * @return number of elements in the extension.
	 */
	public final int getNumElements() {
		return ellipticCurveList.size();
	}


	@Override
	protected final byte[] getData() {
		final int length = 2 * ellipticCurveList.size();
		final ByteBuffer buffer = ByteBuffer.allocate(2 + length);
		buffer.putShort((short) length);
		for (TlsNamedCurves ellipticCurve : ellipticCurveList) {
			buffer.putShort((short) ellipticCurve.getValue());
		}
		buffer.flip();
		return buffer.array();
	}


	/**
	 * @return default Supported Groups.
	 */
	public static TlsExtSupportedGroups createDefault() {
		final TlsExtSupportedGroups extension = new TlsExtSupportedGroups();
		extension.addNamedGroup(TlsNamedCurves.secp521r1);
		extension.addNamedGroup(TlsNamedCurves.brainpoolP512r1);
		extension.addNamedGroup(TlsNamedCurves.secp384r1);
		extension.addNamedGroup(TlsNamedCurves.brainpoolP384r1);
		extension.addNamedGroup(TlsNamedCurves.secp256r1);
		extension.addNamedGroup(TlsNamedCurves.secp256k1);
		extension.addNamedGroup(TlsNamedCurves.brainpoolP256r1);
		extension.addNamedGroup(TlsNamedCurves.secp224r1);
		extension.addNamedGroup(TlsNamedCurves.secp224k1);
		extension.addNamedGroup(TlsNamedCurves.secp192r1);
		extension.addNamedGroup(TlsNamedCurves.secp192k1);
		return extension;
	}


	@Override
	public final TlsExtension createExtension(final byte[] data) {
		final byte[] supportedGroups = data;
		// First 2 bytes is the length of the buffer
		for (int i = 2; i < supportedGroups.length; i += 2) {
			TlsNamedCurves curve = TlsNamedCurves.valueOf(supportedGroups[i], supportedGroups[i + 1]);
			if (curve != null) {
				try {
					TlsExtSupportedGroups extention = new TlsExtSupportedGroups();
					extention.addNamedGroup(curve);
					return extention;
				} catch (Exception e) {
					// Ignore
				}
			}
		}
		return null;
	}
}
