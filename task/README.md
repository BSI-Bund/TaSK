# TaSK Framework

*Version 0.5.2*

The "TaSK Framework" is a configurable test tool for performing TLS conformity tests based on the Technical Guideline [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf).
The test tool has a modular structure to enable efficient further development and applicability in different test scenarios.
This testing tool enables test centers to efficiently and effectively test various DUTs from different application-specific scenarios for conformity.
Furthermore, it offers manufacturers the possibility to perform independent tests during development.
In the case of TLS implementations, the test tool itself checks both the manufacturer's specifications and direct conformity with the BSI's technical guidelines.

## 1 Structure

The framework consists of two components: "TaSK" and "TLS Test Tool".

### 1.1 TaSK

The test case runner with report generator and validation mechanism for the input documents.
It is started via a command line interface and can be executed locally or via a REST API. The configuration is done via XML files which are explained later in this document. 

### 1.2 TLS Test Tool 

The TLS test tool is a stand-alone application that is capable of performing different TLS handshakes and apply manipulations on the TLS communication. It is used in the background by TaSK. 
Depending on the type of the DUT and the test-scenario, it can act as a TLS client or a TLS server. 

### 1.3 Overview TaSK Framework Modules

The TaSK framework has been designed to be modular. The numbered circles in the figure represent the order of the workflow.
![Overview TaSK modules and workflow](./documentation/overview.png "Overview TaSK")



Several *configuration files* are directly derived from the specification TR-03116-TS and need not to be edited. But the user must set the values in the global configuration files according to the test environment. 

The *MICS verifier* checks the manufacturer's specification for plausibility. 

Once the MICS verification has successfully been completed, the *test suite setup* module can start generating the *test case run plan* (TRP). The TRP is a self-contained XML file that contains a list of test cases to be executed along with all the required parameters and dependencies. Only test cases determined by the application profile will be executed. The test cases are independent from each other and do not rely on a specific execution order unless a dependency has been explicitly specified in the test case description. 

The TRP is then used as input to the *test case execution*. The test cases are executed sequentially one after another as specified in the TRP and the results are captured. To improve code reuse, the test cases have been functionally divided into smaller blocks, namely test fragments. 


## 2 Building the Framework

The build of the TaSK framework is done using [Apache Maven](https://maven.apache.org/).

The build can be executed via the command line, e.g.

```bash
cd ./task
mvn clean install
```

### 2.1 System Requirements for Building

The following system requirements should be met when trying to build the TaSK framework:
- Operating System: x86-64 Linux Distribution, e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop) or [Debian 10](https://www.debian.org/index.de.html)
- Java Version: JDK 17 or newer, e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/)
- Apache Maven 3.8.5 or newer, [Apache Maven](https://maven.apache.org/)

### 2.2 Generation of JavaDocs

After the build of the TaSK project has been successfully executed, the corresponding JavaDocs are available at each module's target directory.
To aggregate the JavaDocs into one single location, the following call to Maven can be executed via the command line.  

```bash
mvn org.apache.maven.plugins:maven-javadoc-plugin:aggregate-no-fork
```
As a result, the JavaDocs will be aggregated at `task/target/site/apidocs`.

## 3 Execution

This chapter explains how the TaSK framework can be executed and configured. 

### 3.1 System Requirements for Execution

The following system requirements should be met when trying to execute the TaSK framework:
- Operating System: x86-64 Linux Distribution, e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop) or [Debian 10](https://www.debian.org/index.de.html).
- Java Version: JRE 17 or newer, e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/)
- TLS Test Tool Version: 0.5.1
- Python3 Version: 3.7 or newer, see [Python3](https://www.python.org/downloads/)
- OpenSSL Version: 3.0.5 or newer, see [OpenSSL 3.0.5](https://www.openssl.org/source/)

### 3.2 Command Line Interface

The TaSK framework provides a command line interface to the user, which can be used to run the test framework.
After the build process, the command line interface JAR can be found in the `com.achelos.task.commandlineinterface/target` directory under the name `com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar`.

The command line interface can be called in the following way:

```bash
java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar <arguments>
```

#### 3.2.1 Command Line Parameters

`-c, --config-file <arg>`   
*Required*   
The path to the global configuration XML file.   

`   --client-auth-certchain <arg>`   
*Required only for testing TLS server with client authentication*  
The PEM encoded client authentication certificate chain.
If servers with client authentication are tested, then a valid client certificate chain [CERT_DEFAULT_CLIENT] has to be provided to the Tls Test Tool
according to table 19 of the [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf).
Thus, the tester has to enter a valid client certificate chain via this CLI parameter and the corresponding private key (see command line parameter below).
The tester is responsible to offer a valid client certificate which the TOE trusts.

`   --client-auth-key <arg>`   
*Required only for testing TLS server with client authentication*  
The PEM encoded private key for the client authentication certificate.
This key should correspond to the entered client certificate chain (see command line parameter above).

`-d, --certificate-directory <arg>`   
*Required if CHECK_CERTS Profile is set in MICS. Otherwise optional.*   
The path to a directory with certificates in either DER or PEM encoding. 

`-g, --debug`   
*Optional*   
Print additional debug information to the console

`-i, --ignore-mics-verification`   
*Optional*   
Ignore the result of the MICS verification when running the resulting test cases.

`-m, --mics-file <arg>`   
*Required if executed locally and no test run plan is provided*   
The path to a machine-readable ICS file.

`-p, --generate-pdf-report`   
*Optional*   
Generate a PDF report. Also generates the XML report, regardless of whether option `-x` is set.

`-s, --rest-server`     
*Required if no local execution shall be executed.*    
Flag indicating whether the TaSK framework shall be executed as a REST server.     
*Note: In REST server mode, only TLS-Server DUTs can be tested*

`-t, --testrunplan <arg>`   
*Required if executed locally and no MICS is provided*    
The path to a TestRunplan XML file.

`-x, --generate-xml-report`   
*Optional*   
Generate a XML report


### 3.3 Execution Modes

During the execution of the TaSK CLI, either a MICS file (`mics-file`), a test run plan file (`testrunplan`),
or the REST server option (`rest-server`) has to be provided. If either the MICS or the test run plan file has been provided, the test run is executed locally.
If the REST server option is set, the TaSK framework is executed as a REST server, and listens for incoming execution requests on the specified network interface.

#### 3.3.1 Local Mode

When the TaSK framework is executed locally, all configuration and input data has to be provided via the CLI.
A single test run is executed with the provided information and the TaSK framework shuts down afterwards.

#### 3.3.2 REST Server Mode

If the TaSK framework is executed in REST server mode, a HTTP(S) Server is started and the TaSK framework listens for incoming execution requests.
The provider who runs the TaSK framework server is required to provide the global configuration file via the CLI.
The global configuration file contains parameters for the specification of hostname and port the server should listen to.
A user can execute a test case run, by providing a MICS file or a test run plan file for a DUT of Type TLS-Server via the REST interface.
An OpenAPI v3 conform specification of the REST API can be either found here: [`openapi.yaml`](com.achelos.task.rest-impl/src/main/resources/openapi.yaml)
or retrieved from a running TaSK REST server via a `GET /` request. 
A tool like e.g. [Swagger UI](https://swagger.io/tools/swagger-ui/) can be used to visualize the REST API documentation.

In the Global Configurations the TLS credentials for the REST Server can be specified by providing a PKCS#12 file. 
All necessary data must be contained in this file. The Root CAs certificate shall be provided to potential client applications
to allow a secure connection.

### 3.4 Quick Start

The following steps are meant as a quick start guide. More detailed informations are contained in the following chapters.
1. Make sure the *System Requirements for Execution* are fulfilled.  
    The TLS Test Tool is available as a separate Source Bundle. For information about the build of the TLS Test Tool have a look at contained `README` file.
2. Copy the exemplary global configuration file [`ExampleGlobalConfig.xml`](data/configuration/ExampleGlobalConfig.xml) to a location of your choice.  
Edit your copy of the global configuration by setting all mandatory configuration options as described in the comments of the `ExampleGlobalConfig.xml`.  
The specification files are contained in the *data* directory of the TaSK source bundle and can be used as is.
3. Copy one of the exemplary MICS files from [`<task-dir>/data/input/`](data/input/) to a location of your choice.  
    Edit your copy of the MICS file to contain the required information about the device under test according to chapter 3 of the [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf).
4. If the `CHECK_CERTS` profile is contained in your MICS file, make sure to have the TLS certificate chain of your device under test available as single files in a known location.
5. The TaSK framework can now be executed by the following call:

        java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar
        --config-file
        /path/to/your/copy/ExampleGlobalConfig.xml
        --mics-file
        /path/to/your/copy/ExampleMICS.xml
        --generate-pdf-report
        # If CHECK_CERTS Profile is included
        --certificate-directory
        /path/to/your/certificates



### 3.5 User Input

The user of the TaSK framework has two options of executing the TaSK framework.

1. MICS   
  The user can provide a machine-readable version of the ICS document ("MICS").  
  If the "CHECK_CERTS" test profile has been set in the MICS, the respective part in the MICS has also to be set and the respective certificate chain has to be provided as an input to the TaSK framework.  
  If a MICS is provided and can be successfully verified, the TaSK framework generates a test run plan file to specify the test suite to execute. 

1. Test Run Plan  
  If an already existing test run plan file is provided, the MICS file and the certificate verification is skipped, and the test suite specified by this test run plan file is executed.

*Note: For some application types, it might be necessary to provide an additional interface for the DUT in form of a RMI service. For more information please see the chapter [DUT motivators](#37-dut-motivators)* 

#### 3.5.1 MICS 

The Machine-readable Implementation Conformance Statement ("MICS") is a XML file, which includes information about the DUT, provided by the vendor according to chapter 3 of the [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf). 
The structure of the MICS file is based on tables 2 to 17 in the document. 

The MICS XML must be conform to `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/input/MICS.xsd`.  
It is provided via the CLI with the option `-m`:

```bash
java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar <other_arguments> -m /path/to/mics.xml
```

Commented MICS examples are contained in `<task-dir>/data/input/`.  
See the annex of this readme for more details.

#### 3.5.2 DUT certificates

If the "CHECK_CERTS" test profile has been set in the MICS the test cases "TLS_CERT_01" - "TLS_CERT_12" described in the [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf) will be executed by the MICS verifier module.
For that purpose the TLS certificate chain used by the DUT has to be provided to the TaSK framework. 

Via the CLI a directory can be specified, which is searched for the respective certificates. Each certificate has to be provided as a separate file, encoded either in PEM or in DER encoding.

```bash
java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar <other_arguments> -d /path/to/certificate/directory
```

#### 3.5.3 Test Run Plan

The TaSK framework uses the provided MICS file to generate a test run plan file. This test run plan file is used to specify an application specific test suite containing the test cases corresponding to the test profiles of the device under test.

Moreover, an already existing test run plan file can be used to rerun the specified test suite from the command line interface. 

Via the CLI a test run plan file can be provided as an alternative to a MICS file with the option `-t`:

```bash
java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar <other_arguments> -t /path/to/testrunplan/file.xml
```

The test run plan must be conform to the XML schema `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/testrunplan/TestRunPlan.xsd`.  

### 3.6 Configuration

The configuration of the TaSK framework is done via a set of XML files. On one hand a *Global Configuration* file is provided, which is used to set a number of options and configurations regarding the general execution of the TaSK framework.

On the other hand, a collection of XML files is used to represent test cases, application specific profiles and technical guidelines, which are used as a baseline by the TaSK framework. Usually there is no need to edit these files. 

All XML schemas are contained as resource in the module `com.achelos.task.xmlparser`.

#### 3.6.1 Global Configuration

The global configuration XML file is used by the TaSK framework to specify the general test framework properties to be used. 
For example, the name of the tester or test center that should appear in the report, the type of report to generate, or the location of the "TLS Test Tool".

The global configuration XML file must be conform to  `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/configuration/GlobalConfig.xsd` and is provided to the CLI with the option `-c`:

```bash
java -jar com.achelos.task.commandlineinterface-<version>-jar-with-dependencies.jar <other_arguments> -c /path/to/global/configuration.xml
```
Commented example: `<task-dir>/data/input/ExampleGlobalConfig.xml`

#### 3.6.2 Application Specifications

The TaSK framework has been designed to be configurable with minimal effort. This means that if an application specific profile or technical guideline needs to be updated in the future, or a new application specific profile or technical guideline needs to be added, it can be done without modifying any code. To achieve this, the TaSK framework uses a collection of XML configuration files during its initialization.

- TR-03116-TS Test Profiles
- TR-03116-TS Test Case Definitions
- Application Specific Profiles
- Technical Guideline Specifications

The root directory of the specification files has to be provided as a global configuration parameter, which includes all of these files in the specified structure:

##### 3.6.2.1 TR-03116-TS Test Profiles

One XML file specifies all the valid test profiles according to Table 1 in [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf). Profiles are used during the MICS verification process to identify the subset of test cases for the run plan.

The profiles definition file is `<task-dir>/data/specification/TestProfiles.xml`.  
This XML is conform to `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/configuration/TestProfiles.xsd`.

##### 3.6.2.2 TR-03116-TS Test Case Definitions

The BSI has made available all the test case definitions specified in [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf) as a collection of XML files. 
These XML files are included in the configuration data and consist of different attributes and elements. However, we are only interested in the "id" attributes and the "Profile" elements.
This information is used in the MICS validation and test suite setup modules to select the test cases that have to be executed according to the application specific profile.

The test case definition files are stored in the directory `<task-dir>/data/specification/TestCases`.  
Test case definitions must be conform to the Schema `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/configuration/TestCase.xsd`.

##### 3.6.2.3 TR-03116-TS Application Specific Profiles

The test cases defined in [TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_v1.pdf) are a collection of different TLS test cases that can be executed for different types of applications.
The decision on the applicability of a particular TLS test case is made based on the specific application at hand. Therefore, in order to execute the TLS test cases, each application (e.g. eID-Client, eID-Server, Smart Metering, E-Mail-Trsp, etc.) must specify which profiles are applicable for it.

These application profiles are specified in the [Annex to BSI TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_Annex.pdf).
The profile mappings for these applications have been manually specified in XML configuration files that are then supplied to the TaSK framework. 

The application profiles definition files are stored in the directory `<task-dir>/data/specification/ApplicationSpecificProfiles`.  
These XML files must be conform to `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/configuration/ApplicationMapping.xsd`.

##### 3.6.2.4 TR-03116-TS Technical Guideline Specifications

Each application is mapped to a base specification which is defined in the [Annex to BSI TR-03116-TS](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03116/BSI-TR-03116-TS_Annex.pdf).
However, such a base specification can be derived from multiple specification documents. 

Each specification document has been divided into smaller modules and is represented in XML.
Specific modules are then merged together to form the application specification. 

Moreover, a specification document can relate to another specification document as a base specification. The corresponding XML file can relate to another specification for that purpose.
These relations are resolved by the TaSK framework during the reading of such specifications. While doing so, subsequent specifications override requirements of the underlying base specification.

The specification fragments can be found in `<task-dir>/data/specification/ApplicationSpecifications/`.  
They are conform to `<task-dir>/com.achelos.task.xmlparser/src/main/resources/schemas/configuration/TLSSpecification.xsd`.

### 3.7 DUT motivators

For some application types it is necessary to use an external motivator to interact with the DUT.

#### 3.7.1 Browser Simulator

If the DUT is an eID-Client application (ApplicationType `TR-03124-1-EID-CLIENT-TLS-1-2` or `TR-03124-1-EID-CLIENT-TLS-2`), TaSK uses the BrowserSimulator from the [eID-Client Testbed](https://github.com/eID-Testbeds/client) to motivate the DUT to connect to the server. 

The BrowserSimulator works as a RMI server and implements the three methods from the IBrowserSimulator interface:
- startApp
- sendHttpRequest
- stopApp

For more details and configuration options see the BrowserSimulator documentation.

Make sure to set a rule in your hostfile (remote and/or local)

    task.achelos.de  <ip address>
    
Then start the BrowserSimulator before executing the test run with TaSK:  

    java -jar browsersimulator.jar <IP address> </path/to/eID-Client executable> 

#### 3.7.2 TCToken URL Provider

If the test interface `TR-03130-1-EID-SERVER-ECARD-PSK` is selected, a RMI service needs to be implemented, which satisfies the `com/achelos/task/rmi/tctokenprovider/TCTokenURLProvider.java` interface from the module `com.achelos.task.configuration`.

For successful tests of the interface `TR-03130-1-EID-SERVER-ECARD-PSK` a corresponding eService simulation is required and has to be deployed in addition to the eID-Server.
The TLS testbed does not provide an implementation of this eService simulation, i.e. the operator of the TLS testbed must ensure that this component is given.
The eService simulation will be initiated by the TLS testbed, more precisely by a special RMI component, the `TCTokenURLProvider`.
During this process, the RMI component communicates with the eService simulation to initiate the online authentication request and to retrieve the `TCTokenURL`.
The `TCTokenURL` presents the URL under which the TC Token for the eID-Server connection can be obtained.

Because the implementations of the eService simulation may differ regarding their structure, customizations must be made to the RMI component.
A well commented sample RMI component is given as a separate Maven project in `rmi_examples/TCTokenURLProvider`.
To build this exemplary `TCTokenURLProvider` switch the directory to the `rmi_examples/TCTokenURLProvider` folder and execute the `mvn clean install` command.
After the build, you can execute the RMI server with the command `java -jar target/TCTokenURLProvider.jar`.

#### 3.7.3 Adapting the MICS

If a RMI service is necessary to provide, the MICS or TRP need to be adapted. The address and port under which the RMI service is available need to be added to the *ApplicationSpecification* of the DUT in these documents, e.g.:

```xml
<ApplicationUnderTest>
    <ApplicationType>TR-03130-1-EID-SERVER-ECARD-PSK</ApplicationType>
    <RespectiveTechnicalGuideline>TR-03130-1</RespectiveTechnicalGuideline> <!-- Optional-->
    <URL>test.address</URL> <!-- Address of the eID-Servers eCard API -->
    <Port>443</Port> <!--  Port of the eID-Servers eCard API -->
    <RMIURL>localhost</RMIURL> <!-- Address of the TCTokenURLProvider RMI Service -->
    <RMIPort>1099</RMIPort> <!-- Port of the TCTokenURLProvider RMI Service -->
</ApplicationUnderTest>
```

## 4 License

The TaSK framework is licensed under the EUPL-1.2-or-later.
For more information on the license, see the included [license text](documentation/LICENSE.md) or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).

## 5 Third Party Libraries

All dependencies to third party libraries, their respective websites and licenses are listed in [documentation/3rdPartyLicenses.md](documentation/3rdPartyLicenses.md)

## 6 Annex: XML Examples

### 6.1 TaSK Configuration Examples

Commented examples of the configuration files can be found in TaSK's `data`-folder.

- MICS example for TLS client: [`<task-dir>/data/input/ExampleMICS_Client.xml`](data/input/ExampleMICS_Client.xml)
- MICS example for TLS server: [`<task-dir>/data/input/ExampleMICS_Server.xml`](data/input/ExampleMICS_Server.xml)
- MICS example for eID-Client TLS-1-2: [`<task-dir>/data/input/ExampleMICS_eID-Client-TLS-1-2.xml`](data/input/ExampleMICS_eID-Client-TLS-1-2.xml)
- MICS example for eID-Client TLS-2: [`<task-dir>/data/input/ExampleMICS_eID-Client-TLS-2.xml`](data/input/ExampleMICS_eID-Client-TLS-2.xml)
- MICS example for eID-Server EIDAS-MW: [`<task-dir>/data/input/ExampleMICS_eID-Server-EIDAS-MW.xml`](data/input/ExampleMICS_eID-Server-EIDAS-MW.xml)
- MICS example for eID-Server SAML: [`<task-dir>/data/input/ExampleMICS_eID-Server-SAML.xml`](data/input/ExampleMICS_eID-Server-SAML.xml)
- MICS example for eID-Server eCard API NonPSK: [`<task-dir>/data/input/ExampleMICS_eID-Server-eCard-API-NonPSK.xml`](data/input/ExampleMICS_eID-Server-eCard-API-NonPSK.xml)
- MICS example for eID-Server eCard API PSK: [`<task-dir>/data/input/ExampleMICS_eID-Server-eCard-API-PSK.xml`](data/input/ExampleMICS_eID-Server-eCard-API-PSK.xml)
- MICS example for eID-Server eID Interface: [`<task-dir>/data/input/ExampleMICS_eID-Server-eID-Interface.xml`](data/input/ExampleMICS_eID-Server-eID-Interface.xml)
- Test run plan example for TLS client: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_Client.xml`](data/testrunplan/TRP_ExampleMICS_Client.xml)
- Test run plan example for TLS server: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_Server.xml`](data/testrunplan/TRP_ExampleMICS_Server.xml)
- Test run plan example for eID-Client TLS-1-2: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Client-TLS-1-2xml`](data/testrunplan/TRP_ExampleMICS_eID-Client-TLS-1-2.xml)
- Test run plan example for eID-Client TLS-2: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Client-TLS-2xml`](data/testrunplan/TRP_ExampleMICS_eID-Client-TLS-2.xml)
- Test run plan example for eID-Server EIDAS-MW: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Server-EIDAS-MW.xml`](data/testrunplan/TRP_ExampleMICS_eID-Server-EIDAS-MW.xml)
- Test run plan example for eID-Server SAML: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Server-SAML.xml`](data/testrunplan/TRP_ExampleMICS_eID-Server-SAML.xml)
- Test run plan example for eID-Server eCard API NonPSK: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Server-eCard-API-NonPSK.xml`](data/testrunplan/TRP_ExampleMICS_eID-Server-eCard-API-NonPSK.xml)
- Test run plan example for eID-Server eCard API PSK: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Server-eCard-API-PSK.xml`](data/testrunplan/TRP_ExampleMICS_eID-Server-eCard-API-PSK.xml)
- Test run plan example for eID-Server eID Interface: [`<task-dir>/data/testrunplan/TRP_ExampleMICS_eID-Server-eID-Interface.xml`](data/testrunplan/TRP_ExampleMICS_eID-Server-eID-Interface.xml)
- Global configuration example: [`<task-dir>/data/configuration/ExampleGlobalConfig.xml`](data/configuration/ExampleGlobalConfig.xml)
- TR-03116-TS test case definitions: [`<task-dir>/data/specification/TestCases/TR-03116-TS`](data/specification/TestCases/TR-03116-TS)
