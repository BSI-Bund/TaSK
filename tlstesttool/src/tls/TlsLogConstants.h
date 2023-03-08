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
#ifndef TLS_TLSLOGCONSTANTS_H_
#define TLS_TLSLOGCONSTANTS_H_

namespace TlsTestTool {
/**
 * Constants containing values for the logging interface.
 */
namespace TlsLogConstants {
const char * const CLIENTHELLO_RX = "ClientHello message received.\n";
const char * const CLIENTHELLO_RX_VALID = "Valid ClientHello message received.\n";
const char * const CLIENTHELLO_RX_BAD = "Bad ClientHello message received.\n";
const char * const CLIENTHELLO_TX = "ClientHello message transmitted.\n";
const char * const SERVERHELLO_RX_VALID = "Valid ServerHello message received.\n";
const char * const SERVERHELLO_RX_BAD = "Bad ServerHello message received.\n";
const char * const SERVERHELLO_TX = "ServerHello message transmitted.\n";
const char * const HELLORETRY_RX_VALID = "Valid HelloRetryRequest message received.\n";
const char * const HELLORETRY_RX_BAD = "Bad HelloRetryRequest message received.\n";
const char * const HELLORETRY_TX = "HelloRetryRequest message transmitted.\n";
const char * const CERTIFICATE_RX_VALID = "Valid Certificate message received.\n";
const char * const CERTIFICATE_RX_BAD = "Bad Certificate message received.\n";
const char * const CERTIFICATE_TX = "Certificate message transmitted.\n";
const char * const CERTIFICATE_STATUS_RX = "Certificate status message received.\n";
const char * const CERTIFICATE_STATUS_TX = "Certificate status message transmitted.\n";
const char * const ENCRYPTEDEXTENSIONS_RX_VALID = "Valid EncryptedExtensions message received.\n";
const char * const ENCRYPTEDEXTENSIONS_TX = "EncryptedExtensions message transmitted.\n";
const char * const SERVERKEYEXCHANGE_RX_VALID = "Valid ServerKeyExchange message received.\n";
const char * const SERVERKEYEXCHANGE_RX_BAD = "Bad ServerKeyExchange message received.\n";
const char * const SERVERKEYEXCHANGE_TX = "ServerKeyExchange message transmitted.\n";
const char * const CERTIFICATEREQUEST_RX_VALID = "Valid CertificateRequest message received.\n";
const char * const CERTIFICATEREQUEST_RX_BAD = "Bad CertificateRequest message received.\n";
const char * const CERTIFICATEREQUEST_TX = "CertificateRequest message transmitted.\n";
const char * const SERVERHELLODONE_RX_VALID = "Valid ServerHelloDone message received.\n";
const char * const SERVERHELLODONE_RX_BAD = "Bad ServerHelloDone message received.\n";
const char * const SERVERHELLODONE_TX = "ServerHelloDone message transmitted.\n";
const char * const CLIENTKEYEXCHANGE_RX_VALID = "Valid ClientKeyExchange message received.\n";
const char * const CLIENTKEYEXCHANGE_RX_BAD = "Bad ClientKeyExchange message received.\n";
const char * const CLIENTKEYEXCHANGE_TX = "ClientKeyExchange message transmitted.\n";
const char * const CERTIFICATEVERIFY_RX_VALID = "Valid CertificateVerify message received.\n";
const char * const CERTIFICATEVERIFY_RX_BAD = "Bad CertificateVerify message received.\n";
const char * const CERTIFICATEVERIFY_TX = "CertificateVerify message transmitted.\n";
const char * const CHANGECIPHERSPEC_RX_VALID = "Valid ChangeCipherSpec message received.\n";
const char * const CHANGECIPHERSPEC_RX_BAD = "Bad ChangeCipherSpec message received.\n";
const char * const CHANGECIPHERSPEC_TX = "ChangeCipherSpec message transmitted.\n";
const char * const FINISHED_RX_VALID = "Valid Finished message received.\n";
const char * const FINISHED_RX_BAD = "Bad Finished message received.\n";
const char * const FINISHED_TX = "Finished message transmitted.\n";

const char * const ALERT_RX = "Alert message received.\n";
const char * const ALERT_LEVEL = "Alert.level=%02x\n";
const char * const ALERT_DESCRIPTION = "Alert.description=%02x\n";

const char * const CLIENTHELLO_CLIENTVERSION = "ClientHello.client_version";
const char * const CLIENTHELLO_RANDOM = "ClientHello.random";
const char * const CLIENTHELLO_SESSIONID = "ClientHello.session_id";
const char * const CLIENTHELLO_CIPHERSUITES = "ClientHello.cipher_suites";
const char * const CLIENTHELLO_COMPRESSIONMETHODS = "ClientHello.compression_methods";
const char * const CLIENTHELLO_EXTENSIONS = "ClientHello.extensions";
const char * const SERVERHELLO_SERVERVERSION = "ServerHello.server_version";
const char * const SERVERHELLO_RANDOM = "ServerHello.random";
const char * const SERVERHELLO_SESSIONID = "ServerHello.session_id";
const char * const SERVERHELLO_CIPHERSUITE = "ServerHello.cipher_suite";
const char * const SERVERHELLO_COMPRESSIONMETHOD = "ServerHello.compression_method";
const char * const SERVERHELLO_EXTENSIONS = "ServerHello.extensions";
const char * const ENCRYTPED_EXTENSIONS = "EncryptedExtensions.extensions";

const char * const HELLORETRY_SERVERVERSION = "HelloRetryRequest.server_version";
const char * const HELLORETRY_RANDOM = "HelloRetryRequest.random";
const char * const HELLORETRY_SESSIONID = "HelloRetryRequest.session_id";
const char * const HELLORETRY_CIPHERSUITE = "HelloRetryRequest.cipher_suite";
const char * const HELLORETRY_COMPRESSIONMETHOD = "HelloRetryRequest.compression_method";
const char * const HELLORETRY_EXTENSIONS = "HelloRetryRequest.extensions";

const char * const SERVERKEYEXCHANGE_PARAMS_CURVEPARAMS_NAMEDCURVE =
		"ServerKeyExchange.params.curve_params.namedcurve=%02x";
const char * const SERVERKEYEXCHANGE_PARAMS_PUBLIC = "ServerKeyExchange.params.public=";
const char * const SERVERKEYEXCHANGE_PARAMS_DHP = "ServerKeyExchange.params.dh_p";
const char * const SERVERKEYEXCHANGE_PARAMS_DHG = "ServerKeyExchange.params.dh_g";
const char * const SERVERKEYEXCHANGE_PARAMS_DHYS = "ServerKeyExchange.params.dh_Ys";
const char * const SERVERKEYEXCHANGE_SIGNEDPARAMS_ALGORITHM_HASH =
		"ServerKeyExchange.signed_params.algorithm.hash=%02x\n";
const char * const SERVERKEYEXCHANGE_SIGNEDPARAMS_ALGORITHM_SIGNATURE =
		"ServerKeyExchange.signed_params.algorithm.signature=%02x\n";
const char * const SERVERKEYEXCHANGE_SIGNEDPARAMS_MD5HASH = "ServerKeyExchange.signed_params.md5_hash";
const char * const SERVERKEYEXCHANGE_SIGNEDPARAMS_SHAHASH = "ServerKeyExchange.signed_params.sha_hash";
const char * const SERVERKEYEXCHANGE_SIGNEDPARAMS_SIGNATURE = "ServerKeyExchange.signed_params.signature";
const char * const CLIENTKEYEXCHANGE_EXCHANGEKEYS_PREMASTERSECRET = "ClientKeyExchange.exchange_keys.pre_master_secret";
const char * const CLIENTKEYEXCHANGE_EXCHANGEKEYS_MASTERSECRET = "ClientKeyExchange.exchange_keys.master_secret";

const char * const HEARTBEATMESSAGE_RECEIVED_TLS13 = "Heartbeat message received";
const char * const HEARTBEATMESSAGE_TRANSMITTED_TLS13 = "Heartbeat message transmitted";
}
}

#endif /* TLS_TLSLOGCONSTANTS_H_ */
