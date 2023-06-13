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
#include "HexStringHelper.h"
#include "strings/StringHelper.h"
#include <cstdint>
#include <cstdlib>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

namespace Tooling {
    namespace HexStringHelper {

        std::string byteArrayToHexString(const std::vector<uint8_t> &byteArray) {
            std::stringstream stream;
            stream << std::setfill('0') << std::hex;
            for (uint_fast16_t byte: byteArray) {
                stream << std::setw(2) << byte << ' ';
            }
            return stream.str();
        }

        std::vector<uint8_t> hexStringToByteArray(const std::string &hexString) {
            const auto trimmedHexString = StringHelper::trim(hexString);
            if (trimmedHexString.empty()) {
                return {};
            }
            std::vector<uint8_t> byteArray;
            byteArray.reserve(trimmedHexString.size() / 3);
            char *pos = const_cast<char *>(trimmedHexString.c_str());
            char *oldPos = nullptr;
            while ((oldPos != pos) && ('\0' != *pos)) {
                oldPos = pos;
                byteArray.emplace_back(std::strtoul(pos, &pos, 16));
            }
            return byteArray;
        }
    }
}
