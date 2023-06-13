#!/bin/bash


# IN
MANIPULATION_ID="CERT_INVALID_STRUCTURE"

HOME_DIR=$PWD

declare -a KEY_ALGORITHM=("RSA" "DSA" "ECDSA") #make uniform

### Generate TLS 1.2 certificates 
### This just copies the CERT_DEFAULT content.
### Add one byte by hand to destroy the ASN.1 structure!

for key_alg in "${KEY_ALGORITHM[@]}"
do
   if [ "$key_alg" != "RSA-PSS" ]; then
        mkdir -p "${key_alg,,}"
        cd "${key_alg,,}"
    else 
        mkdir -p "rsa"
        cd "rsa"
    fi    
    
    cp -Tr "CERT_DEFAULT" "${MANIPULATION_ID}"
    
    echo "${key_alg}"
    python3 ../manipulateLastByte.py -i $(pwd)/${MANIPULATION_ID}/

    cd ..
done		  
cd ..

