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
#ifndef TLSTESTTOOL_TLSEXPORTSECRETSCONSTANT_H
#define TLSTESTTOOL_TLSEXPORTSECRETSCONSTANT_H

#include <cstddef>

namespace TlsTestTool {

    namespace TlsExportSecretsConstant {
        //TLS 1.2
        const char *const CLIENT_SECRET_LABEL = "CLIENT_RANDOM";

        //TLS 1.3
        const char *const CLIENT_HANDSHAKE_LABEL = "CLIENT_HANDSHAKE_TRAFFIC_SECRET";
        const char *const SERVER_HANDSHAKE_LABEL = "SERVER_HANDSHAKE_TRAFFIC_SECRET";
        const char *const CLIENT_APPLICATION_LABEL = "CLIENT_TRAFFIC_SECRET_0";
        const char *const SERVER_APPLICATION_LABEL = "SERVER_TRAFFIC_SECRET_0";
        const char *const EXPORTER_LABEL = "EXPORTER_SECRET";

        const size_t CLIENT_RANDOM_SIZE = 32;
        const size_t SECRET_SIZE_TLS_12 = 48;
        const size_t SECRET_SIZE_TLS_13 = 32;
    }

}
#endif /*TLSTESTTOOL_TLSEXPORTSECRETSCONSTANT_H*/
