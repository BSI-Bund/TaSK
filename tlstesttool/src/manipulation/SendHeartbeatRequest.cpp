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
#include "SendHeartbeatRequest.h"
#include "network/TcpClient.h"
#include "tls/TlsHandshakeState.h"
#include "tls/TlsPlaintextHeader.h"
#include "tls/TlsHeartbeatMessageHeader.h"
#include "tls/TlsSession.h"
#include "strings/HexStringHelper.h"
#include <algorithm>
#include <cstdint>
#include <vector>

namespace TlsTestTool {

    void SendHeartbeatRequest::executePreHandshake(TlsSession &session) {
        if (when == "beforeHandshake") {
            send(session);
        }
    }

    void SendHeartbeatRequest::executePreStep(TlsSession & /*session*/) {
    }

    void SendHeartbeatRequest::executePostStep(TlsSession & /*session*/) {
    }

    void SendHeartbeatRequest::executePostHandshake(TlsSession &session) {
        if (when == "afterHandshake") {
            send(session);
        }
    }

    void SendHeartbeatRequest::send(TlsSession &session) {
        SendHeartbeatRequest::Manipulation::log(__FILE__, __LINE__, "Sending HeartbeatRequest message...");
        const std::vector<uint8_t> padding(16, 0xab);
        std::vector<char> message(
                sizeof(TlsPlaintextHeader) + sizeof(HeartbeatMessageHeader) + payload.size() + padding.size(), 0);

        auto *recordHeader = reinterpret_cast<TlsPlaintextHeader *>(message.data());
        recordHeader->type = TlsContentType::HEARTBEAT;
        recordHeader->version = std::make_pair(0x03, 0x03); //session.getVersion();
        recordHeader->length.set(sizeof(HeartbeatMessageHeader) + payload.size() + padding.size());

        auto *heartbeatHeader = reinterpret_cast<HeartbeatMessageHeader *>(message.data() + sizeof(TlsPlaintextHeader));
        // heartbeat_request(1)
        // http://tools.ietf.org/html/rfc6520#section-3
        heartbeatHeader->type = 1;
        heartbeatHeader->payload_length.set(payloadLength);

        std::copy(
                payload.cbegin(), payload.cend(),
                std::next(message.begin(), sizeof(TlsPlaintextHeader) + sizeof(HeartbeatMessageHeader)));

        std::copy(
                padding.cbegin(), padding.cend(),
                std::next(message.begin(),
                          sizeof(TlsPlaintextHeader) + sizeof(HeartbeatMessageHeader) + payload.size()));

        std::vector<uint8_t> bytes;
        for (char &c: message) {
            bytes.push_back(static_cast<std::uint8_t>(c));
        }
        SendHeartbeatRequest::Manipulation::log(__FILE__, __LINE__, "Message content: " +
                                                                    Tooling::HexStringHelper::byteArrayToHexString(
                                                                            bytes));


        if (session.getState() == TlsTestTool::TlsHandshakeState::HANDSHAKE_DONE) {
            std::vector<uint8_t> finalMessage;
            finalMessage.insert(finalMessage.end(), bytes.begin() + sizeof(TlsPlaintextHeader), bytes.end());
            session.sendRecord(static_cast<int>(TlsContentType::HEARTBEAT), finalMessage);
        } else {
            session.getSocket()->write(message);
        }
    }
} // namespace TlsTestTool
