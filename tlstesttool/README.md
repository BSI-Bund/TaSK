# TLS Test Tool
The TLS Test Tool is able to test a huge variety of TLS clients and servers. This README gives an overview over the structure of the TLS Test Tool and its usage. Furthermore, interfaces on different levels as well as the input and output formats for their correct usage are explained.

As a client, the TLS Test Tool establishes a TCP/IP connection and starts a TLS handshake by sending a [ClientHello message](https://datatracker.ietf.org/doc/html/rfc5246#section-7.4.1.2). The user can influence this default behaviour by using one or more of the provided manipulations.

As a server, the TLS Test Tool waits on a specific port and waits until a client connects to the server and performs a TLS handshake. The user can influence this default behaviour by using one or more of the provided manipulations.

Content:

<ol>
  <li><a href="README.md#building">Building the Tool</a></li>
  <li><a href="README.md#configuration">Configuration</a>
    <ol>
      <li><a href="README.md#cli">Command line arguments</a></li>
      <li><a href="README.md#configfile">Configuration file</a>
        <ol>
          <li><a href="README.md#inputbinary">Input of binary data</a></li>
          <li><a href="README.md#networkoptions">Network options</a></li>
          <li><a href="README.md#libraryoptions">Library options</a></li>
          <li><a href="README.md#loggingoptions">Logging options</a></li>
          <li><a href="README.md#tlsoptions">TLS options</a></li>
          <li><a href="README.md#messagemanipulations">Message manipulations</a></li>
          <li><a href="README.md#examples">Examples</a></li>
        </ol>
      </li>
    </ol>
  </li>
  <li><a href="README.md#license">License</a></li>
  <li><a href="README.md#3rdpartylicenses">Third party Licenses</a></li>
</ol>

<a name="building"></a>
## 1 Building the Tool
The TLS Test Tool uses CMake as its build system and requires a C++14-compatible compiler. For
building external libraries, Perl and Patch are required. For example, on a Debian GNU/Linux system,
the required software can be acquired by installing the build-essential and cmake packages.

To build the tool from the command line, perform the following steps:

```bash
cd [...]/tlstesttool
mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE=Release ..
cmake --build .
```

After a successful build, the TLS Test Tool can be found in the src sub-directory. 

<a name="configuration"></a>
## 2 Configuration
The available arguments for configuring the TLS Test Tool are described here. Values that a user
has to provide are denoted in square brackets (e.g., [length] for a value named length).

<a name="cli"></a>
### 2.1 Command line arguments
The TLS Test Tool expects at least one argument on the command line.
```bash
--configFile=[configuration file path]
```
Specify the path to a configuration file. When this argument is given multiple times, all
given configuration files are read. Options from configuration files that are given later on the
command line will overwrite options from those given earlier.

Examples:
```bash
TlsTestTool --configFile=config/TestCase27.conf
TlsTestTool --configFile=tlsOptions.conf
```

<a name="configfile"></a>
### 2.2 Configuration file
The configuration for the TLS Test Tool is given in a configuration file. The configuration file
is a plain text file. Lines that start with the hash sign (#) are treated as comments and ignored.
Arguments are given as name-value pairs separated with the equals sign (=). The following arguments
are known.

<a name="inputbinary"></a>
#### 2.2.1 Input of binary data
Binary data is given in hexadecimal form. The bytes of a byte array have to be encoded separately
and printed separated by a space character. Each byte is represented by two digits from 0-9a-f. For
example, the array of the two bytes 0xc0 0x30 has to be given as c0 30. In the following, the word
HEXSTRING- is used as placeholder for an arbitrary byte array. Please note that an empty byte array is
possible and has to be represented by an empty string.

<a name="networkoptions"></a>
#### 2.2.2 Network options

* `mode=[mode]`

  (required, with **mode** either `client` or `server`)

  Specify the **mode** for the TLS Test Tool. 

  If *mode=client*, the TLS Test Tool will run as a TLS
  client and connect to a server using TCP/IP. 

  If *mode=server*, the TLS Test Tool will run as a TLS server and listen for incoming TCP/IP connections. 


* `host=[host]`

  (required, if *mode=client*, with string **host**)

  If *mode=client*, specify a *host name or IP address* that the TLS Test Tool should connect to.

  Ignored, if *mode=server*.

* `port=[port]`

  (required, with decimal integer **port**)

  If *mode=client*, the *TCP port* of a service to connect to. 

  If *mode=server*, the *TCP port* to bind and listen to on the local host.

* `waitBeforeClose=[timeout]`

  (with *decimal* integer **timeout**, default `10 seconds`)

  Specify the *timeout in seconds* that the tool waits for incoming data after a run before
closing the TCP/IP connection.

* `receiveTimeout=[timeout]`

  (with *decimal* integer **timeout**, default `120 seconds`)

  Specify the *timeout in seconds* that the tool waits for incoming TCP/IP packets during a
receive operation.


* `listenTimeout=[timeout]`

  (with *decimal* integer **timeout**, default `60 seconds`)

  If *mode=server*, the TLS Test Tool will exit if no incoming TCP/IP connection is received
within timeout seconds.

  If not specified, then per default the TLS Test Tool chooses a 60 second timeout.

  Ignored, if *mode=client*.

<a name="libraryoptions"></a>
#### 2.2.3 Library options

* `tlsLibrary=[library]`

  (with **library** either `mbed TLS` or `OpenSSL`)

  Specify the *TLS library* to be used by the TLS Test Tool.

  If not specified, the `mbed TLS` library is used.

<a name="loggingoptions"></a>
#### 2.2.4 Logging options

* `logLevel=[level]`

  (with **level** in `{off, low, medium, high}`, default `off`)

  Amount of log output on the command line.

  `high` : Much debug output (e.g., additional hex dumps).

  `medium` : Medium amount of debug output (e.g., additional output of sizes of received packages).

  `low` : Little debug output (e.g., print actions that are performed).

  `off` : No output.


<a name="tlsoptions"></a>
#### 2.2.5 TLS options

* `certificateFile=[path]`

  (with **path** pointing to a PEM- or DER-encoded file)

  File containing a X.509 certificate that will be used as *server or client certificate*, respectively,
depending on the mode.

* `privateKeyFile=[path]`

  (with **path** pointing to a PEM- or DER-encoded file)

  File containing a private key that matches the certificate’s public key.

* `tlsVersion=([major],[minor])`

  (with *decimal* integers **major** equal to `3` and **minor** from `[1, 3]`)

  If *mode=client*, send the specified version in *ClientHello.client_version*.

  If *mode=server*, accept only the specified version and send it in *ServerHello.server_version*. 

  Use `(3,1)` for TLS v1.0,
`(3,2)` for TLS v1.1, `(3,3)` for TLS v1.2. If not specified, all three TLS versions are accepted
by a server and the highest version is sent by a client.

* `tlsCipherSuites=([valueUpper],[valueLower])[,([valueUpper],[valueLower])...]`

  (with *hexadecimal* integers **valueUpper** and **valueLower** preceded with 0x)

  Specify a list of *supported TLS cipher suites* in decreasing order of preference. If this option is 
set, at least one TLS cipher suite has to be given. 

  If *mode=client*, send this list in *ClientHello.cipher_suites*.

  If *mode=server*, use this list to find a matching TLS cipher suite to send in *ServerHello.cipher_suite*. 

  The values correspond to the values from the [TLS Cipher Suite Registry](https://datatracker.ietf.org/doc/html/rfc5246#section-7.2.1). For
example, the value `(0xC0,0x2C)` corresponds to TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384. If not specified, a default list of TLS cipher suites is used.

* `tlsServerDHParams=[predefined]`  

  *mbed TLS only*

  (with *predefined* from `{rfc3526_1536, rfc3526_2048, rfc3526_3072, rfc3526_4096, rfc3526_6144, rfc3526_8192, rfc5114_1024_160, rfc5114_2048_224 , rfc5114_2048_256}`)

  If *mode=server*, configure the Diffie-Hellman group that will be used. The value of predefined
can be one of the keys given in the above listing.

  Ignored, if *mode=client*.


* `tlsSecretFile=[path]`

  (with **path** pointing to an *output file*)

  Append the master_secret in the NSS Key Log Format to a plain text file. This file can be
used by Wireshark to decrypt TLS packets.


* `tlsEncryptThenMac=[enabled]`

  (with *Boolean* value **enabled** either `true` or `false`, default `true`)

  Enable (enabled=true) or disable (enabled=false) the usage of Encrypt-then-MAC.


* `tlsSupportedGroups=[predefined][,[predefined]...]`

  *OpenSSL only*
  
  (with **predefined** equal to a *key*)

  If *mode=client*, configure the supported groups in the corresponding ClientHello extension.
The supported groups extension indicates the named groups which the client supports for key
exchange, ordered from most preferred to least preferred.

  For example: `tlsSupportedGroups=secp192r1,secp224r1,secp256r1`

  Ignored, if *mode=server*.


* `tlsSignatureSchemes=([valueUpper],[valueLower])[,([valueUpper],[valueLower])...]` 

  *OpenSSL only*

  (with *hexadecimal* integers **valueUpper** and **valueLower** preceded with 0x)

  If *mode=client*, specify a list of supported TLS signature schemes in decreasing order of preference. If this
option is set, at least one TLS signature scheme has to be given.
Configure the TLS signature schemes in the corresponding ClientHello extension.

  For example, the value `(0x04,0x03)` corresponds to `ecdsa_secp256r1_sha256`.

  Ignored, if *mode=server*.


* `handshakeType=[type]`

  *OpenSSL only*

  (with **type** from `{normal, resumptionWithSessionID, resumptionWithSessionTicket, zeroRTT}`, default `normal`)

  The type of handshake that will be performed. For resumptionWithSessionID, resumptionWithSessionTicket and 
zeroRTT. If *mode=client*, then the client performs depending on the type either a normal handshake or a session resumption.
If a session resumption is performed (type = `{resumptionWithSessionID, resumptionWithSessionTicket, zeroRTT}`), then the sessionCache needs to be set as well.

  If *mode=server*, then the server expects the client to perform the configured handshake. If *type=normal*, the server listens and wait for the client to connect once and perform a normal TLS handshake. If a session resumption type is selected, then the server waits for the client to perform an intial handshake and after that to perform the session resumption. The TLS Test Tool server stays alive for these 2 handshakes.

  The *zeroRTT* handshakeType can only be selected if TLS 1.3 is used (https://datatracker.ietf.org/doc/html/rfc8446#section-2.3).


* `sessionCache=[SessionCacheString]`

  *OpenSSL only*

  (with the **SessionCacheString** outputted by the TLS Test Tool in the first handshake.)

  If *mode=client*, the session cache is used to perform a session resumption with either sessionIDs or session tickets. 

  Ignored, if *mode=server*.


* `earlyData=[bytes]` 

  *OpenSSL only*

  (with **bytes** given as *HEXSTRING*)
  
  Note: The **handshake type** must be `zeroRTT` and the **sessionCache** needs to be set.
  
  If *mode=client*, then the TLS Test Tool sends early data in the session resumption to the server (https://datatracker.ietf.org/doc/html/rfc8446#section-2.3). This feature only works in TLS 1.3.  
  
  Ignored, if *mode=server*.


* `ocspResponseFile=[path]`

  *OpenSSL only*

  (with **path** pointing to an *ocspResponseFile*)

  If *mode=server*, then the TLS Test Tool sends a CertificateStatus in the TLS handshake containing the OCSP response from the *ocspResponseFile* (OCSP stapling).
  
  Ignored, if *mode=client*.


* `psk=[bytes]`

  (with **bytes** given as *HEXSTRING*)

  Note: The configured **psk** is only used if a corresponding *PSK-ciphersuite* is set (see https://www.rfc-editor.org/rfc/rfc4279).

  Sets the psk (Pre-Shared Key) to the TLS Test Tool.
  Later, in the TLS handshake both parties reuse the psk to establish the TLS Session.

* `pskIdentityHint=[string]`

  *OpenSSL only*

  (with **bytes** given as *STRING*)

  Note: The configured **pskIdentityHint** is only used if a corresponding *PSK-ciphersuite* is set (see https://www.rfc-editor.org/rfc/rfc4279).

  Sets the pskIdentityHint (Pre-Shared Key) to the TLS Test Tool.
  Later, in the TLS handshake the server sends the identity hint in the Server Key Exchange message.

<a name="messagemanipulations"></a>
#### 2.2.6 Message manipulations

* `manipulateClientHelloCompressionMethods=[bytes]` 

  *mbed TLS only*

  (with **bytes** given as *HEXSTRING*)

  If *mode=client*, overwrite the field compression_methods in a ClientHello message with the
byte array given in bytes. 

  Ignored, if *mode=server*.


* `manipulateClientHelloExtensions=[bytes] `

  (with **bytes** given as *HEXSTRING*)

  If *mode=client*, overwrite the field *extensions* in a ClientHello message with the byte array
of extensions separated by a colon ("**:**"). 

  Ignored, if *mode=server*.

* `manipulateEllipticCurveGroup=[predefined]`

  *mbed TLS only*

  (with *predefined* from `{secp192k1, secp192r1, secp224k1, secp224r1, secp256k1, secp256r1, secp384r1, secp521r1 , brainpoolP256r1, brainpoolP384r1, brainpoolP512r1}`) )

  If *mode=server*, configure the elliptic curve group that will be used in the ServerKeyExchange message.

  Ignored, if *mode=client*.


* `manipulateForceCertificateUsage=[ignored]`

  *mbed TLS only*

  (with an arbitrary, possibly empty value **ignored**)
  
  If *mode=server*, when picking a certificate to send and no match is found (e.g., wrong key
  usage), send the first configured certificate instead of failing the handshake. This can be used
  to force sending of invalid certificates.
  
  Ignored, if *mode=client*.

* `manipulateHelloVersion=([major],[minor])` 

  *mbed TLS only*

  (with hexadecimal integers **major** and **minor** preceded with 0x)

  The two bytes given in **major** and **minor** define a – possibly invalid – *ProtocolVersion*. 

  If *mode=client*, replace the field client_version in a ClientHello message with the given protocol
version. 

  If *mode=server*, replace the field server_version in a ServerHello message with the
given protocol version.


* `manipulateRenegotiate=[ignored]` 

  *mbed TLS only*

  (with an arbitrary, possibly empty value **ignored**)

  Perform a *TLS renegotiation* after a successful TLS handshake.

  If *mode=client*, this means sending a ClientHello message.

  If *mode=server*, this means sending a HelloRequest message.

* `manipulateSendHeartbeatRequest=[when],[count],[data]`

  (with **when** either `beforeHandshake` or `afterHandshake`, with positive, decimal integer **count** and **data** given as *HEXSTRING*)

  Send a *HeartbeatRequest message* before starting a TLS handshake or after a successful TLS
handshake, with **data** as payload and **count** as payload length.

  **count** may be zero and **data** may be empty.

* `manipulateServerHelloCompressionMethods=[bytes]` 

  *mbed TLS only*

  (with **bytes** given as *HEXSTRING*)

  If *mode=server*, overwrite the field compression_methods in a ServerHello message with the
byte array given in bytes. 

  Ignored, if *mode=client*.

* `manipulateServerHelloExtensions=[bytes] `

  (with **bytes** given as *HEXSTRING*)

  If *mode=server*, overwrite the field *extensions* in a ServerHello message with the byte array
of extensions separated by a colon ("**:**"). 

  Ignored, if *mode=client*.

<a name="examples"></a>
#### 2.2.7 Examples

Example for running as a TLS client:

```bash
# Connect to a web server
mode=client
host=tls-check.de
port=443
```

<a name="license"></a>
## 3 License
The TLS Test Tool is licensed under the EUPL-1.2-or-later.
For more information on the license, see the included [License text](LICENSE.md) itself, or the according [website](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12) of the European Commision.

<a name="3rdpartylicenses"></a>
## 4 Third party Licenses

The licenses of 3rd party software used within TLS Test Tool are listed below.

### OpenSSL version 3.0.1
Website: [https://www.openssl.org/](https://www.openssl.org/)  
Source: [https://www.openssl.org/source/openssl-3.0.1.tar.gz](https://www.openssl.org/source/openssl-3.0.1.tar.gz)  
License: [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


### Mbed TLS version 2.2.1
Website: [https://tls.mbed.org](https://tls.mbed.org)  
Source: [https://tls.mbed.org/download/mbedtls-2.2.1-apache.tgz](https://tls.mbed.org/download/mbedtls-2.2.1-apache.tgz)  
License: [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


### asio version 1.12.2
Website: [https://think-async.com/Asio/](https://think-async.com/Asio/)  
Source: [http://downloads.sourceforge.net/project/asio/asio/1.12.2%20%28Stable%29/asio-1.12.2.tar.gz](http://downloads.sourceforge.net/project/asio/asio/1.12.2%20%28Stable%29/asio-1.12.2.tar.gz)  
License: [asio License](https://www.boost.org/LICENSE_1_0.txt)


### Zlib version 1.2.11
Website: [https://www.zlib.net](https://www.zlib.net)  
Source: [http://download.sourceforge.net/project/libpng/zlib/1.2.11/zlib-1.2.11.tar.gz](http://download.sourceforge.net/project/libpng/zlib/1.2.11/zlib-1.2.11.tar.gz)  
License: [zlib License](https://zlib.net/zlib_license.html)


