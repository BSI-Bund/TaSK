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
#ifndef MANIPULATION_RENEGOTIATE_H_
#define MANIPULATION_RENEGOTIATE_H_

#include "Manipulation.h"

namespace TlsTestTool {
/**
 * Perform a renegotiation after finishing a TLS Handshake.
 * @see https://tools.ietf.org/html/rfc5246#section-7.4.1.1
 */
class Renegotiate : public Manipulation {
public:
	virtual void executePreHandshake(TlsSession & session) override;
	virtual void executePreStep(TlsSession & session) override;
	virtual void executePostStep(TlsSession & session) override;
	virtual void executePostHandshake(TlsSession & session) override;
};
}

#endif /* MANIPULATION_RENEGOTIATE_H_ */
