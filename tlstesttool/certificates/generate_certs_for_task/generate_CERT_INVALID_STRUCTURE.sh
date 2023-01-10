#!/bin/bash


# IN
MANIPULATION_ID="CERT_INVALID_STRUCTURE_unmanipulated"

HOME_DIR=$PWD

declare -a KEY_ALGORITHM=("RSA" "DSA" "ECDSA") #make uniform

### Generate TLS 1.2 certificates 
### This just copies the CERT_DEFAULT content.
### Add one byte by hand to destroy the ASN.1 structure!

mkdir -p tls12
cd tls12
for key_alg in "${KEY_ALGORITHM[@]}"
do
    mkdir -p "${key_alg,,}"
    cd "${key_alg,,}"        
        
    cp -r "CERT_DEFAULT" "${MANIPULATION_ID}"
    echo "${key_alg}"   
    
    cd ..
done		  
echo "Certs were just copied from DEFAULT. Need manipulation by hand."
cd ..


