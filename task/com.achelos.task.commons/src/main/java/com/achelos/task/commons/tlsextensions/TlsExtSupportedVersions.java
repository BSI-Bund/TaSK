package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsPointFormats;
import com.achelos.task.commons.enums.TlsVersion;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TlsExtSupportedVersions extends TlsExtension{

    private final List<TlsVersion> supportedVersions;

    public TlsExtSupportedVersions(TlsVersion tlsVersion) {
        super(TlsExtensionTypes.supported_versions);
        supportedVersions = List.of(tlsVersion);
    }

    public TlsExtSupportedVersions() {
        super(TlsExtensionTypes.supported_versions);
        supportedVersions = new ArrayList<>();
    }

    public void addSupportedVersion(TlsVersion tlsVersion){
        supportedVersions.add(tlsVersion);
    }

    @Override
    protected byte[] getData() {
        final int length = supportedVersions.size()*2;
        final ByteBuffer buffer = ByteBuffer.allocate(1 + length);
        buffer.put((byte) length);
        for (TlsVersion supportedVersion : supportedVersions) {
            buffer.put(supportedVersion.getMajor());
            buffer.put(supportedVersion.getMinor());
        }
        buffer.flip();
        return buffer.array();
    }

    @Override
    public TlsExtension createExtension(byte[] data) {
        return null;
    }

    public static final TlsExtSupportedVersions createDefault() {
        var supportedVersionExt = new TlsExtSupportedVersions();
        supportedVersionExt.addSupportedVersion(TlsVersion.TLS_V1_2);
        supportedVersionExt.addSupportedVersion(TlsVersion.TLS_V1_3);
        return supportedVersionExt;

    }
}
