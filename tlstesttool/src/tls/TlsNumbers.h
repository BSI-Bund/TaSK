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
#ifndef TLS_TLSNUMBERS_H_
#define TLS_TLSNUMBERS_H_

#include <array>
#include <cstdint>

namespace TlsTestTool {
    using TlsUint8 = uint8_t;

/**
 * Type for storing an unsigned 16 bit number.
 *
 * @see RFC 5246, Section 4.4
 */
    class TlsUint16 {
    private:
        std::array<uint8_t, 2> value;

    public:
        constexpr TlsUint16(TlsUint8 firstByte, TlsUint8 secondByte) : value{firstByte, secondByte} {
        }

        constexpr uint16_t get() const {
            return (value[0] << 8) | value[1];
        }

        void set(const uint16_t newValue) {
            value[0] = (newValue >> 8) & 0xFF;
            value[1] = newValue & 0xFF;
        }
    };

/**
 * Type for storing an unsigned 24 bit number.
 *
 * @see RFC 5246, Section 4.4
 */
    class TlsUint24 {
    private:
        const std::array<uint8_t, 3> value;

    public:
        constexpr TlsUint24(TlsUint8 firstByte, TlsUint8 secondByte, TlsUint8 thirdByte)
                : value{firstByte, secondByte, thirdByte} {
        }

        constexpr uint32_t get() const {
            return (value[0] << 16) | (value[1] << 8) | value[2];
        }
    };

/**
 * Type for storing an unsigned 32 bit number.
 *
 * @see RFC 5246, Section 4.4
 */
    class TlsUint32 {
    private:
        const std::array<uint8_t, 4> value;

    public:
        constexpr TlsUint32(TlsUint8 firstByte, TlsUint8 secondByte, TlsUint8 thirdByte, TlsUint8 fourthByte)
                : value{firstByte, secondByte, thirdByte, fourthByte} {
        }

        constexpr uint32_t get() const {
            return (value[0] << 24) | (value[1] << 16) | (value[2] << 8) | value[3];
        }
    };

/**
 * Type for storing an unsigned 64 bit number.
 *
 * @see RFC 5246, Section 4.4
 */
    class TlsUint64 {
    private:
        const std::array<uint8_t, 8> value;

    public:
        constexpr TlsUint64(TlsUint8 firstByte, TlsUint8 secondByte, TlsUint8 thirdByte, TlsUint8 fourthByte,
                            TlsUint8 fifthByte, TlsUint8 sixthByte, TlsUint8 seventhByte, TlsUint8 eighthByte)
                : value{firstByte, secondByte, thirdByte, fourthByte, fifthByte, sixthByte, seventhByte, eighthByte} {
        }

        constexpr uint64_t get() const {
            return (static_cast<uint64_t>(value[0]) << 56) | (static_cast<uint64_t>(value[1]) << 48)
                   | (static_cast<uint64_t>(value[2]) << 40) | (static_cast<uint64_t>(value[3]) << 32) |
                   (value[4] << 24)
                   | (value[5] << 16) | (value[6] << 8) | value[7];
        }
    };
}

#endif /* TLS_TLSNUMBERS_H_ */
