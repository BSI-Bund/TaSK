package com.achelos.task.commons.certificatehelper;

import com.achelos.task.commons.enums.TlsSignatureScheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public String toString(){
        return signatureScheme.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlsSignatureAlgorithmWithHashTls13 that = (TlsSignatureAlgorithmWithHashTls13) o;
        return signatureScheme == that.signatureScheme;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), signatureScheme);
    }

    @Override
    public boolean isSignatureScheme() {
        return true;
    }

    @Override
    public byte[] convertToBytes() {
        return new byte[] {signatureScheme.getUpper(), signatureScheme.getLower()};
    }

    /**
     * Method takes a byte representation of one or more Signature Algorithms With Hash concatenated and finds all
     * consisting Signature Algorithms With Hash which are returned within object representation.
     *
     * @param data The TlsSignatureScheme list in byte representation
     * @return the List<TlsSignatureScheme>
     */
    public static List<TlsSignatureAlgorithmWithHash> parseSignatureAlgorithmWithHashByteList(final byte[] data) {

        List<TlsSignatureAlgorithmWithHash> foundSignatureAlgorithmWithHash
                = new ArrayList<>();
        if (data != null) {
            // First 2 bytes is the length of the buffer
            for (int i = 2; i < data.length; i += 2) {
                TlsSignatureScheme sigAlgo;

                sigAlgo = TlsSignatureScheme.valueOf(data[i], data[i + 1]);
                if(sigAlgo != null) {
                    foundSignatureAlgorithmWithHash.add(new TlsSignatureAlgorithmWithHashTls13(sigAlgo));
                }
            }
        }
        return foundSignatureAlgorithmWithHash;
    }
}
