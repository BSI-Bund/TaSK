package com.achelos.task.dutpreparation;

public class HandshakePreparationInfo {
    private final String hostName;
    private final int port;
    private final String psk;
    private final String pskIdentity;

    public HandshakePreparationInfo(final String hostName, final int port, final String psk, final String pskIdentity) {
        this.hostName = hostName;
        this.port = port;
        this.psk = psk;
        this.pskIdentity = pskIdentity;
    }

    /**
     * Returns the stored hostname.
     * @return Hostname
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the stored port.
     * @return Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the stored PSK.
     * @return PSK
     */
    public String getPsk() {
        return psk;
    }

    /**
     * Returns the stored PSKIdentity.
     * @return PSKIdentity
     */
    public String getPskIdentity() {
        return pskIdentity;
    }
}
