# TaSK Framework

*Version 1.0.1*

The "TaSK Framework" is a configurable test tool for performing TLS conformity tests based on the Technical Guideline [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf).
The test tool has a modular structure to enable efficient further development and applicability in different test scenarios.
This testing tool enables test centers to efficiently and effectively test various DUTs from different application-specific scenarios for conformity.
Furthermore, it offers manufacturers the possibility to perform independent tests during development.
In the case of TLS implementations, the test tool itself checks both the manufacturer's specifications and direct conformity with the BSI's technical guidelines.


## Structure

The framework consists of two components: "TaSK" and "TLS Test Tool".

### TaSK

The test case runner with report generator and validation mechanism for the input documents.
It is started via a command line interface and can be executed locally or via a REST API.
The configuration is done via XML files.
The component, including in-depth documentation, is provided in [task](./task).

### TLS Test Tool

The TLS test tool is a stand-alone application that is capable of performing different TLS handshakes and apply manipulations on the TLS communication.
It is used in the background by TaSK.
Depending on the type of the DUT and the test-scenario, it can act as a TLS client or a TLS server.
The component, including in-depth documentation, is provided in [tlstesttool](./tlstesttool).


## License

The TaSK framework is licensed under the European Union Public Licence Version 1.2 (or-later).
For more information on the license, see the included license text itself or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
