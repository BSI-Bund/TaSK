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
#ifndef TLS_OPENSSLTLSHELPER_H_
#define TLS_OPENSSLTLSHELPER_H_

#include "tls/TlsCipherSuite.h"
#include "tls/TlsSupportedGroup.h"
#include "tls/TlsSession.h"
#include "tls/TlsSignatureAndHashAlgorithm.h"
#include <cstdint>
#include <string>

#define OPENSSL_CIPHER_SUITES_ALL "ALL"
#define OPENSSL_EC_CURVES_ALL "sect163k1:"\
                                "sect163r1:"\
                                "sect163r2:"\
                                "sect193r1:"\
                                "sect193r2:"\
                                "sect233k1:"\
                                "sect233r1:"\
                                "sect239k1:"\
                                "sect283k1:"\
                                "sect283r1:"\
                                "sect409k1:"\
                                "sect409r1:"\
                                "sect571k1:"\
                                "sect571r1:"\
                                "secp160k1:"\
                                "secp160r1:"\
                                "secp160r2:"\
                                "secp192k1:"\
                                "secp224k1:"\
                                "secp224r1:"\
                                "secp256k1:"\
                                "secp384r1:"\
                                "secp521r1:"\
                                "brainpoolP256r1:"\
                                "brainpoolP384r1:"\
                                "brainpoolP512r1"

#define OPENSSL_SUPPORTEDGROUPS_ALL "sect163k1:"\
                                "sect163r1:"\
                                "sect163r2:"\
                                "sect193r1:"\
                                "sect193r2:"\
                                "sect233k1:"\
                                "sect233r1:"\
                                "sect239k1:"\
                                "sect283k1:"\
                                "sect283r1:"\
                                "sect409k1:"\
                                "sect409r1:"\
                                "sect571k1:"\
                                "sect571r1:"\
                                "secp160k1:"\
                                "secp160r1:"\
                                "secp160r2:"\
                                "secp192k1:"\
                                "secp224k1:"\
                                "secp224r1:"\
                                "secp256k1:"\
                                "secp384r1:"\
                                "secp521r1:"\
                                "brainpoolP256r1:"\
                                "brainpoolP384r1:"\
                                "brainpoolP512r1"\
                                "P-256:"\
                                "P-384:"\
                                "P-521:"\
                                "X25519:"\
                                "X448:"\
                                "ffdhe2048:"\
                                "ffdhe3072:"\
                                "ffdhe4096:"\
                                "ffdhe6144:"\
                                "ffdhe8192"

namespace TlsTestTool {
    namespace OpenSsl {
/**
 * Parameters of OpenSSL in internal representation.
 */
        class TlsHelper {
        public:
            /**
             * Get a cipher suite string in internal OpenSSL representation.
             *
             * @param cipherSuite A pair identifying the cipher suite.
             * @return internal cipher suite string
             * @throw std::invalid_argument Thrown, if an unknown pair is given
             */
            static const std::string getInternalCipherSuite(const TlsCipherSuite cipherSuite);

            /**
                 * Get a elliptic curve string in internal OpenSSL representation.
                 *
                 * @param supportedGroup A ID identifying the supported group.
                 * @return internal elliptic curve string
                 * @throw std::invalid_argument Thrown, if an unknown ID is given
                 */
            static const std::string getInternalEllipticCurve(const TlsSupportedGroupID eccCurveID);

            static const std::string getInternalSupportedGroup(const TlsSupportedGroupID supportedGroupID);

            static const std::string
            getInternalSignatureAlgorithm(const TlsSignatureAndHashAlgorithm signatureAlgorithm);

            static const std::string getInternalSignatureScheme(const TlsSignatureScheme signatureScheme);
        };
    }
}

#endif /* TLS_OPENSSLTLSHELPER_H_ */
