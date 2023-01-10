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
#include "ManipulateEllipticCurveGroup.h"
#include "tls/TlsSession.h"
#include <string>

namespace TlsTestTool {
void ManipulateEllipticCurveGroup::executePreHandshake(TlsSession & /*session*/) {
}

void ManipulateEllipticCurveGroup::executePreStep(TlsSession & session) {
    if (!session.isClient() && (TlsHandshakeState::SERVER_KEY_EXCHANGE == session.getState())) {
        log(__FILE__, __LINE__, "Setting EllipticCurveGroup to " + std::to_string(manipulatedEllipticCurveGroup)
                    + " before sending ServerKeyExchange.");
        session.overwriteEllipticCurveGroup(manipulatedEllipticCurveGroup);
    }
}

void ManipulateEllipticCurveGroup::executePostStep(TlsSession & /*session*/) {
}

void ManipulateEllipticCurveGroup::executePostHandshake(TlsSession & /*session*/) {
}
}
