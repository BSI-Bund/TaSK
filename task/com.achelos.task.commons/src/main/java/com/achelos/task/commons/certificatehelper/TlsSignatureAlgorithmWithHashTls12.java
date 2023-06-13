package com.achelos.task.commons.certificatehelper;

import com.achelos.task.commons.enums.TlsHashAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureAlgorithm;
import com.achelos.task.commons.enums.TlsSignatureScheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TlsSignatureAlgorithmWithHashTls12 extends TlsSignatureAlgorithmWithHash{


    /**
     * Constructor using an instance of {@link TlsSignatureAlgorithm} and {@link TlsHashAlgorithm}.
     *
     * @param signatureAlgorithm The TLS Signature Algorithm to store.
     * @param hashAlgorithm      The Hash Algorithm to store.
     */
    public TlsSignatureAlgorithmWithHashTls12(TlsSignatureAlgorithm signatureAlgorithm, TlsHashAlgorithm hashAlgorithm) {
        super(signatureAlgorithm, hashAlgorithm);
    }

    @Override
    public boolean isSignatureScheme() {
        return false;
    }

	@Override
	public byte[] convertToBytes() {
		return new byte[] {getHashAlgorithm().getValue(), getSignatureAlgorithm().getValue()};
	}

	/**
	 * This function outputs all TlsSignatureAlgorithmWithHash which should be supported according to the TR-02102-2
	 * and for which we have created a server certificate
	 * @return
	 */
	public static List<TlsSignatureAlgorithmWithHash> getSupportedCertificateTypesTls12(){
		List<TlsSignatureAlgorithmWithHash> signatureAlgorithmWithHashes = new LinkedList<>();
		List<TlsHashAlgorithm> supportedHashList = new LinkedList<>(Arrays.asList(TlsHashAlgorithm.sha256, TlsHashAlgorithm.sha384, TlsHashAlgorithm.sha512));
		List<TlsSignatureAlgorithm> supportedSignatureAlgorithmList =
				new LinkedList<>(Arrays.asList( TlsSignatureAlgorithm.ecdsa, TlsSignatureAlgorithm.rsa, TlsSignatureAlgorithm.dsa));

		for (TlsSignatureAlgorithm sigAlg: supportedSignatureAlgorithmList){
			for(TlsHashAlgorithm hashAlg: supportedHashList) {
				signatureAlgorithmWithHashes.add(new TlsSignatureAlgorithmWithHashTls12(sigAlg, hashAlg));
			}
		}
		return signatureAlgorithmWithHashes;
	}

	/**
	 * Method takes a byte representation of one or more Signature Algorithms With Hash concatenated and finds all
	 * consisting Signature Algorithms With Hash which are returned within object representation.
	 *
	 * @param data The TlsSignatureAlgorithmWithHash list in byte representation
	 * @return the List<TlsSignatureAlgorithmWithHash>
	 */
	public static List<TlsSignatureAlgorithmWithHash> parseSignatureAlgorithmWithHashByteList(final byte[] data) {
		List<TlsSignatureAlgorithmWithHash> foundSignatureAlgorithmWithHash
				= new ArrayList<>();
		if (data != null) {
			// First 2 bytes is the length of the buffer
			for (int i = 2; i < data.length; i += 2) {
				TlsSignatureAlgorithmWithHash sigHashAlgo;
				var sigAlgo = TlsSignatureAlgorithm.getElement(data[i + 1]);
				var hashAlgo = TlsHashAlgorithm.getElement(data[i]);
				if(sigAlgo == null || hashAlgo == null){
					if(!TlsSignatureScheme.isSignatureScheme(data[i], data[i+1])){
						throw new IllegalArgumentException("Illegal SignatureWithHashAlgorithm");
					} else{
						continue;
					}
				}
				sigHashAlgo = new TlsSignatureAlgorithmWithHashTls12(sigAlgo, hashAlgo);
				foundSignatureAlgorithmWithHash.add(sigHashAlgo);
			}
		}
		return foundSignatureAlgorithmWithHash;
	}
}
