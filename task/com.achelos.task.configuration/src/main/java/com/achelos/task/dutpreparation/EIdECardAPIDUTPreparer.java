package com.achelos.task.dutpreparation;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.rmi.tctokenprovider.TCTokenURLProvider;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;

public class EIdECardAPIDUTPreparer implements DUTPreparer {
    private final LoggingConnector logger;
    private final String rmiHostName;
    private final int rmiPort;
    public EIdECardAPIDUTPreparer(final String rmiHostName, final int rmiPort) {
        this.logger = LoggingConnector.getInstance();
        this.rmiHostName = rmiHostName;
        this.rmiPort = rmiPort;
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
    }
    @Override
    public HandshakePreparationInfo prepareHandshake() {
        try {
            var tcTokenURL = new URL(requestTCTokenURL());
            HttpURLConnection con = (HttpURLConnection) tcTokenURL.openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(true);

            int statusCode = con.getResponseCode();
            logger.debug("Get TCToken Status Code: " + statusCode);

            // Fetch content
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            var tcTokenContent = content.toString();

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringBufferInputStream(tcTokenContent));
            Document doc = builder.parse(is);
            var serverAddress= doc.getElementsByTagName("ServerAddress").item(0).getTextContent();
            URL url = new URL(serverAddress);
            String hostname = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            var sessionIdentifier = doc.getElementsByTagName("SessionIdentifier").item(0).getTextContent();
            var psk = doc.getElementsByTagName("PSK").item(0).getTextContent();
            return new HandshakePreparationInfo(hostname, port, psk, sessionIdentifier);

        } catch (Exception e) {
            throw new RuntimeException("Error preparing handshake.", e);
        }

    }

    private String requestTCTokenURL() {
        try {
            // Look up the remote object
            TCTokenURLProvider remoteTCTokenURLProvider = (TCTokenURLProvider) Naming.lookup("//" + rmiHostName+ ":"
                    + rmiPort + "/" + TCTokenURLProvider.SERVICE_NAME);

            // Call the remote method
            return remoteTCTokenURLProvider.retrieveTCTokenURL();

        } catch (Exception e) {
            throw new RuntimeException("Unable to prepare DUT for handshake.", e);
        }
    }
}
