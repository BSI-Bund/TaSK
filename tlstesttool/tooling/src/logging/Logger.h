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
#ifndef LOGGING_LOGGER_H_
#define LOGGING_LOGGER_H_

#include "logging/LogLevel.h"
#include <functional>
#include <iosfwd>
#include <memory>
#include <string>

namespace Tooling {
    class Logger;

    using LogFilter = std::function<void(Logger &logger, const LogLevel, const std::string &, const std::string &)>;

/**
 * Logger writing lines to an output stream.
 */
    class Logger {
    public:
        /**
         * Construct a logger and assign an output stream.
         * @param logOutput Output stream to which the logger will write
         */
        Logger(std::ostream &logOutput);

        /**
         * Destroy the logger and free resources.
         */
        ~Logger();

        /**
         * Configure the logger's verbosity. Log entries below
         * @param level Log level to set
         */
        void setLogLevel(const LogLevel level) {
            logLevel = level;
        }

        /**
         * Change the separator between the log columns. The default value is " ".
         * @param separator New separator
         */
        void setColumnSeparator(const std::string &separator) {
            columnSeparator = separator;
        }

        /**
         * Output a log entry.
         * @param level Log level defining the entry's severity.
         * @param origin Origin the entry.
         * @param message The entry's log message.
         */
        void log(const LogLevel level, const std::string &origin, const std::string &message);

        /**
         * Output a log entry. Convenience function that assembles the origin part of the log entry in the format
         * "category(location:line)".
         * @param level Log level defining the entry's severity.
         * @param category Additional category that will be shown.
         * @param file File that created the entry.
         * @param line Line in @p file that created the entry.
         * @param message The entry's log message.
         */
        void log(const LogLevel level, const std::string &category, const std::string &file, const int line,
                 const std::string &message);

        /**
         * Attach a callback function that is called when a log entry is written. The function can analyze the log entry and
         * create additional log entries using the reference to the logger.
         * @param filter Callback function that receives log entries
         */
        void addLogFilter(LogFilter &&filter);

        const std::pair<uint8_t, uint8_t> &getTlsVersion() const;

        void setTlsVersion(const std::pair<uint8_t, uint8_t> &p_tlsVersion);


    private:
        struct Data;
        std::unique_ptr<Data> impl;
        LogLevel logLevel;
        std::string columnSeparator;
        std::pair<uint8_t, uint8_t> tlsVersion;
    };
}

#endif /* LOGGING_LOGGER_H_ */
