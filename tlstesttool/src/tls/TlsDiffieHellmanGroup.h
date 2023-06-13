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
#ifndef TLS_TLSDIFFIEHELLMANGROUP_H_
#define TLS_TLSDIFFIEHELLMANGROUP_H_

#include <string>

namespace TlsTestTool {
/**
 * Parameters of a Diffie-Hellman group.
 */
    class TlsDiffieHellmanGroup {
    public:
        /**
         * Get a predefined Diffie-Hellman group.
         *
         * @param key A key identifying the group. It can be one of
         * @li "rfc3526_1536": RFC 3526 - 1536-bit MODP Group
         * @li "rfc3526_2048": RFC 3526 - 2048-bit MODP Group
         * @li "rfc3526_3072": RFC 3526 - 3072-bit MODP Group
         * @li "rfc3526_4096": RFC 3526 - 4096-bit MODP Group
         * @li "rfc3526_6144": RFC 3526 - 6144-bit MODP Group
         * @li "rfc3526_8192": RFC 3526 - 8192-bit MODP Group
         * @li "rfc5114_1024_160": RFC 5114 - 1024-bit MODP Group with 160-bit Prime Order Subgroup
         * @li "rfc5114_2048_224": RFC 5114 - 2048-bit MODP Group with 224-bit Prime Order Subgroup
         * @li "rfc5114_2048_256": RFC 5114 - 2048-bit MODP Group with 256-bit Prime Order Subgroup
         * @return Diffie-Hellman group
         * @throw std::invalid_argument Thrown, if an unknown key is given
         */
        static const TlsDiffieHellmanGroup &getPredefined(const std::string &key);

        /**
         * Create a Diffie-Hellman group by giving its parameters.
         *
         * @param prime The prime as hexadecimal string
         * @param generator The generator as hexadecimal string
         */
        TlsDiffieHellmanGroup(const char *const p, const char *const g);

        /**
         * Get the group's prime.
         * @return Prime as null-terminated, hexadecimal string
         */
        const char *getPrime() const {
            return prime;
        }

        /**
         * Get the group's generator.
         * @return Generator as null-terminated, hexadecimal string
         */
        const char *getGenerator() const {
            return generator;
        }

    private:
        const char *const prime;
        const char *const generator;
    };
}

#endif /* TLS_TLSDIFFIEHELLMANGROUP_H_ */
