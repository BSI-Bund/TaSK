package com.achelos.task.dutmotivator.email;

import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;

public class DNSServerContainer {

    private final String containerIP;
    private final String taskHostIP;
    private static final String taskZone = "task.";
    private static final String taskHostName = "tlstest.task.";
    private static final String tlsTestToolPortAndProtocol = "_25._tcp.";
    private static final String updateKeyName = "dns-updater.key.";
    private static final String updateKey = "O/vKb6ScuOjIKF5HGzczKA==";
    private static final int timetolive = 100; // It might be necessary to set this to a lower value. Maybe 1 is possible?

    public DNSServerContainer(final String dNSServercontainerIP, final String taskHostIP) throws IOException {

        // Set the Container IP
        containerIP = dNSServercontainerIP;
        this.taskHostIP = taskHostIP;
        // Update A Record for tlstest.task
        updateARecord();

    }

    /**
     * Update the DNS Server TLSTestTool ARecord entry.
     * @throws IOException If an error occurs when updating the DNS Server Entries.
     */
    private void updateARecord() throws IOException {
        // create a dynamic update message
        Update update = new Update(Name.fromString(taskZone));

        // Delete old record.
        update.delete(Name.fromString(taskHostName));
        // Add a new A record for the hostname with the specified IP address and TTL
        update.add(new ARecord(Name.fromString(taskHostName), DClass.IN, timetolive, InetAddress.getByName(taskHostIP)));
        update.delete(Name.fromString("a.root-dns-servers.task."));
        update.add(new ARecord(Name.fromString("a.root-dns-servers.task."), DClass.IN, timetolive, InetAddress.getByName(taskHostIP)));

        // Send update to DNS Server
        updateDNSServer(update);

    }

    /**
     * Update the DNS Server TLSTestTool MX Record entry.
     * @throws IOException If an error occurs when updating the DNS Server Entries.
     */
    private void updateMXRecord() throws IOException {
        // create a dynamic update message
        var update = new Update(Name.fromString(taskZone));

        MXRecord mxRecord = new MXRecord(Name.fromString(taskHostName), DClass.IN, timetolive, 10, Name.fromString(taskHostName));

        // create the update request
        update.add(mxRecord);
        // Send update to DNS Server
        updateDNSServer(update);
    }

    /**
     * Update the DNS Server TLSTestTool entry.
     * @param certificateAssociationData The new certificate association data to be set. If empty or null, just deletes the current entry.
     * @throws IOException If an error occurs when updating the DNS Server Entries.
     */
    public void updateTLSARecord(final byte[] certificateAssociationData) throws IOException {
        // create a dynamic update message
        Update update = new Update(Name.fromString(taskZone));

        // Name
        var serviceName = Name.fromString(tlsTestToolPortAndProtocol + taskHostName);

        // Delete old record.
        update.delete(serviceName);
        if (certificateAssociationData != null && certificateAssociationData.length != 0) {
            // Add a new A record for the hostname with the specified IP address and TTL
            update.add(new TLSARecord(serviceName, DClass.IN, timetolive, TLSARecord.CertificateUsage.DOMAIN_ISSUED_CERTIFICATE, TLSARecord.Selector.FULL_CERTIFICATE, TLSARecord.MatchingType.SHA256, certificateAssociationData));
        }
        updateDNSServer(update);
    }

    /**
     * Update the DNS Server TLSTestTool entry.
     * @throws IOException If an error occurs when updating the DNS Server Entries.
     */
    private void updateDNSServer(Update update) throws IOException {
        // sign the update message using the specified key
        var tsig = new TSIG(TSIG.HMAC_MD5, updateKeyName, updateKey);
        update.setTSIG(tsig);
        // send the update message to the DNS server
        SimpleResolver resolver = new SimpleResolver(containerIP);
        resolver.setTSIGKey(tsig);
        resolver.setTCP(true);
        Message response = resolver.send(update);

        // check if the update was successful
        if (response.getRcode() != Rcode.NOERROR) {
            throw new RuntimeException("DNS Update failed with error " + response.getRcode());
        }
    }

}
