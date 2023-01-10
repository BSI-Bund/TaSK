package com.achelos.task.dutcommandgenerators;

import com.achelos.task.configuration.TestRunPlanConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

public class EIDClientTls12DUTCommandGenerator implements DUTCommandGenerator {

    private static final String EXECUTE_SCRIPT_RESOURCE_PATH = "connect_eid_client_tls12.py";
    private final Path script;
    private final String eIDClientExecutable;
    private final Integer eIDClientPort;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;

    public EIDClientTls12DUTCommandGenerator(final TestRunPlanConfiguration configuration) throws Exception {

        // Execute Script
        script = Files.createTempFile(null, ".py");
        try (InputStream embeddedScript
                     = this.getClass().getClassLoader().getResourceAsStream(EXECUTE_SCRIPT_RESOURCE_PATH)) {
            Files.copy(embeddedScript, script, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr--"));
        eIDClientExecutable = configuration.getDUTExecutable();
        eIDClientPort = configuration.getDutEIDClientPort();

        tlsTestToolPort = configuration.getTlsTestToolPort();
        tlsTestToolHostName = "localhost";
    }



    private List<String> callExecutionScript() {
        final ArrayList<String> command = new ArrayList<>();
        // Script File Path
        command.add("python3");
        command.add(script.toAbsolutePath().toString());

        // eID-Client Executable
        command.add("-eid_client");
        command.add(eIDClientExecutable);
        // eID-Client Port
        command.add("-eid_client_port");
        command.add(Integer.toString(eIDClientPort));
        // eID-Server Hostname
        command.add("-eservice_hostname");
        command.add(tlsTestToolHostName);
        // eID-Server Port
        command.add("-eservice_port");
        command.add(Integer.toString(tlsTestToolPort));

        return command;
    }

    @Override
    public List<String> connectToServer(final boolean isSessionResumption) {
        // Ignore isSessionResumption.
        return this.callExecutionScript();
    }
}
