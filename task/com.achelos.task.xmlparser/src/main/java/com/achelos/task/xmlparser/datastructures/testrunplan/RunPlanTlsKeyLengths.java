package com.achelos.task.xmlparser.datastructures.testrunplan;

import java.util.HashMap;
import java.util.List;

import com.achelos.task.commons.enums.TlsVersion;

/**
 * Internal data strutuce representing the TLS Key Lengths data stored in the Test Run Plan file.
 */
public class RunPlanTlsKeyLengths {

	/**
	 * Internal class combining an integer keyLength value, with a boolean indicating whether this key length is supported or not.
	 */
	public static class KeyLengths {
		private final Integer keyLength;
		private final Boolean supported;

		/**
		 * Constructor setting keyLength and supported boolean.
		 */
		public KeyLengths(final Integer keyLength, final Boolean supported) {
			this.keyLength = keyLength;
			this.supported = supported;
		}

		/**
		 * Return the contained key length value.
		 * @return the contained key length value.
		 */
		public Integer getKeyLength() {
			return keyLength;
		}

		/**
		 * Return the information whether this key length value is supported.
		 * @return information whether this key length value is supported.
		 */
		public Boolean isSupported() {
			return supported;
		}
	}

	private final HashMap<TlsVersion, List<KeyLengths>> rsaKeyLengths;
	private final HashMap<TlsVersion, List<KeyLengths>> dsaKeyLengths;
	private final HashMap<TlsVersion, List<KeyLengths>> dheKeyLengths;

	/**
	 * Constructor setting information on supported RSA, DSA, and DHE Key Lengths.
	 */
	public RunPlanTlsKeyLengths(final HashMap<TlsVersion, List<KeyLengths>> rsaKeyLengths,
			final HashMap<TlsVersion, List<KeyLengths>> dsaKeyLengths,
			final HashMap<TlsVersion, List<KeyLengths>> dheKeyLengths) {
		this.rsaKeyLengths = new HashMap<>(rsaKeyLengths);
		this.dsaKeyLengths = new HashMap<>(dsaKeyLengths);
		this.dheKeyLengths = new HashMap<>(dheKeyLengths);
	}

	/**
	 * Return the information on key length support regarding RSA.
	 * @return information on key length support regarding RSA.
	 */
	public HashMap<TlsVersion, List<KeyLengths>> getRsaKeyLengths() {
		return new HashMap<>(rsaKeyLengths);
	}

	/**
	 * Return the information on key length support regarding DSA.
	 * @return information on key length support regarding DSA.
	 */
	public HashMap<TlsVersion, List<KeyLengths>> getDsaKeyLengths() {
		return new HashMap<>(dsaKeyLengths);
	}

	/**
	 * Return the information on key length support regarding DHE.
	 * @return information on key length support regarding DHE.
	 */
	public HashMap<TlsVersion, List<KeyLengths>> getDheKeyLengths() {
		return new HashMap<>(dheKeyLengths);
	}

}
