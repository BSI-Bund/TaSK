#!/bin/bash

# 1st Parameter is the base directory, where all certificates are stored
baseDirectory=$1

# 2nd Parameter is the path to the conf file of the certificates
configurationPath=$2

# 3rd Parameter openssl customized
opensslExecutable=${3:-openssl}

# 4th Parameter: ecdsa or rsa
keyType=${4:-rsa}

configurationFile=/tmp/configfile.cnf

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

  cp $configurationPath $configurationFile

  sed -i "s+PLACEHOLDER_BASE_DIR+${baseDirectory}/${certFolder}/root-ca+g" $configurationFile

  mkdir ${certFolder}
  cd ${certFolder}
  mkdir root-ca
  cd root-ca
  mkdir certs db private
  touch db/index
  openssl rand -hex 16 > db/serial
  chmod 700 private
  cd ..

  #Create a private key for root CA.
  cd root-ca

  if [ "$keyType" = "rsa" ]; then
     ${opensslExecutable} genrsa -out private/root-ca.pem 2048
  elif [ "$keyType" = "ecdsa" ]; then
      ${opensslExecutable} ecparam -name secp256r1 -genkey -noout -out private/root-ca.pem
  fi
  ${opensslExecutable} req -new -config $configurationFile -out root-ca.csr -key private/root-ca.pem -nodes


  #create a CA certificate which is valid for 1 year
  ${opensslExecutable} req -new -x509 -days 365 -key private/root-ca.pem -out certs/root-ca.pem -config $configurationFile -extensions ca_ext

  done
