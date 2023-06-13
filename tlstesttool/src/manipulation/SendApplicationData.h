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
#ifndef MANIPULATION_SENDAPPLICATIONDATA_H_
#define MANIPULATION_SENDAPPLICATIONDATA_H_

#include "Manipulation.h"
#include <cstdint>
#include <vector>

namespace TlsTestTool {
/**
 * Send desired TLS application data after finishing a TLS Handshake. Additionally, the function allows to repeat the
 * send process @c n times.
 * @see RFC 5246, Appendix A.3 (https://tools.ietf.org/html/rfc5246#page-69)
 */
    class SendApplicationData : public Manipulation {
    public:
        /**
         * Create a manipulation.
         *
         * @param newNumberSendData	 Integer specifying how many times the specified message will be send after a TLS
         * handshake was finished successfully.
         * @param newApplicationData Application data that will be sent.
         */
        SendApplicationData(const uint64_t newNumberSendData, const std::vector<uint8_t> newApplicationData)
                : Manipulation(), numberSendData(newNumberSendData), applicationData(newApplicationData) {
        }

        const std::vector<uint8_t> &getTlsApplicationData() const {
            return applicationData;
        }

        uint64_t getNumberSendData() const {
            return numberSendData;
        }

        virtual void executePreHandshake(TlsSession &session) override;

        virtual void executePreStep(TlsSession &session) override;

        virtual void executePostStep(TlsSession &session) override;

        virtual void executePostHandshake(TlsSession &session) override;

    private:
        const uint64_t numberSendData;
        const std::vector<uint8_t> applicationData;
    };
}

#endif /* MANIPULATION_SENDAPPLICATIONDATA_H_ */
