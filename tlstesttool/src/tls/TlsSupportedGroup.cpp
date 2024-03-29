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
#include "TlsSupportedGroup.h"
#include <stdexcept>

namespace TlsTestTool {
//This method returns Suported groups.
    TlsSupportedGroupID TlsSupportedGroup::getPredefined(const std::string &key) {

        if ("sect163k1" == key) {
            return 1;
        } else if ("sect163r1" == key) {
            return 2;
        } else if ("sect163r2" == key) {
            return 3;
        } else if ("sect193r1" == key) {
            return 4;
        } else if ("sect193r2" == key) {
            return 5;
        } else if ("sect233k1" == key) {
            return 6;
        } else if ("sect233r1" == key) {
            return 7;
        } else if ("sect239k1" == key) {
            return 8;
        } else if ("sect283k1" == key) {
            return 9;
        } else if ("sect283r1" == key) {
            return 10;
        } else if ("sect409k1" == key) {
            return 11;
        } else if ("sect409r1" == key) {
            return 12;
        } else if ("sect571k1" == key) {
            return 13;
        } else if ("sect571r1" == key) {
            return 14;
        } else if ("secp160k1" == key) {
            return 15;
        } else if ("secp160r1" == key) {
            return 16;
        } else if ("secp160r2" == key) {
            return 17;
        } else if ("secp192k1" == key) {
            return 18;
        } else if ("secp192r1" == key) {
            return 19;
        } else if ("secp224k1" == key) {
            return 20;
        } else if ("secp224r1" == key) {
            return 21;
        } else if ("secp256k1" == key) {
            return 22;
        } else if ("secp256r1" == key || "P-256" == key) { //secp256r1
            return 23;
        } else if ("secp384r1" == key || "P-384" == key) { //secp384r1
            return 24;
        } else if ("secp521r1" == key || "P-521" == key) { //secp521r1
            return 25;
        } else if ("brainpoolP256r1" == key) {
            return 26;
        } else if ("brainpoolP384r1" == key) {
            return 27;
        } else if ("brainpoolP512r1" == key) {
            return 28;
        } else if ("x25519" == key || "X25519" == key) {
            return 29;
        } else if ("x448" == key || "X448" == key) {
            return 30;
        } else if ("brainpoolP256r1tls13" == key) {
            return 31;
        } else if ("brainpoolP384r1tls13" == key) {
            return 32;
        } else if ("brainpoolP512r1tls13" == key) {
            return 33;
        } else if ("ffdhe2048" == key) {
            return 256;
        } else if ("ffdhe3072" == key) {
            return 257;
        } else if ("ffdhe4096" == key) {
            return 258;
        } else if ("ffdhe6144" == key) {
            return 259;
        } else if ("ffdhe8192" == key) {
            return 260;
        } else if ("arbitrary_explicit_prime_curves" == key
                   || "arbitrary_explicit_char2_curves" == key) {
            throw std::invalid_argument("Unsupported group \"" + key + "\"");
        } else {
            throw std::invalid_argument("Unknown group \"" + key + "\"");
        }
    }
}
