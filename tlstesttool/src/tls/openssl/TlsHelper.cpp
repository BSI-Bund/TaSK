/*
* TLS-Test Tool
* The TLS Test Tool checks the TLS configuration and compliance with the protocol specification for TLS servers and clients.
*
* Licensed under EUPL-1.2-or-later.
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at the LICENSE.md file or visit
*
* https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
#include "TlsHelper.h"
#include "strings/HexStringHelper.h"
#include "openssl/ssl.h"
#include <stdexcept>

namespace TlsTestTool {
namespace OpenSsl {

const std::string TlsHelper::getInternalCipherSuite(const TlsCipherSuite cipherSuite) {
	if ((cipherSuite.first == 0x13) && (cipherSuite.second == 0x02)) {
		return "TLS_AES_256_GCM_SHA384";
	}
	else if ((cipherSuite.first == 0x13) && (cipherSuite.second == 0x03)) {
		return "TLS_CHACHA20_POLY1305_SHA256";
	}
	else if ((cipherSuite.first == 0x13) && (cipherSuite.second == 0x01)) {
		return  "TLS_AES_128_GCM_SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x2c)) {
		return "ECDHE-ECDSA-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x30)) {
		return "ECDHE-RSA-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x9f)) {
		return "DHE-RSA-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xa9)) {
		return "ECDHE-ECDSA-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xa8)) {
		return "ECDHE-RSA-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xaa)) {
		return "DHE-RSA-CHACHA20-POLY1305";
    }
    	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x2b)) {
		return "ECDHE-ECDSA-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x2f)) {
		return "ECDHE-RSA-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x9e)) {
		return "DHE-RSA-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x24)) {
		return "ECDHE-ECDSA-AES256-SHA384";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x28)) {
		return "ECDHE-RSA-AES256-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x6b)) {
		return "DHE-RSA-AES256-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x23)) {
		return "ECDHE-ECDSA-AES128-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x27)) {
		return "ECDHE-RSA-AES128-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x67)) {
		return "DHE-RSA-AES128-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x0a)) {
		return "ECDHE-ECDSA-AES256-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x14)) {
		return "ECDHE-RSA-AES256-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x39)) {
		return "DHE-RSA-AES256-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x09)) {
		return "ECDHE-ECDSA-AES128-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x13)) {
		return "ECDHE-RSA-AES128-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x33)) {
		return "DHE-RSA-AES128-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xad)) {
		return "RSA-PSK-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xab)) {
		return "DHE-PSK-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xae)) {
		return "RSA-PSK-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xad)) {
		return "DHE-PSK-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xac)) {
		return "ECDHE-PSK-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x9d)) {
		return "AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xa9)) {
		return "PSK-AES256-GCM-SHA384";
	}
	else if ((cipherSuite.first == 0xcc) && (cipherSuite.second == 0xab)) {
		return "PSK-CHACHA20-POLY1305";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xac)) {
		return "RSA-PSK-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xaa)) {
		return "DHE-PSK-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x9c)) {
		return "AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xa8)) {
		return "PSK-AES128-GCM-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x3d)) {
		return "AES256-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x3c)) {
		return "AES128-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x38)) {
		return "ECDHE-PSK-AES256-CBC-SHA384";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x36)) {
		return "ECDHE-PSK-AES256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x21)) {
		return "SRP-RSA-AES-256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x20)) {
		return "SRP-AES-256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xb7)) {
		return "RSA-PSK-AES256-CBC-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xb3)) {
		return "DHE-PSK-AES256-CBC-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x95)) {
		return "RSA-PSK-AES256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x91)) {
		return "DHE-PSK-AES256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x35)) {
		return "AES256-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xaf)) {
		return "PSK-AES256-CBC-SHA384";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x8d)) {
		return "PSK-AES256-CBC-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x37)) {
		return "ECDHE-PSK-AES128-CBC-SHA256";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x35)) {
		return "ECDHE-PSK-AES128-CBC-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x1e)) {
		return "SRP-RSA-AES-128-CBC-SHA";
	}
	else if ((cipherSuite.first == 0xc0) && (cipherSuite.second == 0x1d)) {
		return "SRP-AES-128-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xb6)) {
		return "RSA-PSK-AES128-CBC-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xb2)) {
		return "DHE-PSK-AES128-CBC-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x94)) {
		return "RSA-PSK-AES128-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x90)) {
		return "DHE-PSK-AES128-CBC-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x2f)) {
		return "AES128-SHA";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xae)) {
		return "PSK-AES128-CBC-SHA256";
	}
	else if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0x8c)) {
		return "PSK-AES128-CBC-SHA";
	}
	else {
		throw std::invalid_argument("Cipher suite \""
			+ Tooling::HexStringHelper::byteArrayToHexString({cipherSuite.first, cipherSuite.second}) + "\" not supported with OpenSSL");
	}
}

const std::string TlsHelper::getInternalSupportedGroup(const TlsSupportedGroupID supportedGroupID) {
	switch (supportedGroupID) {
        case 1:
			return "sect163k1";
			break;
        case 2:
			return "sect163r1";
			break;    
		case 3:
			return "sect163r2";
			break;
        case 4:
			return "sect193r1";
			break;    
        case 5:
			return "sect193r2";
			break;    
		case 6:
			return "sect233k1";
			break;
		case 7:
			return "sect233r1";
			break;
        case 8:
			return "sect239k1";
			break;
		case 9:
			return "sect283k1";
			break;
		case 10:
			return "sect283r1";
			break;
		case 11:
			return "sect409k1";
			break;
		case 12:
			return "sect409r1";
			break;
		case 13:
			return "sect571k1";
			break;
		case 14:
			return "sect571r1";
			break;
        case 15:
			return "secp160k1";
			break;
        case 16:
			return "secp160r1";
			break;
        case 17:
			return "secp160r2";
			break;            
        case 18:
			return "secp192k1";
			break;
		case 20:
			return "secp224k1";
			break;
		case 21:
			return "secp224r1";
			break;
        case 22:
			return "secp256k1";
			break;
        case 23:
            return"P-256";
            break;
		case 24:
			return "secp384r1";
			break;
		case 25:
			return "secp521r1";
			break;
		case 26:
			return "brainpoolP256r1";
			break;
		case 27:
			return "brainpoolP384r1";
			break;
		case 28:
			return "brainpoolP512r1";
			break;
        case 29:
            return"X25519";
            break;
        case 30:
            return"X448";
            break;
        case 256:
            return"ffdhe2048";
            break;
        case 257:
            return"ffdhe3072";
            break;
        case 258:
            return"ffdhe4096";
            break;
        case 259:
            return"ffdhe6144";
            break;
        case 260:
            return"ffdhe8192";
            break;
		default:
			throw std::invalid_argument("Supported group \""
				+ std::to_string(supportedGroupID) + "\" not supported with OpenSSL");
	}
}


const std::string TlsHelper::getInternalSignatureAlgorithm(const TlsSignatureAndHashAlgorithm signatureAlgorithm) {
    
    std::string mappedSignatureAlgorithm;
    
    
    switch (signatureAlgorithm.first) {
        case 0:
            mappedSignatureAlgorithm = "anonymous";
            break;
        case 1:
            mappedSignatureAlgorithm = "RSA";
            break;
        case 2:
            mappedSignatureAlgorithm = "DSA";
            break;
        case 3:
            mappedSignatureAlgorithm = "ECDSA";
            break;
        default:
            throw std::invalid_argument("Signature Algorithm (\""
			+ std::to_string(signatureAlgorithm.first) + "," + std::to_string(signatureAlgorithm.second) + "\") not supported with OpenSSL");
    }
        
    mappedSignatureAlgorithm += "+";
    
    switch (signatureAlgorithm.second) {
        case 0:
            mappedSignatureAlgorithm += "NONE";
            break;
        case 1:
            mappedSignatureAlgorithm += "MD5";
            break;
        case 2:
            mappedSignatureAlgorithm += "SHA1";
            break;
        case 3:
            mappedSignatureAlgorithm += "SHA224";
            break;
        case 4:
            mappedSignatureAlgorithm += "SHA256";
            break;
        case 5:
            mappedSignatureAlgorithm += "SHA384";
            break;
        case 6:
            mappedSignatureAlgorithm += "SHA512";
            break;
        default:
            throw std::invalid_argument("Signature Algorithm (\""
			+ std::to_string(signatureAlgorithm.first) + "," + std::to_string(signatureAlgorithm.second) + "\") not supported with OpenSSL");
    }
    return mappedSignatureAlgorithm;
}

const std::string TlsHelper::getInternalSignatureScheme(const TlsSignatureScheme signatureScheme) {
    
if (signatureScheme.first == 0x04 && signatureScheme.second == 0x01) {
        return "rsa_pkcs1_sha256";
    } else if (signatureScheme.first == 0x05 && signatureScheme.second == 0x01) {
        return "rsa_pkcs1_sha384";
    } else if (signatureScheme.first == 0x06 && signatureScheme.second == 0x01) {
        return "rsa_pkcs1_sha512";
    } else if (signatureScheme.first == 0x04 && signatureScheme.second == 0x03) {
        return "ecdsa_secp256r1_sha256";
    } else if (signatureScheme.first == 0x05 && signatureScheme.second == 0x03) {
        return "ecdsa_secp384r1_sha384";
    } else if (signatureScheme.first == 0x06 && signatureScheme.second == 0x03) {
        return "ecdsa_secp521r1_sha512";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x04) {
        return "rsa_pss_rsae_sha256";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x05) {
        return "rsa_pss_rsae_sha384";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x06) {
        return "rsa_pss_rsae_sha512";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x07) {
        return "ed25519";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x09) {
        return "rsa_pss_pss_sha256";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x0a) {
        return "rsa_pss_pss_sha384";
    } else if (signatureScheme.first == 0x08 && signatureScheme.second == 0x0b) {
        return "rsa_pss_pss_sha512";
    } else if (signatureScheme.first == 0x02 && signatureScheme.second == 0x01) {
        return "rsa_pkcs1_sha1";
    } else if (signatureScheme.first == 0x02 && signatureScheme.second == 0x03) {
        return "ecdsa_sha1";
    } else {
		throw std::invalid_argument("Signature Scheme \""
			+ Tooling::HexStringHelper::byteArrayToHexString({signatureScheme.first, signatureScheme.second}) + "\" not supported with OpenSSL");
	}
}

}
}
