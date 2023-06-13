# Browser Simulator

If TaSK shall test a DUT of type `TR-03124-1-EID-CLIENT-TLS-1-2` or `TR-03124-1-EID-CLIENT-TLS-2`, the DUT needs to be motivated to connect to the server. The BrowserSimulator can start the DUT, trigger the connection attempt and close the DUT.

This is a fully functional Implementation of the BrowserSimulator interface, which can be used as is. This BrowserSimulator enhances the IPSmallBrowserSimulator that comes with the eID-ClientTestbed from BSI (see <https://github.com/eID-Testbeds/client>).


## 1 Function

A BrowserSimulator must implement the interface `com.secunet.ipsmall.rmi.IBrowserSimulator` from the module `com.achelos.task.configuration`.

The interface defines three methods:

- **startApp()**  
Executes the app (DUT) with the command that was given as startup parameter.
- **sendRequest(url)**  
The url addresses the webservice port of the eID-Client and triggers the client to request the tcToken.  
For example, the AusweisApp2 needs this format:  
`http://127.0.0.1:24727/eID-Client?tcTokenURL=https://{eservice_hostname}:{eservice_port}`
- **stopApp()**  
Kills the app process and returns log entries from the DUT if available.


## 2 Build

Navigate to the folder with the pom.xml and execute  

    $ mvn clean install

The artifacts are copied to `target/browsersimulator` and packed as zip archive during the build process.

### System Requirements for Building

- Operating System: x86-64 Linux Distribution (e.g. [Ubuntu 20.04](https://ubuntu.com/download/desktop))
- Java JDK 17 or newer (e.g. [OpenJDK 17](https://openjdk.java.net/projects/jdk/17/))
- Apache Maven 3.8.5 or newer ([Apache Maven](https://maven.apache.org/))


## 3 Execution

#### Browser Simulator

Add an entry to your `hosts` file, mapping `tlstest.task` to the ip address of the TaSK framework. If the BrowserSimulator and TaSK are running on the same machine, this may be `localhost` or `127.0.0.1`.

Start the RMI-Server with parameters:

    $ java -jar browsersimulator.jar <IP address> <DUT executable>

- **IP address**  
Local ip address or hostname which will be used to configure the bind address for the RMI service.
If the BrowserSimulator and TaSK are running on the same machine, this may be `localhost` or `127.0.0.1`.
If the BrowserSimulator and TaSK are running on different machines, the bind address needs to be the `real` IP address of the machine the RMI Server is running on. In the case of multiple network interface controllers make sure the IP address belongs to the correct interface.

- **DUT executable**  
The path to the DUT (eID-Client), which shall be executed for each test case.

You can use `run_windows.bat`, `run_linux.sh` or `run_mac.sh`.

#### TaSK

1. Create a MICS for your DUT, based on `ExampleMICS_eID-Client-TLS-1-2.xml` or `ExampleMICS_eID-Client-TLS-2.xml`.
2. Start `com.achelos.task.commandlineexecution.jar` with this MICS. *A respective TRP can also be used.*


## 4 License

The BrowserSimulator 2 is licensed under the EUPL-1.2-or-later.  
For more information on the license, see the included [license text](LICENSE.md) or the according [website of the European Commission](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12).


## 5 Third Party Libraries

All dependencies to third party libraries, their respective websites and licenses are listed in [3rdPartyLicenses.md](3rdPartyLicenses.md)
