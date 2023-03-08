package com.achelos.task.dutcommandgenerators;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.utilities.logging.LogBean;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EIDClientTls2DUTCommandGenerator implements DUTCommandGenerator {

    private static final String EXECUTE_SCRIPT_RESOURCE_PATH = "connect_eid_client_tls2.py";
    private static final String CERT_GEN_SCRIPT_RESOURCE_PATH = "eid_client_generate_eservice_certificates.sh";
    private static final String CERT_GEN_CONFIG_RESOURCE_PATH = "eid_client_eservice_certificate.cnf";
    private static final String SERVICE_CERT_REL_PATH = "certificateRsa/root-ca/certs/root-ca.pem";
    private static final String SERVICE_KEY_REL_PATH = "certificateRsa/root-ca/private/root-ca.pem";
    private static final String BROWSERSIMULATOR_CLIENT_PATH = "browsersimulatorclient.jar";
    private static final String APP_SPEC_CHECK_REGEX = "^HTTP message from eID-Client: statusCode:\\s*?(\\d+)\\s*?$";
    private final Path script;
    private final Path browsersimClientJar;
    private final String browsersimHostname;
    private final String browsersimPort;
    private File serverKeyPath;
    private File serverCertPath;
    private final Integer eIDClientPort;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;
    private final byte[] psk;

    public EIDClientTls2DUTCommandGenerator(final TestRunPlanConfiguration configuration) throws Exception {

        // Execute Script
        script = Files.createTempFile(null, ".py");
        try (InputStream embeddedScript
                     = this.getClass().getClassLoader().getResourceAsStream(EXECUTE_SCRIPT_RESOURCE_PATH)) {
            Files.copy(embeddedScript, script, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr--"));


        // Certificate Generation Script
        Path genScript = Files.createTempFile(null, ".sh");
        try (InputStream embeddedGenScript
                     = this.getClass().getClassLoader().getResourceAsStream(CERT_GEN_SCRIPT_RESOURCE_PATH)) {
            Files.copy(embeddedGenScript, genScript, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.setPosixFilePermissions(genScript, PosixFilePermissions.fromString("rwxr-xr--"));

        // Certificate Generation Configuration
        Path genConfig = Files.createTempFile(null, ".sh");
        try (InputStream embeddedGenConfig
                     = this.getClass().getClassLoader().getResourceAsStream(CERT_GEN_CONFIG_RESOURCE_PATH)) {
            Files.copy(embeddedGenConfig, genConfig, StandardCopyOption.REPLACE_EXISTING);
        }

        browsersimHostname = configuration.getBrowserSimulatorURL();
        browsersimPort = configuration.getBrowserSimulatorPort();
        browsersimClientJar = Files.createTempFile("browsersimulator", ".jar");
        try (InputStream browsersimExecutable
	                = this.getClass().getClassLoader().getResourceAsStream(BROWSERSIMULATOR_CLIENT_PATH)) {
	        Files.copy(browsersimExecutable, browsersimClientJar, StandardCopyOption.REPLACE_EXISTING);
	    }

        executeCertGeneration(genScript, genConfig, configuration);

        this.eIDClientPort = configuration.getDutEIDClientPort();

        tlsTestToolHostName = configuration.getTaSKServerAddress();
        this.tlsTestToolPort = configuration.getTlsTestToolPort();
        this.psk = configuration.getPSKValue();

    }

    private void executeCertGeneration(final Path generationScript, final Path generationConfig, final TestRunPlanConfiguration configuration)
            throws Exception {

        final ArrayList<String> command = new ArrayList<>();
        // Script File Path
        command.add(generationScript.toAbsolutePath().toString());

        // Certificate base path
        var cert_basepath = configuration.getMotivatorCertDirectory();
        command.add(cert_basepath.getAbsolutePath());

        // Template
        var templatePath = generationConfig.toAbsolutePath().toString();
        command.add(templatePath);

        // OpenSSL Executable Path
        var opensslExecPath = configuration.getOpenSSLExecutable();
        command.add(opensslExecPath);

        // Execute
        Runtime.getRuntime().exec(command.toArray(new String[0]));

        serverCertPath = new File(cert_basepath, SERVICE_CERT_REL_PATH);
        serverKeyPath = new File(cert_basepath, SERVICE_KEY_REL_PATH);
    }


    private List<String> callExecutionScript() {

        if (psk == null || psk.length == 0) {
            throw new RuntimeException("Pre-shared key may not be null or empty with eID-Client TLS-2 channel.");
        }

        final ArrayList<String> command = new ArrayList<>();
        // Script File Path
        command.add("python3");
        command.add(script.toAbsolutePath().toString());

        // eService Mock Certificate
        command.add("-server_cert");
        command.add(serverCertPath.getAbsolutePath());
        // eService Mock Key
        command.add("-server_key");
        command.add(serverKeyPath.getAbsolutePath());
        // eID-Client Port
        command.add("-eid_client_port");
        command.add(Integer.toString(eIDClientPort));
        // eID-Server Hostname
        command.add("-eid_server_hostname");
        command.add(tlsTestToolHostName);
        // eID-Server Port
        command.add("-eid_server_port");
        command.add(Integer.toString(tlsTestToolPort));
        // PSK
        command.add("-psk");
        command.add(DatatypeConverter.printHexBinary(psk));
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
    public List<String> connectToServer(final boolean isSessionResumption){
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
            // Check that the status is a redirect
            return ((Integer.parseInt(matcher.group(1)) % 100) == 3);
        } else {
            return false;
        }
    }
}
