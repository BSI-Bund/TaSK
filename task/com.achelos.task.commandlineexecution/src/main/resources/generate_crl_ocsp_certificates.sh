#!/bin/bash

# 1st Parameter is the base directory, where all certificates are stored
baseDirectory=$1

# 2nd Parameter is the path to the conf file of the certificates
configurationPath=$2

# 3rd Parameter is the base path to the ca certificate folder
# This is the same path as the tls_test_tool_certificates_path in the
# global configuration
caBasePath=$3

# 4th Parameter is the ocspPort
ocspPort=$4

# 5th Parameter is the crlPort
crlPort=$5

# 6th Parameter openssl customized
opensslExecutable=${6:-openssl}

# 7th Parameter: ecdsa or rsa
keyType=${7:-rsa}

configurationFile=/tmp/configfile.cnf
caBasePath+="/tls12"
mkdir -p $baseDirectory

for keyType in ecdsa rsa
do
  cd $baseDirectory
  if [ "$keyType" = "rsa" ]; then
      certFolder=certificateRsa
  elif [ "$keyType" = "ecdsa" ]; then
      certFolder=certificateEcdsa
  else
      echo "Unsupported Key Type parameter added"
      exit
  fi
  
  caCertificate="${caBasePath}/${keyType}/CERT_OCSP_CRL/root-ca_certificate.pem"
  caPrivateKey="${caBasePath}/${keyType}/CERT_OCSP_CRL/root-ca_private_key.pem"

  cp $configurationPath $configurationFile

  sed -i "s+PLACEHOLDER_BASE_DIR+${baseDirectory}/${certFolder}/root-ca+g" $configurationFile

  sed -i "s+PLACEHOLDER_PORT_CRL+${crlPort}+g" $configurationFile

  sed -i "s+PLACEHOLDER_PORT_OCSP+${ocspPort}+g" $configurationFile

  mkdir ${certFolder}
  cd ${certFolder}
  mkdir root-ca
  cd root-ca
  mkdir certs db private crl
  chmod 700 private
  touch db/index
  openssl rand -hex 16 > db/serial
  echo 1001 > db/crlnumber
  cp ${caCertificate} certs/root-ca.pem
  cp ${caPrivateKey} private/root-ca.pem
  cd ..

  # Create the Server Certificate
  mkdir server-certificate
  cd server-certificate
  mkdir certs private
  chmod 700 private

  if [ "$keyType" = "rsa" ]; then
     ${opensslExecutable} genrsa -out private/server-certKey.pem 2048
  elif [ "$keyType" = "ecdsa" ]; then
      ${opensslExecutable} ecparam -name secp256r1 -genkey -noout -out private/server-certKey.pem
  fi

  # Create the end user Server Certificate
  ${opensslExecutable} req -new -x509 -days 3650 -key private/server-certKey.pem -out certs/server-certificate.pem -config $configurationFile -nodes
  ${opensslExecutable} req -new -x509 -days 3650 -key private/server-certKey.pem -out certs/revoked-server-certificate.pem -config $configurationFile -nodes

  #Generate the certificate signing request(CSR) for the generated end-user certificate.
  ${opensslExecutable} x509 -x509toreq -in certs/server-certificate.pem -out server-certificate.csr -signkey private/server-certKey.pem
  ${opensslExecutable} x509 -x509toreq -in certs/revoked-server-certificate.pem -out revoked-server-certificate.csr -signkey private/server-certKey.pem

  #Sign the server certificate, using  CA and include CRL URLs and OCSP URLs in the certificate
  ${opensslExecutable} ca -batch -days 3650 -keyfile ${caPrivateKey} -cert ${caCertificate} -config  $configurationFile -notext -out certs/server-certificate.pem -infiles server-certificate.csr
  ${opensslExecutable} ca -batch -days 3650 -keyfile ${caPrivateKey} -cert ${caCertificate} -config  $configurationFile -notext -out certs/revoked-server-certificate.pem -infiles revoked-server-certificate.csr

  #revoke the certificate
  ${opensslExecutable} ca -keyfile ${caPrivateKey} -cert ${caCertificate} -config $configurationFile -revoke certs/revoked-server-certificate.pem -crl_reason keyCompromise

  #create the CRL
  cd ../root-ca
  ${opensslExecutable} ca -gencrl -keyfile private/root-ca.pem -cert certs/root-ca.pem -out crl/root-ca.crl -config $configurationFile -batch

  #Create OCSP responder certificate
  cd ..
  mkdir ocsp-certificate
  cd ocsp-certificate
  mkdir certs private
  chmod 700 private

  if [ "$keyType" = "rsa" ]; then
     ${opensslExecutable} genrsa -out private/ocsp-signing.pem 2048
  elif [ "$keyType" = "ecdsa" ]; then
      ${opensslExecutable} ecparam -name secp256r1 -genkey -noout -out private/ocsp-signing.pem
  fi

  ${opensslExecutable} req -new -nodes -out ocsp-signing.csr -key private/ocsp-signing.pem -config $configurationFile
  ${opensslExecutable} ca -keyfile  ${caPrivateKey} -cert  ${caCertificate} -in ocsp-signing.csr -out certs/ocsp-signing.pem -config $configurationFile -batch
done
