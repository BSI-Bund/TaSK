package com.achelos.task.commons.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.achelos.task.commons.tools.StringTools;


/**
 * All assigned TLS cipher suites from the TLS Cipher Suite Registry.
 *
 * @see <a href="http://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-parameters-4">TLS Cipher Suite
 * Registry</a>
 */
public enum TlsCipherSuite {
	/** TLS_NULL_WITH_NULL_NULL((byte) 0x00, (byte) 0x00). */
	TLS_NULL_WITH_NULL_NULL((byte) 0x00, (byte) 0x00, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_NULL_MD5((byte) 0x00, (byte) 0x01). */
	TLS_RSA_WITH_NULL_MD5((byte) 0x00, (byte) 0x01, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_NULL_SHA((byte) 0x00, (byte) 0x02). */
	TLS_RSA_WITH_NULL_SHA((byte) 0x00, (byte) 0x02, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x03). */
	TLS_RSA_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x03, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x04). */
	TLS_RSA_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x04, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x05). */
	TLS_RSA_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x05, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5((byte) 0x00, (byte) 0x06). */
	TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5((byte) 0x00, (byte) 0x06, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_IDEA_CBC_SHA((byte) 0x00, (byte) 0x07). */
	TLS_RSA_WITH_IDEA_CBC_SHA((byte) 0x00, (byte) 0x07, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x08). */
	TLS_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x08, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x09). */
	TLS_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x09, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x0A). */
	TLS_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x0A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x0B). */
	TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x0B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_DSS_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x0C). */
	TLS_DH_DSS_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x0C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x0D). */
	TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x0D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x0E). */
	TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x0E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x0F). */
	TLS_DH_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x0F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x10). */
	TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x10, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x11). */
	TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x11, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_DSS_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x12). */
	TLS_DHE_DSS_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x12, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x13). */
	TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x13, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x14). */
	TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x14, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x15). */
	TLS_DHE_RSA_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x15, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x16). */
	TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x16, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_anon_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x17). */
	TLS_DH_anon_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x17, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x18). */
	TLS_DH_anon_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x18, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x19). */
	TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA((byte) 0x00, (byte) 0x19, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_anon_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x1A). */
	TLS_DH_anon_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x1A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DH_anon_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x1B). */
	TLS_DH_anon_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x1B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x1E). */
	TLS_KRB5_WITH_DES_CBC_SHA((byte) 0x00, (byte) 0x1E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x1F). */
	TLS_KRB5_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x1F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x20). */
	TLS_KRB5_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x20, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_KRB5_WITH_IDEA_CBC_SHA((byte) 0x00, (byte) 0x21). */
	TLS_KRB5_WITH_IDEA_CBC_SHA((byte) 0x00, (byte) 0x21, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_DES_CBC_MD5((byte) 0x00, (byte) 0x22). */
	TLS_KRB5_WITH_DES_CBC_MD5((byte) 0x00, (byte) 0x22, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_3DES_EDE_CBC_MD5((byte) 0x00, (byte) 0x23). */
	TLS_KRB5_WITH_3DES_EDE_CBC_MD5((byte) 0x00, (byte) 0x23, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x24). */
	TLS_KRB5_WITH_RC4_128_MD5((byte) 0x00, (byte) 0x24, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_KRB5_WITH_IDEA_CBC_MD5((byte) 0x00, (byte) 0x25). */
	TLS_KRB5_WITH_IDEA_CBC_MD5((byte) 0x00, (byte) 0x25, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA((byte) 0x00, (byte) 0x26). */
	TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA((byte) 0x00, (byte) 0x26, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA((byte) 0x00, (byte) 0x27). */
	TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA((byte) 0x00, (byte) 0x27, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_KRB5_EXPORT_WITH_RC4_40_SHA((byte) 0x00, (byte) 0x28). */
	TLS_KRB5_EXPORT_WITH_RC4_40_SHA((byte) 0x00, (byte) 0x28, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5((byte) 0x00, (byte) 0x29). */
	TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5((byte) 0x00, (byte) 0x29, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5((byte) 0x00, (byte) 0x2A). */
	TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5((byte) 0x00, (byte) 0x2A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_KRB5_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x2B). */
	TLS_KRB5_EXPORT_WITH_RC4_40_MD5((byte) 0x00, (byte) 0x2B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2C). */
	TLS_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2D). */
	TLS_DHE_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2E). */
	TLS_RSA_PSK_WITH_NULL_SHA((byte) 0x00, (byte) 0x2E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x2F). */
	TLS_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x2F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x30). */
	TLS_DH_DSS_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x30, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x31). */
	TLS_DH_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x31, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x32). */
	TLS_DHE_DSS_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x32, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x33). */
	TLS_DHE_RSA_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x33, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x34). */
	TLS_DH_anon_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x34, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x35). */
	TLS_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x35, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x36). */
	TLS_DH_DSS_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x36, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x37). */
	TLS_DH_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x37, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x38). */
	TLS_DHE_DSS_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x38, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x39). */
	TLS_DHE_RSA_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x39, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x3A). */
	TLS_DH_anon_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x3A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_NULL_SHA256((byte) 0x00, (byte) 0x3B). */
	TLS_RSA_WITH_NULL_SHA256((byte) 0x00, (byte) 0x3B, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3C). */
	TLS_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3C, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x3D). */
	TLS_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x3D, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3E). */
	TLS_DH_DSS_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3E, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3F). */
	TLS_DH_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x3F, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x40). */
	TLS_DHE_DSS_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x40, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x41). */
	TLS_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x41, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x42). */
	TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x42, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x43). */
	TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x43, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x44). */
	TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x44, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x45). */
	TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x45, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x46). */
	TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA((byte) 0x00, (byte) 0x46, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x67). */
	TLS_DHE_RSA_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x67, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x68). */
	TLS_DH_DSS_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x68, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x69). */
	TLS_DH_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x69, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6A). */
	TLS_DHE_DSS_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6A, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6B). */
	TLS_DHE_RSA_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6B, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x6C). */
	TLS_DH_anon_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0x6C, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6D). */
	TLS_DH_anon_WITH_AES_256_CBC_SHA256((byte) 0x00, (byte) 0x6D, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x84). */
	TLS_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x84, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x85). */
	TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x85, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x86). */
	TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x86, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x87). */
	TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x87, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x88). */
	TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x88, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x89). */
	TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA((byte) 0x00, (byte) 0x89, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x8A). */
	TLS_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x8A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x8B). */
	TLS_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x8B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x8C). */
	TLS_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x8C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x8D). */
	TLS_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x8D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x8E). */
	TLS_DHE_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x8E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x8F). */
	TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x8F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_DHE_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x90). */
	TLS_DHE_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x90, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x91). */
	TLS_DHE_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x91, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x92). */
	TLS_RSA_PSK_WITH_RC4_128_SHA((byte) 0x00, (byte) 0x92, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x93). */
	TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0x00, (byte) 0x93, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_RSA_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x94). */
	TLS_RSA_PSK_WITH_AES_128_CBC_SHA((byte) 0x00, (byte) 0x94, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x95). */
	TLS_RSA_PSK_WITH_AES_256_CBC_SHA((byte) 0x00, (byte) 0x95, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x96). */
	TLS_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x96, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x97). */
	TLS_DH_DSS_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x97, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x98). */
	TLS_DH_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x98, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x99). */
	TLS_DHE_DSS_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x99, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x9A). */
	TLS_DHE_RSA_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x9A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x9B). */
	TLS_DH_anon_WITH_SEED_CBC_SHA((byte) 0x00, (byte) 0x9B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0x9C). */
	TLS_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0x9C, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0x9D). */
	TLS_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0x9D, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0x9E). */
	TLS_DHE_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0x9E, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0x9F). */
	TLS_DHE_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0x9F, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA0). */
	TLS_DH_RSA_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA0, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA1). */
	TLS_DH_RSA_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA1, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA2). */
	TLS_DHE_DSS_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA2, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA3). */
	TLS_DHE_DSS_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA3, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA4). */
	TLS_DH_DSS_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA4, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA5). */
	TLS_DH_DSS_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA5, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA6). */
	TLS_DH_anon_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA6, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA7). */
	TLS_DH_anon_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA7, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA8). */
	TLS_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xA8, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA9). */
	TLS_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xA9, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xAA). */
	TLS_DHE_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xAA, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xAB). */
	TLS_DHE_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xAB, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xAC). */
	TLS_RSA_PSK_WITH_AES_128_GCM_SHA256((byte) 0x00, (byte) 0xAC, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xAD). */
	TLS_RSA_PSK_WITH_AES_256_GCM_SHA384((byte) 0x00, (byte) 0xAD, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xAE). */
	TLS_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xAE, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xAF). */
	TLS_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xAF, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB0). */
	TLS_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB0, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB1). */
	TLS_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB1, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xB2). */
	TLS_DHE_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xB2, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xB3). */
	TLS_DHE_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xB3, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB4). */
	TLS_DHE_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB4, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB5). */
	TLS_DHE_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB5, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xB6). */
	TLS_RSA_PSK_WITH_AES_128_CBC_SHA256((byte) 0x00, (byte) 0xB6, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xB7). */
	TLS_RSA_PSK_WITH_AES_256_CBC_SHA384((byte) 0x00, (byte) 0xB7, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB8). */
	TLS_RSA_PSK_WITH_NULL_SHA256((byte) 0x00, (byte) 0xB8, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB9). */
	TLS_RSA_PSK_WITH_NULL_SHA384((byte) 0x00, (byte) 0xB9, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBA). */
	TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBA, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBB). */
	TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBB, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBC). */
	TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBC, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBD). */
	TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBD, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBE). */
	TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBE, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBF). */
	TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256((byte) 0x00, (byte) 0xBF, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC0). */
	TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC0, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC1). */
	TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC1, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC2). */
	TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC2, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC3). */
	TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC3, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC4). */
	TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC4, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC5). */
	TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256((byte) 0x00, (byte) 0xC5, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_EMPTY_RENEGOTIATION_INFO_SCSV((byte) 0x00, (byte) 0xFF). */
	TLS_EMPTY_RENEGOTIATION_INFO_SCSV((byte) 0x00, (byte) 0xFF, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_AES_128_GCM_SHA256((byte) 0x13, (byte) 0x01). */
	TLS_AES_128_GCM_SHA256((byte) 0x13, (byte) 0x01, TlsVersion.TLS_V1_3),
	/** TLS_AES_256_GCM_SHA384((byte) 0x13,(byte) 0x02). */
	TLS_AES_256_GCM_SHA384((byte) 0x13, (byte) 0x02, TlsVersion.TLS_V1_3),
	/** TLS_CHACHA20_POLY1305_SHA256((byte) 0x13,(byte) 0x03). */
	TLS_CHACHA20_POLY1305_SHA256((byte) 0x13, (byte) 0x03, TlsVersion.TLS_V1_3),
	/** TLS_AES_128_CCM_SHA256((byte) 0x13,(byte) 0x04). */
	TLS_AES_128_CCM_SHA256((byte) 0x13, (byte) 0x04, TlsVersion.TLS_V1_3),
	/** TLS_AES_128_CCM_8_SHA256((byte) 0x13,(byte) 0x05). */
	TLS_AES_128_CCM_8_SHA256((byte) 0x13, (byte) 0x05, TlsVersion.TLS_V1_3),
	/** TLS_FALLBACK_SCSV((byte) 0x56, (byte) 0x00). */
	TLS_FALLBACK_SCSV((byte) 0x56, (byte) 0x00, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x01). */
	TLS_ECDH_ECDSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x01, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x02). */
	TLS_ECDH_ECDSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x02, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x03). */
	TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x03, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x04). */
	TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x04, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x05). */
	TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x05, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x06). */
	TLS_ECDHE_ECDSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x06, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x07). */
	TLS_ECDHE_ECDSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x07, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x08). */
	TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x08, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x09). */
	TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x09, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x0A). */
	TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x0A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x0B). */
	TLS_ECDH_RSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x0B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x0C). */
	TLS_ECDH_RSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x0C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x0D). */
	TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x0D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDH_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x0E). */
	TLS_ECDH_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x0E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x0F). */
	TLS_ECDH_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x0F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x10). */
	TLS_ECDHE_RSA_WITH_NULL_SHA((byte) 0xC0, (byte) 0x10, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x11). */
	TLS_ECDHE_RSA_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x11, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x12). */
	TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x12, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x13). */
	TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x13, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x14). */
	TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x14, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_anon_WITH_NULL_SHA((byte) 0xC0, (byte) 0x15). */
	TLS_ECDH_anon_WITH_NULL_SHA((byte) 0xC0, (byte) 0x15, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_anon_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x16). */
	TLS_ECDH_anon_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x16, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x17). */
	TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x17, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDH_anon_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x18). */
	TLS_ECDH_anon_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x18, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_anon_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x19). */
	TLS_ECDH_anon_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x19, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1A). */
	TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1B). */
	TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1C). */
	TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x1C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_SRP_SHA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1D). */
	TLS_SRP_SHA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1E). */
	TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1F). */
	TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x1F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x20). */
	TLS_SRP_SHA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x20, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x21). */
	TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x21, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x22). */
	TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x22, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x23). */
	TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x23, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x24). */
	TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x24, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x25). */
	TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x25, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x26). */
	TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x26, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x27). */
	TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x27, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x28). */
	TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x28, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x29). */
	TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x29, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x2A). */
	TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x2A, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2B). */
	TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2B, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x2C). */
	TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x2C, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2D). */
	TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2D, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x2E). */
	TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x2E, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2F). */
	TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x2F, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x30). */
	TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x30, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x31). */
	TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256((byte) 0xC0, (byte) 0x31, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x32). */
	TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384((byte) 0xC0, (byte) 0x32, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x33). */
	TLS_ECDHE_PSK_WITH_RC4_128_SHA((byte) 0xC0, (byte) 0x33, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x34). */
	TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA((byte) 0xC0, (byte) 0x34, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1),
	/** TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x35). */
	TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA((byte) 0xC0, (byte) 0x35, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x36). */
	TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA((byte) 0xC0, (byte) 0x36, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x37). */
	TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256((byte) 0xC0, (byte) 0x37, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x38). */
	TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384((byte) 0xC0, (byte) 0x38, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_NULL_SHA((byte) 0xC0, (byte) 0x39). */
	TLS_ECDHE_PSK_WITH_NULL_SHA((byte) 0xC0, (byte) 0x39, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_NULL_SHA256((byte) 0xC0, (byte) 0x3A). */
	TLS_ECDHE_PSK_WITH_NULL_SHA256((byte) 0xC0, (byte) 0x3A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_NULL_SHA384((byte) 0xC0, (byte) 0x3B). */
	TLS_ECDHE_PSK_WITH_NULL_SHA384((byte) 0xC0, (byte) 0x3B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x3C). */
	TLS_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x3C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x3D). */
	TLS_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x3D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x3E). */
	TLS_DH_DSS_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x3E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x3F). */
	TLS_DH_DSS_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x3F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x40). */
	TLS_DH_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x40, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x41). */
	TLS_DH_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x41, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x42). */
	TLS_DHE_DSS_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x42, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x43). */
	TLS_DHE_DSS_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x43, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x44). */
	TLS_DHE_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x44, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x45). */
	TLS_DHE_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x45, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x46). */
	TLS_DH_anon_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x46, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x47). */
	TLS_DH_anon_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x47, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x48). */
	TLS_ECDHE_ECDSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x48, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x49). */
	TLS_ECDHE_ECDSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x49, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4A). */
	TLS_ECDH_ECDSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4B). */
	TLS_ECDH_ECDSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4C). */
	TLS_ECDHE_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4C, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4D). */
	TLS_ECDHE_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4D, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4E). */
	TLS_ECDH_RSA_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x4E, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4F). */
	TLS_ECDH_RSA_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x4F, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x50). */
	TLS_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x50, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x51). */
	TLS_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x51, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x52). */
	TLS_DHE_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x52, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x53). */
	TLS_DHE_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x53, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x54). */
	TLS_DH_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x54, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x55). */
	TLS_DH_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x55, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x56). */
	TLS_DHE_DSS_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x56, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x57). */
	TLS_DHE_DSS_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x57, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x58). */
	TLS_DH_DSS_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x58, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x59). */
	TLS_DH_DSS_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x59, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5A). */
	TLS_DH_anon_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5A, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5B). */
	TLS_DH_anon_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5B, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5C). */
	TLS_ECDHE_ECDSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5C, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5D). */
	TLS_ECDHE_ECDSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5D, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5E). */
	TLS_ECDH_ECDSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x5E, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5F). */
	TLS_ECDH_ECDSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x5F, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x60). */
	TLS_ECDHE_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x60, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x61). */
	TLS_ECDHE_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x61, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x62). */
	TLS_ECDH_RSA_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x62, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x63). */
	TLS_ECDH_RSA_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x63, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x64). */
	TLS_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x64, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x65). */
	TLS_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x65, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x66). */
	TLS_DHE_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x66, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x67). */
	TLS_DHE_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x67, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x68). */
	TLS_RSA_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x68, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x69). */
	TLS_RSA_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x69, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6A). */
	TLS_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6A, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6B). */
	TLS_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6B, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6C). */
	TLS_DHE_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6C, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6D). */
	TLS_DHE_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6D, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6E). */
	TLS_RSA_PSK_WITH_ARIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x6E, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6F). */
	TLS_RSA_PSK_WITH_ARIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x6F, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x70). */
	TLS_ECDHE_PSK_WITH_ARIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x70, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x71). */
	TLS_ECDHE_PSK_WITH_ARIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x71, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x72). */
	TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x72, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x73). */
	TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x73, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x74). */
	TLS_ECDH_ECDSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x74, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x75). */
	TLS_ECDH_ECDSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x75, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x76). */
	TLS_ECDHE_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x76, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x77). */
	TLS_ECDHE_RSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x77, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x78). */
	TLS_ECDH_RSA_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x78, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x79). */
	TLS_ECDH_RSA_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x79, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7A). */
	TLS_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7A, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7B). */
	TLS_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7B, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7C). */
	TLS_DHE_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7C, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7D). */
	TLS_DHE_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7D, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7E). */
	TLS_DH_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x7E, TlsVersion.TLS_V1_2),
	/** TLS_DH_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7F). */
	TLS_DH_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x7F, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x80). */
	TLS_DHE_DSS_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x80, TlsVersion.TLS_V1_2),
	/** TLS_DHE_DSS_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x81). */
	TLS_DHE_DSS_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x81, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x82). */
	TLS_DH_DSS_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x82, TlsVersion.TLS_V1_2),
	/** TLS_DH_DSS_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x83). */
	TLS_DH_DSS_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x83, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x84). */
	TLS_DH_anon_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x84, TlsVersion.TLS_V1_2),
	/** TLS_DH_anon_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x85). */
	TLS_DH_anon_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x85, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x86). */
	TLS_ECDHE_ECDSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x86, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x87). */
	TLS_ECDHE_ECDSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x87, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x88). */
	TLS_ECDH_ECDSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x88, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_ECDSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x89). */
	TLS_ECDH_ECDSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x89, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8A). */
	TLS_ECDHE_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8A, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8B). */
	TLS_ECDHE_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8B, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8C). */
	TLS_ECDH_RSA_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8C, TlsVersion.TLS_V1_2),
	/** TLS_ECDH_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8D). */
	TLS_ECDH_RSA_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8D, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8E). */
	TLS_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x8E, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8F). */
	TLS_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x8F, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x90). */
	TLS_DHE_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x90, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x91). */
	TLS_DHE_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x91, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x92). */
	TLS_RSA_PSK_WITH_CAMELLIA_128_GCM_SHA256((byte) 0xC0, (byte) 0x92, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x93). */
	TLS_RSA_PSK_WITH_CAMELLIA_256_GCM_SHA384((byte) 0xC0, (byte) 0x93, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x94). */
	TLS_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x94, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x95). */
	TLS_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x95, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x96). */
	TLS_DHE_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x96, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x97). */
	TLS_DHE_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x97, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x98). */
	TLS_RSA_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x98, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x99). */
	TLS_RSA_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x99, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x9A). */
	TLS_ECDHE_PSK_WITH_CAMELLIA_128_CBC_SHA256((byte) 0xC0, (byte) 0x9A, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x9B). */
	TLS_ECDHE_PSK_WITH_CAMELLIA_256_CBC_SHA384((byte) 0xC0, (byte) 0x9B, TlsVersion.TLS_V1_0, TlsVersion.TLS_V1_1,
			TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0x9C). */
	TLS_RSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0x9C, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0x9D). */
	TLS_RSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0x9D, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0x9E). */
	TLS_DHE_RSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0x9E, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0x9F). */
	TLS_DHE_RSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0x9F, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA0). */
	TLS_RSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA0, TlsVersion.TLS_V1_2),
	/** TLS_RSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA1). */
	TLS_RSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA1, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA2). */
	TLS_DHE_RSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA2, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA3). */
	TLS_DHE_RSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA3, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xA4). */
	TLS_PSK_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xA4, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xA5). */
	TLS_PSK_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xA5, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xA6). */
	TLS_DHE_PSK_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xA6, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xA7). */
	TLS_DHE_PSK_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xA7, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA8). */
	TLS_PSK_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xA8, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA9). */
	TLS_PSK_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xA9, TlsVersion.TLS_V1_2),
	/** TLS_PSK_DHE_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xAA). */
	TLS_PSK_DHE_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xAA, TlsVersion.TLS_V1_2),
	/** TLS_PSK_DHE_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xAB). */
	TLS_PSK_DHE_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xAB, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xAC). */
	TLS_ECDHE_ECDSA_WITH_AES_128_CCM((byte) 0xC0, (byte) 0xAC, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xAD). */
	TLS_ECDHE_ECDSA_WITH_AES_256_CCM((byte) 0xC0, (byte) 0xAD, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xAE). */
	TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8((byte) 0xC0, (byte) 0xAE, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xAF). */
	TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8((byte) 0xC0, (byte) 0xAF, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xA8). */
	TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xA8, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xA9). */
	TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xA9, TlsVersion.TLS_V1_2),
	/** TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAA). */
	TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAA, TlsVersion.TLS_V1_2),
	/** TLS_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAB). */
	TLS_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAB, TlsVersion.TLS_V1_2),
	/** TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAC). */
	TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAC, TlsVersion.TLS_V1_2),
	/** TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAD). */
	TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAD, TlsVersion.TLS_V1_2),
	/** TLS_RSA_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAD). */
	TLS_RSA_PSK_WITH_CHACHA20_POLY1305_SHA256((byte) 0xCC, (byte) 0xAE, TlsVersion.TLS_V1_2);

	private byte upper;
	private byte lower;
	private TlsVersion[] versions;

	/**
	 * The constructor of the cipher suite enumeration.
	 *
	 * @param upper The upper byte.
	 * @param lower The lower byte.
	 * @param versions The compatible TLS versions.
	 */
	TlsCipherSuite(final byte upper, final byte lower, final TlsVersion... versions) {
		this.upper = upper;
		this.lower = lower;
		this.versions = versions;
	}


	/**
	 * Return the cipher suite's value as hexadecimal pair.
	 *
	 * @return String with two hexadecimal encoded bytes prefixed with "0x", separated by comma, and enclosed in
	 * parentheses (e.g., "(0x00,0x2F)")
	 */
	public String getValuePair() {
		return String.format("(0x%02x,0x%02x)", upper, lower);
	}


	/**
	 * Return the cipher suite's value as hexadecimal string.
	 *
	 * @return String with two hexadecimal encoded bytes separated by space (e.g., "00 2F")
	 */
	public String getValueHexString() {
		return String.format("%02x %02x", upper, lower);
	}


	/**
	 * Return the Cipher Suite's MAC bit length.
	 *
	 * @return Integer with the bit length of the MAC
	 * @throws Exception if MAC of the Cipher Suite can not be resolved.
	 */
	public final int getBitLenMAC() throws Exception {
		int bitLenMAC;
		final int csCcm128 = 128;
		final int sha384 = 384;
		final int sha256 = 256;
		final int sha = 160;
		final int ccm8 = 64;
		if (name().contains("MD5")) {
			return csCcm128;
		}
		if (name().contains("SHA384")) {
			bitLenMAC = sha384;
		} else if (name().contains("SHA256")) {
			bitLenMAC = sha256;
		} else if (name().contains("SHA")) {
			bitLenMAC = sha;
		} else if (name().contains("CCM_8")) {
			bitLenMAC = ccm8;
		} else if (name().contains("CCM")) {
			bitLenMAC = csCcm128;
		} else {
			throw new Exception("Unable to resolve MAC from Cipher Suite: " + name());
		}
		return bitLenMAC;
	}


	/**
	 * Gets all compatible TLS versions.
	 *
	 * @return all compatible TLS versions.
	 */
	public final TlsVersion[] getTlsVersions() {
		return versions.clone();
	}


	/**
	 * Checks if the desired version is supported.
	 *
	 * @param desiredVersion The desired version.
	 * @return whether the desired version is supported.
	 */
	public final boolean isVersionSupported(final TlsVersion desiredVersion) {
		for (TlsVersion version : versions) {
			if (version == desiredVersion) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Checks if the desired version is supported and if it is the only supported version.
	 *
	 * @param desiredVersion The desired version.
	 * @return true, if the desired version is supported and if it is the only supported version. false, otherwise.
	 */
	public final boolean isOnlyVersionSupported(final TlsVersion desiredVersion) {
		return versions.length == 1 && isVersionSupported(desiredVersion);
	}


	/**
	 * Returns all cipher suites for given TLS version.
	 *
	 * @param version The TLS version.
	 * @return a list with all cipher suites for given TLS version.
	 */
	public static List<TlsCipherSuite> getAll(final TlsVersion version) {
		final List<TlsCipherSuite> result = new ArrayList<>();
		if (version == TlsVersion.TLS_V1_3) {
			result.add(TlsCipherSuite.TLS_AES_128_GCM_SHA256);
			result.add(TlsCipherSuite.TLS_AES_256_GCM_SHA384);
			result.add(TlsCipherSuite.TLS_CHACHA20_POLY1305_SHA256);
			result.add(TlsCipherSuite.TLS_AES_128_CCM_8_SHA256);
			result.add(TlsCipherSuite.TLS_AES_128_CCM_SHA256);
		} else {
			result.addAll(filterByName("TLS_RSA_"));
			result.addAll(filterByName("TLS_DHE_"));
			result.addAll(filterByName("TLS_ECDHE_"));
			result.addAll(filterByName("TLS_PSK_"));
			result.addAll(filterByName("TLS_DH_"));
			result.addAll(filterByName("TLS_ECDH_"));
			result.addAll(filterByName("TLS_SRP_"));
		}
		return result;
	}


	@Override
	public String toString() {
		return name() + ' ' + getValuePair();
	}

	/**
	 * Returns a human readable name of this cipher suite.
	 *
	 * @return cipher suite's name
	 */
	public String getName() {
		return name();
	}


	/**
	 * Filter all known cipher suites and return those that contain a given string in their name.
	 *
	 * @param search String to search (e.g., "_CBC_")
	 * @return the list of cipher suites that contain the given string.
	 */
	public static List<TlsCipherSuite> filterByName(final String search) {
		final List<TlsCipherSuite> result = new ArrayList<>();
		for (TlsCipherSuite tlsCipherSuite : values()) {
			if (tlsCipherSuite.name().contains(search)) {
				result.add(tlsCipherSuite);
			}
		}
		return result;
	}


	/**
	 * Filter all known cipher suites and return those whose name matches a given pattern.
	 *
	 * @param pattern Pattern to match (e.g., "^.*_AES_256_CBC_SHA$")
	 * @return the list of cipher suites with names matching the given pattern
	 */
	public static List<TlsCipherSuite> filterByName(final Pattern pattern) {
		final List<TlsCipherSuite> result = new ArrayList<>();
		for (TlsCipherSuite tlsCipherSuite : values()) {
			if (pattern.matcher(tlsCipherSuite.name()).matches()) {
				result.add(tlsCipherSuite);
			}
		}
		return result;
	}


	/**
	 * Filter all known cipher suites and return those that contain two given strings in their name.
	 *
	 * @param search1 string to search (e.g., ""TLS_RSA_WITH_"")
	 * @param search2 string to search (e.g., "_CBC_")
	 * @return the list of cipher suites that contain the given strings.
	 */
	public static List<TlsCipherSuite> filterByNames(final String search1, final String search2) {
		final List<TlsCipherSuite> result = new ArrayList<>();
		for (TlsCipherSuite tlsCipherSuite : values()) {
			if (tlsCipherSuite.name().contains(search1) && tlsCipherSuite.name().contains(search2)) {
				result.add(tlsCipherSuite);
			}
		}
		return result;
	}


	/**
	 * Method takes a string representation of one or more cipher suites concatenated (format: C0 2B C0 2F...) and find
	 * all consisting cipher suites which are returned within object representation.
	 *
	 * @param cipherSuiteList the cipher suites list in String representation
	 * @return List<TlsCipherSuite>
	 */
	public static List<TlsCipherSuite> parseCipherSuiteStringList(final String[] cipherSuiteList) {
		return parseCipherSuiteStringList(Arrays.asList(cipherSuiteList));
	}


	/**
	 * Method takes a string representation of one or more cipher suites concatenated (format: C0 2B C0 2F...) and find
	 * all consisting cipher suites which are returned within object representation.
	 *
	 * @param cipherSuiteList the cipher suites list in String representation
	 * @return the List<TlsCipherSuite>
	 */
	public static List<TlsCipherSuite> parseCipherSuiteStringList(final String cipherSuiteList) {
		return parseCipherSuiteStringList(cipherSuiteList, false, null);
	}

	/**
	 * Method takes a string representation of one or more cipher suites concatenated (format: C0 2B C0 2F...) and find
	 * all consisting cipher suites which are returned within object representation.
	 *
	 * @param cipherSuiteList the cipher suites list in String representation
	 * @return the List<TlsCipherSuite>
	 */
	public static List<TlsCipherSuite> parseCipherSuiteStringList(final String cipherSuiteList, boolean filterVersion, TlsVersion tlsVersion) {
		List<TlsCipherSuite> foundCipherSuites = new ArrayList<>();
		if (cipherSuiteList == null || cipherSuiteList.isBlank()) {
			return foundCipherSuites;
		}
		String[] valuePairs = cipherSuiteList.split(" ");
		if (valuePairs.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Either cipher suite list is wrongly formatted or does not contain enough values for a pair: "
							+ cipherSuiteList);
		}
		for (int i = 0; i < valuePairs.length; i += 2) {
			for (TlsCipherSuite cipherSuite : TlsCipherSuite.values()) {
				if (cipherSuite.getValueHexString().equalsIgnoreCase(valuePairs[i] + " " + valuePairs[i + 1])) {
					if(filterVersion && cipherSuite.isVersionSupported(tlsVersion)) {
						foundCipherSuites.add(cipherSuite);
					}
				}
			}
		}
		return foundCipherSuites;
	}


	/**
	 * Method takes a string representation of one or more cipher suites as string array. (format: e.g. (0xC0, 0x2B) and
	 * find all consisting cipher suites which are returned within object representation.
	 *
	 * @param cipherSuiteList the cipher suite array
	 * @return found cipher suites
	 */
	public static List<TlsCipherSuite> parseCipherSuiteStringList(final List<String> cipherSuiteList) {
		List<TlsCipherSuite> foundCipherSuites = new ArrayList<>();

		for (String cs : cipherSuiteList) {
			for (TlsCipherSuite cipherSuite : TlsCipherSuite.values()) {
				if (cipherSuite.getValuePair().equalsIgnoreCase(cs)) {
					foundCipherSuites.add(cipherSuite);
				}
			}
		}
		return foundCipherSuites;
	}


	/**
	 * Gets the cipher suite with matching upper and lower byte values.
	 *
	 * @param upper The upper value.
	 * @param lower The lower value.
	 * @return cipher suite or null if no cipher suites is found.
	 */
	public static TlsCipherSuite valueOf(final byte upper, final byte lower) {
		for (TlsCipherSuite cipherSuite : TlsCipherSuite.values()) {
			if (cipherSuite.upper == upper && cipherSuite.lower == lower) {
				return cipherSuite;
			}
		}
		return null;
	}


	/**
	 * Converts the given hexString into a byte array and gets the cipher suite corresponding cipher suite.
	 *
	 * @param hexString the cipher suite hex string.
	 * @return null if string is invalid e.g. empty or null.
	 */
	public static TlsCipherSuite valueOfHexString(final String hexString) {
		if (null == hexString || hexString.isEmpty()) {
			return null;
		}
		final byte[] bytes = StringTools.toByteArray(hexString.replaceAll(" ", ""));
		if (null == bytes || 2 != bytes.length) {
			return null;
		}
		return valueOf(bytes[0], bytes[1]);
	}


	/**
	 * Gets the cipher suites that support any of the version given in <b>tlsVersions</b> parameter.
	 *
	 * @param tlsVersions The versions to match in cipher suite.
	 * @return the list of all matched cipher suites.
	 */
	public static List<TlsCipherSuite> getCipherSuitesByVersions(final TlsVersion... tlsVersions) {
		List<TlsCipherSuite> cipherSuites = new ArrayList<>();
		for (TlsCipherSuite cipherSuite : TlsCipherSuite.values()) {
			for (TlsVersion tlsVersion : tlsVersions) {
				if (cipherSuite.isVersionSupported(tlsVersion)) {
					cipherSuites.add(cipherSuite);
					break;
				}
			}
		}
		return cipherSuites;
	}
}
