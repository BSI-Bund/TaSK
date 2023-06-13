# Client Executor

If TaSK shall test a DUT of type `TR-03116-4-CLIENT`, the DUT needs to be motivated to initiate a TLS connection to a server.
As this mechanism is proprietary, the tester has to provide an adapter as RMI-Service.

This ClientExecutor is a documented exemplary implementation, which can be used as a basis for a specific motivator.


## 1 Function

A ClientExecutor must implement the interface `com.achelos.task.rmi.clientexecution.ClientExecutor` from the module `com.achelos.task.configuration`.

The interface defines one method:

- **connectToServer(hostname, port, useSessionResumption)**
Initiates a TLS connection to the server denoted by the params hostname and port.
Uses session resumption if the flag is set.  
Returns logs to the TaSK Framework.


## 2 Build

If the ClientExecutor and TaSK are running on different machines, update the `RMI_IP` address in the `RMIServer.java` class with the `real` IP address of the machine the RMI Server is running on. In the case of multiple network interface controllers make sure the IP address belongs to the correct interface.

Navigate to the folder with the pom.xml and execute  

    $ mvn clean package

This will build `ClientExecutor.jar` in the `target` folder.

### System Requirements for Building

- Operating System: x86-64 Linux Distribution (e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop))
- Java JDK 17 or newer (e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/))
- Apache Maven 3.8.5 or newer ([Apache Maven](https://maven.apache.org/))


## 3 Execution

### Client Executor

Start the RMI-Service and pass the necessary parameters:

    $ java -jar ClientExecutor.jar <options>

* `-c,--clientCommand <arg>`
_mandatory_
command to start client
* `-k,--clientKey <arg>`
_optional_
path to client certificate key
* `-r,--resumptionClientCommand <arg>`
_optional_
resumption command to start client
* `-w,--workingDirectory <arg>`
_mandatory_
directory where client will be started
* `-x,--clientCert <arg>`
_optional_
path to client certificate

### TaSK

1. Create a MICS for your DUT, based on `ExampleMICS_Client.xml`.
2. Start `com.achelos.task.commandlineexecution.jar` with this MICS. *A respective TRP can also be used.*


## 4 License

This code is part of TaSK, which is licensed under the EUPL-1.2-or-later.  
For more information on the license, see the included LICENSE.md or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
