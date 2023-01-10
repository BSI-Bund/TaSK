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
#ifndef TLS_TLSVERSION_H_
#define TLS_TLSVERSION_H_

#include <cstdint>
#include <utility>

namespace TlsTestTool {
using TlsVersion = std::pair<uint8_t, uint8_t>;

const TlsVersion TLS_VERSION_INVALID(0, 0);
const TlsVersion TLS_VERSION_SSL_3_0(3, 0);
const TlsVersion TLS_VERSION_TLS_1_0(3, 1);
const TlsVersion TLS_VERSION_TLS_1_1(3, 2);
const TlsVersion TLS_VERSION_TLS_1_2(3, 3);
const TlsVersion TLS_VERSION_TLS_1_3(3, 4);
}

#endif /* TLS_TLSVERSION_H_ */
