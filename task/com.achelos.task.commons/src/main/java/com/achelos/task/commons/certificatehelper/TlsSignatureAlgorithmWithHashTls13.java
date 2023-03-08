package com.achelos.task.commons.certificatehelper;

import com.achelos.task.commons.enums.TlsSignatureScheme;

public class TlsSignatureAlgorithmWithHashTls13 extends TlsSignatureAlgorithmWithHash{

    private final TlsSignatureScheme signatureScheme;

    public TlsSignatureAlgorithmWithHashTls13(TlsSignatureScheme signatureScheme) {
        super(signatureScheme.getSignatureAlgorithm(), signatureScheme.getHashAlgorithm());
        this.signatureScheme = signatureScheme;
    }

    public TlsSignatureScheme getSignatureScheme() {
        return signatureScheme;
    }

    @Override
    public boolean isSignatureScheme() {
        return true;
    }

    @Override
    public byte[] convertToBytes() {
        return new byte[] {signatureScheme.getUpper(), signatureScheme.getLower()};
    }
}
