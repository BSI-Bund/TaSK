# TCToken URL Provider

If TaSK shall test a DUT of type `TR-03130-1-EID-SERVER-ECARD-PSK`, a corresponding eService simulation is required and has to be deployed in addition to the eID-Server.
The TLS testbed does not provide an implementation of this eService simulation, i.e. the operator of the TLS testbed must ensure that this component is given.  
The eService simulation will be initiated by the TLS testbed, more precisely by a special RMI component, the TCTokenURLProvider.
During this process, the RMI component communicates with the eService simulation to initiate the online authentication request and to retrieve the TCTokenURL.  
The TCTokenURL presents the URL under which the TC Token for the eID-Server connection can be obtained.
Because the implementations of the eService simulation may differ regarding their structure, customizations must be made to the RMI component.

This TCTokenURLProvider is a documented exemplary implementation as a basis for a specific motivator.


## 1 Function

A TCTokenURLProvider must implement the interface `com.achelos.task.rmi.tctokenprovider.TCTokenURLProvider` from the module `com.achelos.task.configuration`.

The interface defines one method:

- **retrieveTCTokenURL()**  
Initiates the Online-Authentication Request on the eID-Server (e.g. using a useIDRequest message) and returns a TCTokenURL to the TaSK Framework, which an eID-Client could use to retrieve a TCToken Object (as specified in TR-03130).


## 2 Build

If the TCTokenURLProvider and TaSK are running on different machines, update the `RMI_IP` address in the `RMIServer.java` class with the `real` IP address of the machine the RMI Server is running on. In the case of multiple network interface controllers make sure the IP address belongs to the correct interface.

Navigate to the folder with the pom.xml and execute  

    $ mvn clean package

This will build `TCTokenURLProvider.jar` in the `target` folder.

### 2.1 System Requirements for Building

- Operating System: x86-64 Linux Distribution (e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop))
- Java JDK 17 or newer (e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/))
- Apache Maven 3.8.5 or newer ([Apache Maven](https://maven.apache.org/))


## 3 Execution

### TCTokenURLProvider

Start the RMI-Service:

    $ java -jar TCTokenURLProvider.jar

### TaSK

1. Create a MICS for your DUT, based on `ExampleMICS_eID-Server-eCard-API-PSK.xml`.
2. Start `com.achelos.task.commandlineexecution.jar` with this MICS. *A respective TRP can also be used.*


## 4 License

The TCTokenURLProvider example is part of TaSK, which is licensed under the EUPL-1.2-or-later.  
For more information on the license, see the included LICENSE.md or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
