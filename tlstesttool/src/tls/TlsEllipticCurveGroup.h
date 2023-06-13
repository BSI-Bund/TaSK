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
#ifndef TLS_TLSELLIPTICCURVEGROUP_H_
#define TLS_TLSELLIPTICCURVEGROUP_H_

#include <cstdint>
#include <string>

namespace TlsTestTool {
    using TlsEllipticCurveGroupID = uint16_t;

/**
 * Parameters of a elliptic curve group.
 */
    class TlsEllipticCurveGroup {
    public:
        /**
         * Get a predefined elliptic curve group.
         *
         * @param key A key identifying the group. It can be one of
     * @li "secp192k1": RFC 4492
     * @li "secp192r1": RFC 4492
     * @li "secp224k1": RFC 4492
     * @li "secp224r1": RFC 4492
     * @li "secp256k1": RFC 4492
     * @li "secp256r1": RFC 4492
     * @li "secp384r1": RFC 4492
     * @li "secp521r1": RFC 4492
         * @li "brainpoolP256r1": RFC 7027
         * @li "brainpoolP384r1": RFC 7027
         * @li "brainpoolP512r1": RFC 7027
         * @return elliptic curve group
         * @throw std::invalid_argument Thrown, if an unknown key is given
         */
        static TlsEllipticCurveGroupID getPredefined(const std::string &key);
    };
}

#endif /* TLS_TLSELLIPTICCURVEGROUP_H_ */
