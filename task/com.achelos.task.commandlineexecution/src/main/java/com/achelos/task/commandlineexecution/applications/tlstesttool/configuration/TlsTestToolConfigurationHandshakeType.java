package com.achelos.task.commandlineexecution.applications.tlstesttool.configuration;

import com.achelos.task.xmlparser.datastructures.testrunplan.DUTCapabilities;

import java.util.ArrayList;
import java.util.List;

public enum TlsTestToolConfigurationHandshakeType {

	/** . */
	NORMAL("normal"),
	/** . */
	SessionResumptionWithSessionTicket("resumptionWithSessionTicket"),
	/**  */
	SessionResumptionWithSessionID("resumptionWithSessionID"),
	/** */
	ZERO_RTT("zeroRTT");

	private final String value;

	/**
	 * Default constructor.
	 *
	 * @param value value of the configuration option.
	 */
	TlsTestToolConfigurationHandshakeType(final String value) {
		this.value = value;
	}

	@Override
	public final String toString() {
		return value;
	}

	/**
	 * Retrieve a list of TlsTestToolConfigurationHandshakeType corresponding to the DUT Capabilites provided.
	 * @param dutCapabilities the session resumption capabilties of the DUT.
	 * @return a list of TlsTestToolConfigurationHandshakeType corresponding to the DUT Capabilites provided.
	 */
	public static List<TlsTestToolConfigurationHandshakeType> getHandshakeTypeFromDUTCapabilities(List<DUTCapabilities> dutCapabilities) {
		var list = new ArrayList<TlsTestToolConfigurationHandshakeType>();

		if(dutCapabilities!=null) {
			if (dutCapabilities.contains(DUTCapabilities.SESSION_ID)) {
				list.add(SessionResumptionWithSessionID);
			}
			if (dutCapabilities.contains(DUTCapabilities.SESSION_TICKET)) {
				list.add(SessionResumptionWithSessionTicket);
			}
		}
		return list;
	}
}