package com.achelos.task.dutmotivator.eidclient;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutmotivator.DUTMotivator;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.bind.DatatypeConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class EIDClientTls2DUTMotivator implements DUTMotivator {

    private static final String URL_TEMPLATE = "http://127.0.0.1:%d/eID-Client?tcTokenURL=https://%s:%d";
    private static final String CERT_GEN_SCRIPT_RESOURCE_PATH = "eid_client_generate_eservice_certificates.sh";
    private static final String CERT_GEN_CONFIG_RESOURCE_PATH = "eid_client_eservice_certificate.cnf";
    private static final String SERVICE_CERT_REL_PATH = "certificateRsa/root-ca/certs/root-ca.pem";
    private static final String SERVICE_KEY_REL_PATH = "certificateRsa/root-ca/private/root-ca.pem";
    private static final String APP_SPEC_CHECK_REGEX = "^HTTP message from eID-Client: statusCode:\\s*?(\\d+)\\s*?$";
    private static final String APP_SPEC_CHECK_LOCATION_REGEX = "^\\s*?Location:\\s*?(\\S+)\\s*?$";
    private static final String TC_TOKEN_REFRESH_ADDRESS = "https://www.bsi.bund.de";

    // For now this is a fixed port. Possibly this should be an optional config parameter in the GlobalConfigs.
    private static final Integer MOCK_ESERVICE_PORT = 8447;
    private File serverKeyPath;
    private File serverCertPath;
    private final Integer eIDClientPort;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;
    private final byte[] psk;

    private final BrowserSimulatorRmiImpl browserSim;

    public EIDClientTls2DUTMotivator(final TestRunPlanConfiguration configuration) throws Exception {

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

        executeCertGeneration(genScript, genConfig, configuration);

        this.eIDClientPort = configuration.getDutEIDClientPort();

        tlsTestToolHostName = configuration.getTaSKServerAddress();
        this.tlsTestToolPort = configuration.getTlsTestToolPort();
        this.psk = configuration.getPSKValue();

        var browserSimHostname = configuration.getDutRMIURL();
        var browserSimPort = configuration.getDutRMIPort();

        browserSim = new BrowserSimulatorRmiImpl(browserSimHostname, Integer.parseInt(browserSimPort));

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




    @Override
    public List<String> motivateConnectionToTaSK(final boolean isSessionResumption){
        var motivatorLogs = new LinkedList<String>();
        HttpsServer mockEService = null;
        try {
            mockEService = createHTTPSServer(tlsTestToolHostName, tlsTestToolPort);
            if (mockEService == null) {
                throw new RuntimeException("Unable to create eService Mock Server.");
            }
            mockEService.start();

            // Ignore isSessionResumption, as there is no way to trigger anything else on the eID Client.
            String url = String.format(URL_TEMPLATE, eIDClientPort, tlsTestToolHostName, MOCK_ESERVICE_PORT);
            motivatorLogs.add("Sending URL " + url + " to BrowserSimulator.");
            motivatorLogs.addAll(connectEIDClient(url));

            mockEService.stop(1);
        } catch (Exception e) {
            motivatorLogs.add("Error when trying to motivate eIDClient: " + e.getMessage());
        } finally {
            if (mockEService != null) {
                try {
                    mockEService.stop(3);
                } catch (Exception ignored) {
                }
            }
        }
        return new ArrayList<>(motivatorLogs);
    }

    @Override
    public boolean checkApplicationSpecificInspections(boolean handshakeSuccessful, List<String> logs) {
        if (handshakeSuccessful) {
            return true;
        } else if (logs == null) {
            return false;
        }
        var wasRedirect = false;
        // Search the given message in the provided log list
        var pattern = Pattern.compile(APP_SPEC_CHECK_REGEX);
        for (final String log : logs) {
            var matcher = pattern.matcher(log);
            if (matcher.find()) {
                // Check that the status is a redirect
                wasRedirect = ((Integer.parseInt(matcher.group(1)) % 100) == 3);
                break;
            }
        }
        if (!wasRedirect) {
            return false;
        }
        pattern = Pattern.compile(APP_SPEC_CHECK_LOCATION_REGEX);
        for (final String log : logs) {
            var matcher = pattern.matcher(log);
            if (matcher.find()) {
                // Check that the Location Header is correct.
                return matcher.group(1).startsWith(TC_TOKEN_REFRESH_ADDRESS);
            }
        }
        return false;
    }

    @Override
    public void finalizeRMI() {
        browserSim.finalizeRMI();
    }

    private List<String> connectEIDClient(final String url) {
        var browserSimLogs = new LinkedList<String>();
        try {
            browserSimLogs.add(browserSim.startApp());
            Thread.sleep(3 * 1000);
            browserSimLogs.addAll(browserSim.sendHttpRequest(url, null, false));
            Thread.sleep(3 * 1000);
            browserSimLogs.addAll(browserSim.stopApp());
        } catch (Exception e) {
            browserSimLogs.add("Unable to connectEIDClient: " + e.getMessage());
        }
        return browserSimLogs;
    }

    private HttpsServer createHTTPSServer(final String eidServerHostname,final Integer eidServerPort) throws Exception{
        // Configure SSL

        SSLContext sslContext = buildSSLContext(serverCertPath.getAbsolutePath(), serverKeyPath.getAbsolutePath());
        if (sslContext == null) {
            return null;
        }
        // Create HTTPS server
        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(MOCK_ESERVICE_PORT), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        // Set HTTP handler
        httpsServer.createContext("/", exchange -> {
            String response = "<TCTokenType>\n" +
                    "        <ServerAddress>https://" + eidServerHostname + ":" + eidServerPort + "</ServerAddress>\n" +
                    "        <SessionIdentifier>Client_identity</SessionIdentifier>\n" +
                    "        <RefreshAddress>"+ TC_TOKEN_REFRESH_ADDRESS + "</RefreshAddress>\n" +
                    "        <CommunicationErrorAddress>"+ TC_TOKEN_REFRESH_ADDRESS + "</CommunicationErrorAddress>\n" +
                    "        <Binding>urn:liberty:paos:2006-08</Binding>\n" +
                    "        <PathSecurity-Protocol>urn:ietf:rfc:4279</PathSecurity-Protocol>\n" +
                    "        <PathSecurity-Parameters>\n" +
                    "            <PSK>" + DatatypeConverter.printHexBinary(psk) + "</PSK>\n" +
                    "</PathSecurity-Parameters>" +
                    "</TCTokenType>";
            exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            exchange.getResponseHeaders().add("Content-Length", Integer.toString(response.length()));
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        });
        return httpsServer;
    }


    private static SSLContext buildSSLContext(String certPath, String keyPath) {
        InputStream certFileStream = null;
        try {
            var password = "spanishinquisition";

            certFileStream = new FileInputStream(certPath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(certFileStream);
            certFileStream.close();

            PemReader pemReader = new PemReader(new FileReader(keyPath, StandardCharsets.UTF_8));
            PemObject pemObject = pemReader.readPemObject();
            byte[] privateKeyBytes = pemObject.getContent();
            pemReader.close();

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("cert", cert);
            keyStore.setKeyEntry("key", privateKey, password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagerFactory.getTrustManagers(), null);

            return sslContext;
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (certFileStream != null) {
                try {
                    certFileStream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
