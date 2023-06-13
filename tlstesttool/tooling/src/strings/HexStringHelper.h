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
#ifndef STRINGS_HEXSTRINGHELPER_H_
#define STRINGS_HEXSTRINGHELPER_H_

#include <cstdint>
#include <string>
#include <vector>

namespace Tooling {
/**
 * Collection of helper functions that deal with strings of hexadecimal encoded bytes.
 */
    namespace HexStringHelper {
/**
 * Convert an array of bytes to a string containing hexadecimal values separated by space.
 *
 * @param byteArray Array of bytes
 * @return String containing hexdecimal values
 */
        std::string byteArrayToHexString(const std::vector<uint8_t> &byteArray);

/**
 * Convert a string containing hexadecimal values separated by space to an array of bytes.
 *
 * @param hexString String containing hexdecimal values
 * @return Array of bytes
 */
        std::vector<uint8_t> hexStringToByteArray(const std::string &hexString);
    }
}

#endif /* STRINGS_HEXSTRINGHELPER_H_ */
