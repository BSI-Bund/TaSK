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
#ifndef MANIPULATION_FORCECERTIFICATEUSAGE_H_
#define MANIPULATION_FORCECERTIFICATEUSAGE_H_

#include "Manipulation.h"

namespace TlsTestTool {
/**
 * When picking a certificate to send and no match is found (e.g., wrong key usage), send the first configured
 * certificate instead of failing the handshake. This can be used to force sending of invalid certificates.
 *
 * @author Benjamin Eikel <benjamin.eikel@achelos.de>
 * @date 2016-09-23
 */
    class ForceCertificateUsage : public Manipulation {
    public:
        virtual void executePreHandshake(TlsSession &session) override;

        virtual void executePreStep(TlsSession &session) override;

        virtual void executePostStep(TlsSession &session) override;

        virtual void executePostHandshake(TlsSession &session) override;
    };
}

#endif /* MANIPULATION_FORCECERTIFICATEUSAGE_H_ */
