# Mail Client Connection Executor

If TaSK shall test a DUT of type `TR-03108-1-EMSP-CLIENT-CETI-NO-DANE`, the DUT needs to be motivated to send an e-mail to TaSK.
As this mechanism is proprietary, the tester has to provide an adapter as RMI-Service.

This MailClientConnectionExecutor is a documented exemplary implementation as a basis for a specific motivator.


## 1 Function

A MailClientConnectionExecutor must implement the interface `com.achelos.task.rmi.clientexecution.MailClientConnectionExecutor` from the module `com.achelos.task.configuration`.

The interface defines one method:

- **sendEmailToTask(String receivingEMailAddress)**  
Sends an E-Mail to an address on which the TaSK Framework is listening via the DUT E-Mail Trsp.
Returns logs to the TaSK Framework.


## 2 Build

If the MailClientConnectionExecutor and TaSK are running on different machines, update the `RMI_IP` address in the `RMIServer.java` class with the `real` IP address of the machine the RMI Server is running on. In the case of multiple network interface controllers make sure the IP address belongs to the correct interface.

Navigate to the folder with the pom.xml and execute  

    $ mvn clean package

This will build `MailClientConnectionExecutor.jar` in the `target` folder.

### System Requirements for Building

- Operating System: x86-64 Linux Distribution (e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop))
- Java JDK 17 or newer (e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/))
- Apache Maven 3.8.5 or newer ([Apache Maven](https://maven.apache.org/))


## 3 Execution

### Mail Client Connection Executor

A name resolution for the name `tlstest.task` has to be added to the DNS infrastructure for the DUT E-Mail Trsp. Client. This might be either a DNS entry pointing to the TaSK host ip or an entry in the local hostfile of the DUT.

Start the RMI-Service:  

    $ java -jar MailClientConnectionExecutor.jar

### TaSK

1. Set the GlobalConfiguration Parameter `tls_test_tool_port` to the port, which is used by the DUT to deliver e-mails (usually port `25`).
2. Create a MICS for your DUT, based on `ExampleMICS_EMail-Client-CETI-NODANE.xml`.
3. Start `com.achelos.task.commandlineexecution.jar` with this MICS. *A respective TRP can also be used.*


## 4 License

This code is part of TaSK, which is licensed under the EUPL-1.2-or-later.  
For more information on the license, see the included LICENSE.md or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
