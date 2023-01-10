#!/bin/bash


# IN

MANIPULATION_ID="CERT_DEFAULT"

HOME_DIR=$PWD
CERTIFICATE_EXTENSION="${HOME_DIR}/extension/test_certificate.ext"
CA_CONFIG="${HOME_DIR}/configuration/test_ca_certificate.cnf"
SERVER_CONFIG="${HOME_DIR}/configuration/test_server_certificate.cnf"
CLIENT_CONFIG="${HOME_DIR}/configuration/test_client_certificate.cnf"

KEY_SIZE_RSA_DSA="4096"
LIFETIME_DAYS="1460"
signingRequest="signingRequests"


declare -a SIGNATURE_ALGORITHM=(sha224 sha256 sha384 sha512)
declare -a KEY_ALGORITHM=("RSA" "DSA" "ECDSA") #make uniform
declare -a EC_KEY_CURVES=("secp256r1" "secp384r1" "secp521r1" "brainpoolP256r1" "brainpoolP384r1" "brainpoolP512r1")
#declare -a RSA_PADDING=(pcks15)


generateCACert() {
    CA_KEY=$1
    CA_CONFIG=$2
    CA_CSR=$3
    LIFETIME_DAYS=$4
    CA_CERTIFICATE=$5
    CERTIFICATE_EXTENSION=$6
    SIG_ALGS=$7
       
    openssl req -new -config ${CA_CONFIG} -key ${CA_KEY} -out ${CA_CSR}
    openssl x509 -req -days ${LIFETIME_DAYS} -in "${CA_CSR}" -signkey ${CA_KEY} -out ${CA_CERTIFICATE} -extfile ${CERTIFICATE_EXTENSION} -extensions ca -${SIG_ALGS}
    openssl x509 -noout -text -in ${CA_CERTIFICATE}
}

generateCert () {
    KEY=$1
    CONFIG=$2
    CSR=$3
    LIFETIME_DAYS=$4
    CA_CERTIFICATE=$5
    CA_KEY=$6
    CERTIFICATE_EXTENSION=$7
    CA_SRL=$7
    SIG_ALGS=$8
    CERTIFICATE_OUT=$9

    openssl req -new -config ${SERVER_CONFIG} -key ${KEY} -out ${CSR}
    openssl x509 -req -days ${LIFETIME_DAYS} -in ${SERVER_CSR} -CA ${CA_CERTIFICATE} -CAkey ${CA_KEY} -out ${CERTIFICATE_OUT} -extfile ${CERTIFICATE_EXTENSION} -extensions server -CAserial ${CA_SRL} -CAcreateserial -${SIG_ALGS}
    openssl x509 -noout -text -in ${CERTIFICATE_OUT}
} 


generateKey () {

    KEY_ALGO=$1
    OUTPUT_KEY_FILE=$2
    PREFIX=$3
   
    if [ "$KEY_ALGO" == "RSA" ] || [ "$KEY_ALGO" == "RSA-PSS" ]; then
            openssl genpkey -algorithm $KEY_ALGO -pkeyopt rsa_keygen_bits:${KEY_SIZE_RSA_DSA} -out $OUTPUT_KEY_FILE
            #openssl genrsa -out ${OUTPUT_KEY_FILE} 4096
    fi
    if [ "$KEY_ALGO" == "DSA" ]; then
        DSA_PARAM="${PREFIX}_dsaparam.pem"
        openssl dsaparam -out ${DSA_PARAM} ${KEY_SIZE_RSA_DSA}
        openssl gendsa -out ${OUTPUT_KEY_FILE} ${DSA_PARAM}
    fi
    if [ "$KEY_ALGO" == "ECDSA" ]; then
        EC_KURVE=$4
        openssl ecparam -name ${EC_KURVE} -genkey -noout -out ${OUTPUT_KEY_FILE}
    fi
}

generateCertificateChain () {

    KEY_ALGO_P=$1
    SIG_ALG_P=$2
    TLS_VERSION=$3
    CURVE_P=$4
    
    
    mkdir -p ${signingRequest}
    
    if [ "${KEY_ALGO_P}" == "ECDSA" ]; then 
        PREFIX="${TLS_VERSION}_${KEY_ALGO_P,,}_${SIG_ALG_P}_${CURVE_P}"
    else
        PREFIX="${TLS_VERSION}_${KEY_ALGO_P,,}_${SIG_ALG_P}"
    fi
    echo "PREFIX ##################### ${PREFIX}"
    echo "KEY_ALGO_P ##################### ${KEY_ALGO_P}"
    echo "SIG_ALG_P ##################### ${SIG_ALG_P}"
    #add tls12/tls13 to certificate name
    
    CA_KEY="test_ca_${PREFIX}_private_key.pem"
    CA_CERTIFICATE="test_ca_${PREFIX}_certificate.pem"
    CA_CSR="${signingRequest}/test_ca_${PREFIX}_certificate.csr"
    
    SERVER_KEY="test_server_${PREFIX}_private_key.pem"
    SERVER_CERTIFICATE="test_server_${PREFIX}_certificate.pem"
    SERVER_CSR="${signingRequest}/test_server_${PREFIX}_certificate.csr"
             
    # generate CA
    generateKey $KEY_ALGO_P $CA_KEY $PREFIX $CURVE_P 
    generateCACert $CA_KEY $CA_CONFIG $CA_CSR $LIFETIME_DAYS $CA_CERTIFICATE $CERTIFICATE_EXTENSION $SIG_ALG_P
    
    #generate Server Certificate
    generateKey $KEY_ALGO_P $SERVER_KEY $PREFIX $CURVE_P
    generateCert $SERVER_KEY $SERVER_CONFIG $SERVER_CSR $LIFETIME_DAYS $CA_CERTIFICATE $CA_KEY $CERTIFICATE_EXTENSION $SIG_ALG_P $SERVER_CERTIFICATE
    
}


### Generate TLS 1.2 certificates 
mkdir -p tls12
cd tls12
for key_alg in "${KEY_ALGORITHM[@]}"
do
    mkdir -p "${key_alg,,}"
    cd "${key_alg,,}"
    
    mkdir -p "${MANIPULATION_ID}"
    cd "${MANIPULATION_ID}"   
    
    for sig_alg in "${SIGNATURE_ALGORITHM[@]}"
    do  
        
        if [ "$key_alg" != "ECDSA" ]; then
            generateCertificateChain $key_alg $sig_alg "tls12"
        else
            for curve in "${EC_KEY_CURVES[@]}"
            do
                generateCertificateChain $key_alg $sig_alg "tls12" $curve 
            done
        fi
        
        echo "${key_alg}${sig_alg}"      
    done
    cd ..
    cd ..
done		  
cd ..


