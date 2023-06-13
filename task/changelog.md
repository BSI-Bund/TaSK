# Changelog for Version 1.0.1

## This is fixed
* CLI parameters `--certificate-validation-ca`, `client-auth-certchain` and `client-auth-key` 
now support relative and absolute paths
* Testcases using OpenSSL with brainpool curves in TLS 1.2 are now working correctly

# Changelog for Version 1.0.0

## This is new
* Generation of OpenAPI.yaml during build.
* Experimental support for `TR-03108-1-EMSP-CLIENT-CETI-DANE` application type.
* Dockerfile has been added to execute TaSK Framework.
* Add option to only create TestRunplan and skip execution.
* Add TLSv1.3 support for some testcases.
* Improvement of documentation
* Update TaSK Framework to new Draft of TR-03116-TS and its Annex.
## This is fixed
* Execution of RMI Components Bug was fixed.
* Fix bug in Manual DUT Motivator.
* Fixed bugs in test cases:
  * TLS_B1_FR_08
  * TLS_B1_GP_06
  * TLS_A2_CH_06_T
  * TLS_A2_CH_08
  * TLS_A1_CH_06_T
  * TLS_A1_CH_08
  * TLS_B2_FR_11

# Changelog for Version 0.6.0

## This is new

* Support for Smart-Meter-Gateway tests
  * HAN Client
  * HAN Server
  * WAN Client
* Support for Email test
  * IMAPS
  * POP3S
  * SMTPS
* Improved DUT motivator architecture
* Additional DUT motivator for manual motivation

## This is fixed

* Resolved github issues #6, #7, #8


### New Documents

* example MICS and TRP files for Smart Meter Gateway tests
* example MICS and TRP files for EMail tests

### New Testcases

* Module A1-FR
  * TLS_A1_FR_05
* Module A2-CH
  * TLS_A2_CH_01
  * TLS_A2_CH_02
  * TLS_A2_CH_03
  * TLS_A2_CH_04
  * TLS_A2_CH_05
  * TLS_A2_CH_06_T
  * TLS_A2_CH_07
  * TLS_A2_CH_08
  * TLS_A2_CH_09
  * TLS_A2_CH_10_T
* Module A2-FR
  * TLS_A2_FR_01
  * TLS_A2_FR_02
  * TLS_A2_FR_03
  * TLS_A2_FR_04
  * TLS_A2_FR_05
  * TLS_A2_FR_06
  * TLS_A2_FR_07
  * TLS_A2_FR_08
  * TLS_A2_FR_09
  * TLS_A2_FR_10
  * TLS_A2_FR_11
  * TLS_A2_FR_12_T
  * TLS_A2_FR_13
* Module A2-GP
  * TLS_A2_GP_01_T
  * TLS_A2_GP_02_T
  * TLS_A2_GP_03_T
  * TLS_A2_GP_04
  * TLS_A2_GP_05
  * TLS_A2_GP_06_T
  * TLS_A2_GP_07_T
* Module B2-FR
  * TLS_B2_FR_02_T
  * TLS_B2_FR_03
  * TLS_B2_FR_05
  * TLS_B2_FR_09
  * TLS_B2_FR_10
  * TLS_B2_FR_12
  * TLS_B2_FR_13
  * TLS_B2_FR_17
  * TLS_B2_FR_18
  * TLS_B2_FR_18
* Module B2-GP
  * TLS_B2_GP_09_T

### Updated Testcases

* Module A1-CH
  * TLS_A1_CH_01
  * TLS_A1_CH_02
  * TLS_A1_CH_03
  * TLS_A1_CH_04
  * TLS_A1_CH_05
  * TLS_A1_CH_06_T
  * TLS_A1_CH_07
  * TLS_A1_CH_08
  * TLS_A1_CH_09
  * TLS_A1_CH_10_T
* Module A1-FR
  * TLS_A1_FR_01
  * TLS_A1_FR_02
  * TLS_A1_FR_03
  * TLS_A1_FR_06
  * TLS_A1_FR_07
  * TLS_A1_FR_08
  * TLS_A1_FR_09
  * TLS_A1_FR_11
* Module A1-GP
  * TLS_A1_GP_01_T
  * TLS_A1_GP_02_T
  * TLS_A1_GP_03_T
  * TLS_A1_GP_04
  * TLS_A1_GP_05
  * TLS_A1_GP_06_T
  * TLS_A1_GP_07_T
* Module B2-FR
  * TLS_B1_FR_01_T
  * TLS_B1_FR_02_T
  * TLS_B1_FR_03
  * TLS_B1_FR_04
  * TLS_B1_FR_09
  * TLS_B1_FR_11
  * TLS_B1_FR_17
* Module B2-GP
  * TLS_B1_GP_01_T
  * TLS_B1_GP_02
  * TLS_B1_GP_03_T
  * TLS_B1_GP_04
  * TLS_B1_GP_05_T
  * TLS_B1_GP_08
  * TLS_B1_GP_09
* Module B2-FR
  * TLS_B2_FR_01_T
  * TLS_B2_FR_11
  * TLS_B2_FR_15
  * TLS_B2_FR_16
* Module B2-GP
  * TLS_B2_GP_01_T
  * TLS_B2_GP_02
  * TLS_B2_GP_03_T
  * TLS_B2_GP_04
  * TLS_B2_GP_05_T
  * TLS_B2_GP_08


# Changelog for Version 0.5.2
## This is fixed
### Testbed fixes
* Fix Nullpointer Exception during the build.
* Remove duplicated test testMICSParser.

## This is new
### Testbed improvements
* Update documentation
* Update Recommended and Mandatory profiles for TR-03130-1-AM-TLS-EID-SERVER-ECARD-API-INTERFACE-PSK/NONPSK

# Changelog for Version 0.5.1

## This is fixed

### Testbed fixes
* Remove unused/unnecessary Dockerfile.
* Fix documentation in ExampleGlobalConfig
* Minor changes in README
* RMIURL and RMIPort are not written into every TRP anymore
* Exemplary MICS and TRP files have been made more generic
* Fixed a timing bug, which may have resulted in the Application Specific Inspection Instructions of the eID-Client Interfaces not being tested correctly.
* Fixed a bug regarding the TLS-2 connection of an eID-Client.
* For negative Testcases in A1, it is additionally checked if the handshake successful message was not logged
* For session resumption testcases TLS_A1_FR_10 and TLS_A1_FR_11, after the initial handshake it is now correctly checked if the session is closed successfully
* TLS_B1_FR_11 and TLS_B2_FR_10 are now only executed for TLS 1.2


# Changelog for Version 0.5.0

## This is new


### Testbed improvements
* Global configuration value of 'tls_test_tool_path' can now be provided as a relative path.
* Improved eID-Client motivator by using the Browsersimulator
* eID-Server support in the Testcase Executor
* eID-Server support in TLS Test Tool

### Created documents
* example MICS and TRP for eID Server


### Testcases Implemented for eID-server

* Module B1-FR
  * TLS_B1_FR_15
  * TLS_B1_FR_16
  * TLS_B1_FR_17
* Module B2-FR
  * TLS_B2_FR_01_T
  * TLS_B2_FR_11
  * TLS_B2_FR_14
  * TLS_B2_FR_15
  * TLS_B2_FR_16
* Module B2-GP
  * TLS_B2_GP_01_T
  * TLS_B2_GP_02
  * TLS_B2_GP_03_T
  * TLS_B2_GP_04
  * TLS_B2_GP_05_T
  * TLS_B2_GP_08
  * TLS_B2_GP_10


### Testcases Updated for eID-server

* Module B1-GP
  * TLS_B1_GP_01_T
  * TLS_B1_GP_02
  * TLS_B1_GP_03_T
  * TLS_B1_GP_04
  * TLS_B1_GP_05_T
  * TLS_B1_GP_06
  * TLS_B1_GP_07_T


### Orphaned Testcases Implemented

* Module B1-GP
  * TLS_B1_GP_06
* Module B1-FR
  * TLS_B1_FR_10_T
  * TLS_B1_FR_14
* Module B2-GP
  * TLS_B2_GP_06
  * TLS_B2_GP_07_T
* Module B2-FR
  * TLS_B2_FR_04
  * TLS_B2_FR_06
  * TLS_B2_FR_07
  * TLS_B2_FR_08


### Testcases updated for TLS 1.3

* Module B1-FR
  * TLS_B1_FR_03
  * TLS_B1_FR_04
  * TLS_B1_FR_06
  * TLS_B1_FR_07
  * TLS_B1_FR_08
  * TLS_B1_FR_10_T
  * TLS_B1_FR_11
  * TLS_B1_FR_13
  * TLS_B1_FR_16
* Module B1-GP
  * TLS_B1_GP_01_T
  * TLS_B1_GP_02
  * TLS_B1_GP_03_T
  * TLS_B1_GP_04
  * TLS_B1_GP_05
  * TLS_B1_GP_06
  * TLS_B1_GP_07_T
* Module B2-FR
  * TLS_B2_FR_03
  * TLS_B2_FR_04
  * TLS_B2_FR_06
  * TLS_B2_FR_07
  * TLS_B2_FR_08
  * TLS_B2_FR_10
  * TLS_B2_FR_11
  * TLS_B2_FR_13
  * TLS_B2_FR_16
* Module B2-GP
  * TLS_B2_GP_01_T
  * TLS_B2_GP_02
  * TLS_B2_GP_03_T
  * TLS_B2_GP_04
  * TLS_B2_GP_05
  * TLS_B2_GP_06
  * TLS_B2_GP_07_T

# Changelog for Version 0.4.2

## This is fixed
### Testbed fixes
* Examplary TestRunplan files for eID-Client TLS-1-2 and TLS-2 channels now correspond to examplary MICS files.
### Testcase fixes
* Fixed testcase TLS_A1_GP_06_T


# Changelog for Version 0.4.1

## This is fixed

### Testbed fixes
* Apache FOP dependency updated to (CVE free) Version 2.8
* Integrated previously disabled JUnit tests for the MICSVerifier.
* Fixed the retrieval of the OpenAPI Specification via "GET /" REST command.
* Added "Access-Control-Allow-Origin" Header to REST responses.
* Fixed some inconsistencies in the OpenAPI specification.
* Fixed some issues with the REST responses which led to display problems using Swagger UI.
* Status "Aborted" is now correctly returned when the execution of a test run was aborted (due to e.g. the server shutting down)
* The test profile "PFS" is now disregarded in examplary MICS for eID Clients TLS-2 interface.
* Fixed an issue with ciphersuite priorities in the XML specification of TR-03116-4.
* Updated the exemplary MICS files for eID Client TLS-1-2 and eID Client TLS-2.
* Fixed an issue, where all test cases would fail if "SupportedGroups" in MICS are empty.
* Fixed: Elliptic curves should not be mandatory for eID-Client TLS-2 interface specification.
* Fixed: Hash algorithm ecdsaWithSHA256 should not be mandatory for eID-Client TLS-2 interface specification. 
### Documentation fixes 
* Fixed some documentation issues regarding the REST API.
* Fixed references to exemplary MICS files.
### Testcase fixes
* Fixed testcase TLS_A1_GP_06_T


# Changelog for Version 0.4.0
## This is new

### Implemented test cases
#### eID Client Test Cases
* Module A1 - FR
  * TLS_A1_FR_04_T
  * TLS_A1_FR_12_T
* Module A1 - CH
  * TLS_A1_CH_01
  * TLS_A1_CH_02
  * TLS_A1_CH_03
  * TLS_A1_CH_04
  * TLS_A1_CH_06_T
  * TLS_A1_CH_07
  * TLS_A1_CH_08
  * TLS_A1_CH_09
#### Orphaned Test Cases
* Module A1 - GP
  * TLS_A1_GP_07_T
* Module A1 - FR
  * TLS_A1_FR_13
* Module A1 - CH
  * TLS_A1_CH_05

### XML configuration files
* Additional example MICS files for the TLS-1-2 and TLS-2 channel of the eID-Client were added.
* Additional application type information is provided in the MICS and test case run plan (TRP) files. Example files have been updated.<br>
  ***NOTE: Old MICS and TRP files might not work with the new version of TaSK!<br>
  Please update your MICS or TRP files according to the updated examples.***
* New options were introduced in the global configuration. <br>
  ***NOTE: Old GlobalConfig.xml might not work with the new version of TaSK!<br>
  Please create a new one according to ExampleGlobalConfig.xml***

### Testbed improvements
* Implementation of an REST-API for TLS server test runs.
* Split the execution of a DUT client executable from the test case logic. 
  This allows to reuse the same test cases for different DUT application types.
* Added a feature to execute an eID-Client TLS-1-2 channel DUT in test cases.
* Added a feature to execute an eID-Client TLS-2 channel DUT in test cases.

## This is fixed

### Testbed fixes
* Previously disabled SpotBugs warnings have been fixed and enabled again.
### Documentation
* The PDF Report is now structured more clearly.

# Changelog for Version 0.3.0

## This is new

#### Implemented test cases 
  * Module A1 - FR
    * TLS_A1_FR_01
    * TLS_A1_FR_02
    * TLS_A1_FR_03
    * TLS_A1_FR_06
    * TLS_A1_FR_07
    * TLS_A1_FR_08
    * TLS_A1_FR_09
    * TLS_A1_FR_10
    * TLS_A1_FR_11
  * Module A1 - GP
    * TLS_A1_GP_01_T
    * TLS_A1_GP_02_T
    * TLS_A1_GP_03_T
    * TLS_A1_GP_04
    * TLS_A1_GP_05
    * TLS_A1_GP_06_T
  * Module B1 - FR
    * TLS_B1_FR_13  

#### XML configuration files
* An additional example MICS file for a TLS client test was added
* New options were introduced in the global configuration. <br>
  ***NOTE: Old GlobalConfig.xml will not work with the new version of TaSK!<br>
  Please create a new one according to ExampleGlobalConfig.xml***
* The schema of test run plan files was improved. <br>
  ***NOTE: Old test run plans will not work with the new version of TaSK!***

#### Testbed Improvements
* The XML Parser can read the XML files for the TLS client application specific profile
* Reports are now written to an XML structure
* A PDF version of the report can be generated.
* A motivator automatically launches a TLS client application within the test framework
* TaSK tries to create the configured reports-directory if it is missing.
  Furthermore, there is now a sub-folder for the reports of each test run.
* Added more validity checks to the input of the command line interface.


## This was fixed

#### Test cases
* TLS_B1_FR_09 fixed in the TLS Test Tool
* TLS_B1_GP_07_T fixed to use PFS cipher suites

#### Documentation
* Enhanced TaSK documentation in the README.md
  * added TaSK concept description and diagram
  * removed long xml examples<br>
    the xml example files in the data folder have more documentation now
  * fixed the links and headings
  * updated required maven version to 3.8.5
  * updated several descriptions and wordings
  * added an explanation on the generation of JavaDocs
* Improved the documentation in the example xml files
* Formatting in the README.md of TLS Test Tool is more readable
* Added this `changelog.md`
* Improvement of JavaDocs.

