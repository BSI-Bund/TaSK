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
#ifndef TLS_MBEDTLS_TLSLOGFILTER_H_
#define TLS_MBEDTLS_TLSLOGFILTER_H_

namespace Tooling {
    class Logger;
}
namespace TlsTestTool {
    namespace MbedTls {
/**
 * Implementation of TlsLogFilter for translating mbed TLS specific log messages to general log messages.
 */
        class TlsLogFilter {
        public:
            /**
             * Factory function for creating an instance for the currently used TLS library.
             */
            static void registerInstances(Tooling::Logger &logger);
        };
    }
}

#endif /* TLS_MBEDTLS_TLSLOGFILTER_H_ */
