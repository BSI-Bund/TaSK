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
#ifndef TLS_TLSSUPPORTEDGROUP_H_
#define TLS_TLSSUPPORTEDGROUP_H_

#include <cstdint>
#include <string>

namespace TlsTestTool {
using TlsSupportedGroupID = uint16_t;
/**
 * Parameters of a supported group used in TLS 1.3.
 * The client has indicated that it supports elliptic curve (EC) cryptography for three curve types.
 * To make this extension more generic for other cryptography types it now calls these "supported groups" instead of "supported curves".
 */
class TlsSupportedGroup {
public:
	/**
	 * Get a predefined supported group.
	 *
	 * @param key A key identifying the group. It can be one of
	 * @li "secp192r1": RFC 8422
     * @li "secp224r1": RFC 8422
     * @li "secp256r1": RFC 8422
     * @li "secp384r1": RFC 8422
     * @li "secp521r1": RFC 8422
	 * @li "x25519": RFC 8446/RFC 8422
     * @li "ffdhe2048": RFC 7919
     * @li "ffdhe3072": RFC 7919
     * @li "ffdhe4096": RFC 7919
     * @li "ffdhe6144": RFC 7919
     * @li "ffdhe8192": RFC 7919
	 * @return supported group
	 * @throw std::invalid_argument Thrown, if an unknown key is given
	 */
    static TlsSupportedGroupID getPredefined(const std::string & key);
};
}

#endif /* TLS_TLSSUPPORTEDGROUP_H_ */
