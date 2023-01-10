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
#include "Logger.h"
#include <chrono>
#include <ctime>
#include <iomanip>
#include <iostream>
#include <vector>

/**
 * mingw32 doesn't yet fully support C++11 threads.
 * This header[1] is a workaround that basically
 * encapsules pthreads for use with native Win32 threads.
 *
 * [1] https://github.com/meganz/mingw-std-threads
 */
#if defined(__MINGW32__) and not defined(_GLIBCXX_HAS_GTHREADS)
#include <mingw.mutex.h>
#include <mingw.thread.h>
#else
#include <mutex>
#include <thread>
#endif

namespace Tooling {
static std::string levelToString(const LogLevel level) {
	switch (level) {
		case LogLevel::HIGH:
			return "HIGH";
		case LogLevel::MEDIUM:
			return "MEDIUM";
		case LogLevel::LOW:
			return "LOW";
		case LogLevel::OFF:
		default:
			return "OFF";
	}
}

static void logTimestamp(std::ostream & stream) {
	char timeString[20];
	const auto now = std::chrono::system_clock::now();
	const auto currentTime = std::chrono::system_clock::to_time_t(now);
	const auto len = std::strftime(timeString, sizeof(timeString), "%Y-%m-%dT%H:%M:%S", std::gmtime(&currentTime));
	const auto convertedNow = std::chrono::system_clock::from_time_t(currentTime);
	const auto milliseconds = std::chrono::duration_cast<std::chrono::milliseconds>(now - convertedNow).count();
	stream << std::string{timeString, len} << '.' << std::setfill('0') << std::setw(3) << milliseconds << 'Z';
}

struct Logger::Data {
	std::recursive_mutex dataMutex;
	std::ostream & output;
	std::vector<LogFilter> logFilters{};
	bool processingFilters{false};

	Data(std::ostream & logOutput) : output(logOutput) {
	}
};

Logger::Logger(std::ostream & logOutput) : impl(new Data(logOutput)), logLevel{LogLevel::OFF}, columnSeparator{" "} {
}

Logger::~Logger() = default;

void Logger::log(const LogLevel level, const std::string & origin, const std::string & message) {
	std::lock_guard<std::recursive_mutex> lock(impl->dataMutex);
	if ((static_cast<int>(LogLevel::HIGH) - static_cast<int>(level)) < static_cast<int>(logLevel)) {
		logTimestamp(impl->output);
		impl->output << columnSeparator;
		impl->output << levelToString(level);
		impl->output << columnSeparator;
		impl->output << origin;
		impl->output << columnSeparator;
		impl->output << message;
		if ('\n' != message.back()) {
			impl->output << '\n';
		}
	}
	if (!impl->processingFilters) {
		impl->processingFilters = true;
		for (const auto & logFilter : impl->logFilters) {
			logFilter(*this, level, origin, message);
		}
		impl->processingFilters = false;
	}
}

void Logger::log(const LogLevel level, const std::string & category, const std::string & file, const int line,
				 const std::string & message) {
	// Extract basename from file
	const std::string fileName{file.substr(file.find_last_of("/\\") + 1)};
	const std::string origin{category + '(' + fileName + ':' + std::to_string(line) + ')'};
	log(level, origin, message);
}

void Logger::addLogFilter(LogFilter && filter) {
	std::lock_guard<std::recursive_mutex> lock(impl->dataMutex);
	impl->logFilters.emplace_back(std::move(filter));
}

const std::pair<uint8_t, uint8_t> &Logger::getTlsVersion() const {
    return tlsVersion;
}

void Logger::setTlsVersion(const std::pair<uint8_t, uint8_t> &p_tlsVersion) {
    Logger::tlsVersion = p_tlsVersion;
}

}
