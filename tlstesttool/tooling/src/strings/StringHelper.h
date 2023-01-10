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
#ifndef STRINGS_STRINGHELPER_H_
#define STRINGS_STRINGHELPER_H_

#include <algorithm>
#include <cstdio>
#include <sstream>
#include <string>
#include <vector>

namespace Tooling {
/**
 * Collection of helper functions that deal with strings.
 */
namespace StringHelper {
/**
 * Print a formatted string with one integer parameter. This is a wrapper for @link std::snprintf @endlink that creates
 * a string that holds the output.
 *
 * @param formatString See parameter @c format of @link std::snprintf @endlink
 * @param number Integer that will be given to @link std::snprintf @endlink
 * @return String containing the output of @link std::snprintf @endlink
 */
template <typename T> inline std::string formatInt(const std::string & formatString, const T number) {
	const auto requiredSize = std::snprintf(nullptr, 0, formatString.c_str(), number);
	std::vector<char> outputStr(requiredSize + 1);
	std::snprintf(&outputStr[0], outputStr.size(), formatString.c_str(), number);
	return {outputStr.data(), outputStr.size() - 1};
}

/**
 * Remove newline characters '\\r' and '\\n' from a string. Replace every '\\n' by a space character.
 *
 * @param str String that will be stripped of newlines
 * @return String without newlines
 */
inline std::string removeNewlines(std::string && str) {
	str.erase(std::remove(str.begin(), str.end(), '\r'), str.end());
	std::replace(str.begin(), str.end(), '\n', ' ');
	return str;
}

/**
 * Replace all occurrences of @p search in @p input by @p replacement.
 * @param input Input string that will be searched
 * @param search String that will be searched for
 * @param replacement String that will be used as replacement
 * @return Resulting string after the replacements. If nothing was replaced, it will be a copy of @p input.
 */
inline std::string replaceAll(const std::string & input, const std::string & search, const std::string & replacement) {
	std::string output(input);
	std::string::size_type pos = 0u;
	while ((pos = output.find(search, pos)) != std::string::npos) {
		output.replace(pos, search.length(), replacement);
		pos += replacement.length();
	}
	return output;
}

/**
 * Remove whitespace at the beginning and the end of a string.
 *
 * @param str String that will be trimmed
 * @return String with whitespace at beginning and end removed
 */
inline std::string trim(const std::string & str) {
	const std::string whiteSpace{" \t\v\f\r\n"};
	const auto startPos = str.find_first_not_of(whiteSpace);
	const auto endPos = str.find_last_not_of(whiteSpace);
	if ((std::string::npos == startPos) || (std::string::npos == endPos)) {
		return {};
	} else {
		return str.substr(startPos, endPos - startPos + 1);
	}
}

/**
 * Split a string based on a delimiter.
 *
 * @param s String input to split
 * @param delim Delimiter
 * @return A vector containing the tokens as strings
 */
inline std::vector<std::string> split(const std::string & s, char delim) {
	std::stringstream ss(s);
	std::vector<std::string> elems;
	std::string item;
	while (std::getline(ss, item, delim)) {
		elems.push_back(item);
	}
	return elems;
}

/**
 * Checks whether a string ends with the given ending.
 *
 * @params str The string to check
 * @params ending The ending to check
 * @return true if str ends with the ending, else false
 */
inline bool hasEnding(std::string const & str, std::string const & ending) {
	if (str.length() >= ending.length()) {
		return (0 == str.compare(str.length() - ending.length(), ending.length(), ending));
	} else {
		return false;
	}
}
}
}

#endif /* STRINGS_STRINGHELPER_H_ */
