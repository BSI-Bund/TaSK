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
#ifndef MANIPULATION_MANIPULATION_H_
#define MANIPULATION_MANIPULATION_H_

#include <memory>
#include <string>

namespace Tooling {
    class Logger;
}
namespace TlsTestTool {
    class TlsSession;

/**
 * Abstract base class of all manipulations.
 */
    class Manipulation {
    public:
        virtual ~Manipulation();

        /**
         * Perform a manipulation on the given TLS session before starting the TLS handshake.
         * @param session TLS session that probably will be manipulated.
         */
        virtual void executePreHandshake(TlsSession &session) = 0;

        /**
         * Perform a manipulation on the given TLS session before the execution of a TLS handshake step.
         * @param session TLS session that probably will be manipulated.
         */
        virtual void executePreStep(TlsSession &session) = 0;

        /**
         * Perform a manipulation on the given TLS session after the execution of a TLS handshake step.
         * @param session TLS session that probably will be manipulated.
         */
        virtual void executePostStep(TlsSession &session) = 0;

        /**
         * Perform a manipulation on the given TLS session after finishing the TLS handshake.
         * @param session TLS session that probably will be manipulated.
         */
        virtual void executePostHandshake(TlsSession &session) = 0;

        /**
         * Attach a logger that will be used for log output.
         * @param logger Log that will receive log entries.
         */
        void setLogger(Tooling::Logger &logger);

    protected:
        Manipulation();

        /**
         * If a logger is available, write a message to the log.
         * @param file File that creates the log entry.
         * @param line Line in @p file that creates the log entry.
         * @param message Log message that will be written.
         * @see setLogger()
         */
        void log(const std::string &file, const int line, const std::string &message);

    private:
        class Data;

        //! Use pimpl idiom.
        std::unique_ptr<Data> impl;
    };
}

#endif /* MANIPULATION_MANIPULATION_H_ */
