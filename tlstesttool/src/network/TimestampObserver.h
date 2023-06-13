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
#ifndef NETWORK_TIMESTAMPOBSERVER_H_
#define NETWORK_TIMESTAMPOBSERVER_H_

#include "TcpConnection.h"
#include "AbstractSocketObserver.h"
#include <cstdlib>
#include <memory>

namespace Tooling {
    class Logger;
}
namespace TlsTestTool {
    class TcpClient;

/**
 * Socket observer that prints timestamps for blocks that are received or sent.
 */
    class TimestampObserver : public AbstractSocketObserver {
    public:
        /**
         * Create an observer.
         * @param socket Socket that will be configured to produce timestamps, if supported.
         * @param logger Logger used for printing the timestamps.
         */
        TimestampObserver(std::shared_ptr<TcpConnection> connection, Tooling::Logger &logger);

        /**
         * Virtual destructor.
         */
        virtual ~TimestampObserver();

        virtual void onBlockWritten(std::size_t length) noexcept override;

        virtual void onBlockRead(std::size_t length) noexcept override;

    private:
        class Data;

        //! Use pimpl idiom.
        std::unique_ptr<Data> impl;
    };
}

#endif /* NETWORK_TIMESTAMPOBSERVER_H_ */
