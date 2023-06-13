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
#ifndef MANIPULATION_SENDHEARTBEATREQUEST_H_
#define MANIPULATION_SENDHEARTBEATREQUEST_H_

#include "Manipulation.h"
#include <vector>

namespace TlsTestTool {
/**
 * Send a HeartbeatRequest message before or after performing a TLS handshake.
 * @see http://tools.ietf.org/html/rfc6520#section-4
 */
    class SendHeartbeatRequest : public Manipulation {
    public:
        /**
         * Create a manipulation.
         *
         * @param procedureWhen Either "beforeHandshake" or "afterHandshake"
         * @param newPayloadLength	 Integer specifying the payload_length of Heartbeat Request.
         * @param newPayload Payload that will be sent.
         */
        SendHeartbeatRequest(const std::string &newWhen, const uint16_t newPayloadLength,
                             const std::vector<uint8_t> newPayload)
                : Manipulation(), when(newWhen), payloadLength(newPayloadLength), payload(newPayload) {
        }

        const std::string &getWhen() const {
            return when;
        }

        const std::vector<uint8_t> &getPayload() const {
            return payload;
        }

        uint16_t getPayloadLength() const {
            return payloadLength;
        }

        virtual void executePreHandshake(TlsSession &session) override;

        virtual void executePreStep(TlsSession &session) override;

        virtual void executePostStep(TlsSession &session) override;

        virtual void executePostHandshake(TlsSession &session) override;

    private:
        const std::string when;
        const uint16_t payloadLength;
        const std::vector<uint8_t> payload;

        void send(TlsSession &session);
    };
} // namespace TlsTestTool

#endif /* MANIPULATION_SENDHEARTBEATREQUEST_H_ */
