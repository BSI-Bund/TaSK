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
#ifndef NETWORK_ABSTRACTSOCKETOBSERVER_H_
#define NETWORK_ABSTRACTSOCKETOBSERVER_H_

#include <cstdlib>

namespace TlsTestTool {
/**
 * Abstract base class for socket observers.
 */
    class AbstractSocketObserver {
    public:
        /**
         * Virtual destructor.
         */
        virtual ~AbstractSocketObserver() = default;

        /**
     * Called when the subject has written a block of characters successfully.
     *
     * @param length Number of bytes written
         */
        virtual void onBlockWritten(std::size_t length) noexcept = 0;

        /**
     * Called when the subject has read a block of characters successfully.
         *
     * @param length Number of bytes to read
         */
        virtual void onBlockRead(std::size_t length) noexcept = 0;
    };
}

#endif /* NETWORK_ABSTRACTSOCKETOBSERVER_H_ */
