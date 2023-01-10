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
#ifndef MANIPULATION_MANIPULATESERVEHELLOCOMPRESSIONMETHODS_H_
#define MANIPULATION_MANIPULATESERVEHELLOCOMPRESSIONMETHODS_H_

#include "Manipulation.h"
#include <vector>

namespace TlsTestTool {
/**
* Deactivate ServerHello Compression.
*/
class ManipulateServerHelloCompressionMethods : public Manipulation {
public:
	/**
	 * Create a manipulation.
	 *
	 * @param newCompressionMethods Value to set the compression_methods field to.
	 */
	ManipulateServerHelloCompressionMethods(const std::vector<uint8_t> & newCompressionMethods)
			: Manipulation(), compressionMethods(newCompressionMethods) {
	}

   virtual void executePreHandshake(TlsSession & session) override;
   virtual void executePreStep(TlsSession & session) override;
   virtual void executePostStep(TlsSession & session) override;
   virtual void executePostHandshake(TlsSession & session) override;

   private:
	   const std::vector<uint8_t> compressionMethods;
};
}

#endif /* MANIPULATION_MANIPULATESERVEHELLOCOMPRESSIONMETHODS_H_ */
