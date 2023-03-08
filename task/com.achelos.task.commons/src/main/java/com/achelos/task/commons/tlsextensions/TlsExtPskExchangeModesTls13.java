package com.achelos.task.commons.tlsextensions;

import com.achelos.task.commons.enums.TlsExtensionTypes;
import com.achelos.task.commons.enums.TlsPskKeyExchangeMode;
import com.achelos.task.commons.enums.TlsVersion;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TlsExtPskExchangeModesTls13 extends TlsExtension{
    private final List<TlsPskKeyExchangeMode>  pskKeyExchangeModeList = new ArrayList<>();

    protected TlsExtPskExchangeModesTls13() {
        super(TlsExtensionTypes.psk_key_exchange_modes);
    }

    public void addPskExchangeMode(TlsPskKeyExchangeMode pskKeyExchangeMode){
        pskKeyExchangeModeList.add(pskKeyExchangeMode);
    }

    @Override
    protected byte[] getData() {
        final int length = pskKeyExchangeModeList.size();
        final ByteBuffer buffer = ByteBuffer.allocate(1 + length);
        buffer.put((byte) length);
        for (TlsPskKeyExchangeMode pskKeyExchangeMode : pskKeyExchangeModeList) {
            buffer.put(pskKeyExchangeMode.getValue());
        }
        buffer.flip();
        return buffer.array();    }

    @Override
    public TlsExtension createExtension(byte[] data) {
        return null;
    }

    public static TlsExtPskExchangeModesTls13 createDefault(){
        var extPskExchangeModes = new TlsExtPskExchangeModesTls13();
        extPskExchangeModes.addPskExchangeMode(TlsPskKeyExchangeMode.psk_ke);
        extPskExchangeModes.addPskExchangeMode(TlsPskKeyExchangeMode.psk_dhe_ke);
        return extPskExchangeModes;
    }
}
