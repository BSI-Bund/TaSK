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
#ifndef MANIPULATEELLIPTICCURVEGROUP_H
#define MANIPULATEELLIPTICCURVEGROUP_H

#include "Manipulation.h"
#include "tls/TlsEllipticCurveGroup.h"

namespace TlsTestTool {
/**
 * Manipulate the EllipticCurveGroup used in a ServerKeyExchange message.
 */
    class ManipulateEllipticCurveGroup : public Manipulation {
    public:
        /**
         * Create a manipulation.
         *
         * @param newManipulatedEllipticCurveGroup Integer defining an elliptic curve group
         */
        ManipulateEllipticCurveGroup(TlsEllipticCurveGroupID newManipulatedEllipticCurveGroup) : Manipulation(),
                                                                                                 manipulatedEllipticCurveGroup(
                                                                                                         newManipulatedEllipticCurveGroup) {
        }

        TlsEllipticCurveGroupID getManipulatedEllipticCurveGroup() const {
            return manipulatedEllipticCurveGroup;
        }

        virtual void executePreHandshake(TlsSession &session) override;

        virtual void executePreStep(TlsSession &session) override;

        virtual void executePostStep(TlsSession &session) override;

        virtual void executePostHandshake(TlsSession &session) override;

    private:
        const TlsEllipticCurveGroupID manipulatedEllipticCurveGroup;
    };
}

#endif // MANIPULATEELLIPTICCURVEGROUP_H
