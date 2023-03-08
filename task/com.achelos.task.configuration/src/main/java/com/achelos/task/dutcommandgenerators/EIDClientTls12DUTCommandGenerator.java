package com.achelos.task.dutcommandgenerators;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.utilities.logging.LogBean;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EIDClientTls12DUTCommandGenerator implements DUTCommandGenerator {

    private static final String EXECUTE_SCRIPT_RESOURCE_PATH = "connect_eid_client_tls12.py";
    private static final String BROWSERSIMULATOR_CLIENT_PATH = "browsersimulatorclient.jar";
    private static final String APP_SPEC_CHECK_REGEX = "^HTTP message from eID-Client: statusCode:\\s*?(\\d+)\\s*?$";
    private final Path script;
    private final Integer eIDClientPort;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;
    private final Path browsersimClientJar;
    private final String browsersimHostname;
    private final String browsersimPort;
    

    public EIDClientTls12DUTCommandGenerator(final TestRunPlanConfiguration configuration) throws Exception {

        // Execute Script
        script = Files.createTempFile(null, ".py");
        try (InputStream embeddedScript
                     = this.getClass().getClassLoader().getResourceAsStream(EXECUTE_SCRIPT_RESOURCE_PATH)) {
            Files.copy(embeddedScript, script, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr--"));

        eIDClientPort = configuration.getDutEIDClientPort();

        browsersimHostname = configuration.getBrowserSimulatorURL();
        browsersimPort = configuration.getBrowserSimulatorPort();
        browsersimClientJar = Files.createTempFile("browsersimulator", ".jar");
        try (InputStream browsersimExecutable
	                = this.getClass().getClassLoader().getResourceAsStream(BROWSERSIMULATOR_CLIENT_PATH)) {
	        Files.copy(browsersimExecutable, browsersimClientJar, StandardCopyOption.REPLACE_EXISTING);
	    }

        tlsTestToolHostName = configuration.getTaSKServerAddress();
        tlsTestToolPort = configuration.getTlsTestToolPort();
    }



    private List<String> callExecutionScript() {
        final ArrayList<String> command = new ArrayList<>();
        // Script File Path
        command.add("python3");
        command.add(script.toAbsolutePath().toString());

        // eID-Client Port
        command.add("-eid_client_port");
        command.add(Integer.toString(eIDClientPort));
        // eID-Server Hostname
        command.add("-eservice_hostname");
        command.add(tlsTestToolHostName);
        // eID-Server Port
        command.add("-eservice_port");
        command.add(Integer.toString(tlsTestToolPort));
        // browsersimulator client executable
        command.add("-browsersimclient");
        command.add(browsersimClientJar.toAbsolutePath().toString());
        // browsersimulator hostname
        command.add("-browsersim_hostname");
        command.add(browsersimHostname);
        // browsersimulator port
        command.add("-browsersim_port");
        command.add(browsersimPort);

        return command;
    }

    @Override
    public List<String> connectToServer(final boolean isSessionResumption) {
        // Ignore isSessionResumption.
        return this.callExecutionScript();
    }

    @Override
    public String applicationSpecificInspectionSearchString(boolean handshakeSuccessful) {
        if (!handshakeSuccessful) {
            return APP_SPEC_CHECK_REGEX;
        }
        return null;
    }

    @Override
    public boolean applicationSpecificInspection(boolean handshakeSuccessful, LogBean logBean) {
        if (handshakeSuccessful) {
            return true;
        }
        if (logBean == null) {
            return false;
        }
        var pattern = Pattern.compile(APP_SPEC_CHECK_REGEX);
        var matcher = pattern.matcher(logBean.getMessage());
        if (matcher.find()) {
            // Check that the status is 404
            return (Integer.parseInt(matcher.group(1)) == 404);
        } else {
            return false;
        }
    }
}
