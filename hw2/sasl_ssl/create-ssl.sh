#!/bin/bash

set -o nounset \
    -o errexit \
    -o verbose \
    -o xtrace


CA_NAME="kafka-ca"
DAYS="365"
PASS="changeit"
SSL_DIR="./ssl"


if [ ! -d "${SSL_DIR}" ]; then
  mkdir "${SSL_DIR}"
fi
cd ${SSL_DIR}

# Generate CA key
openssl req -new -x509 -keyout ${CA_NAME}.key -out ${CA_NAME}.crt -days ${DAYS} -subj '/CN=kafka-ca/OU=Test/O=Home/L=Moscow/S=Moscow/C=RU' -passin "pass:${PASS}" -passout "pass:${PASS}"

for i in kafka1 kafka2 kafka3 producer consumer kafdrop kafka-ui
do
	echo $i
	# Create keystores
	keytool -genkey -noprompt \
				 -alias $i \
				 -dname "CN=$i, OU=Test, O=Home, L=Moscow, S=Moscow, C=RU" \
				 -keystore $i.keystore.jks \
				 -keyalg RSA \
				 -storepass "${PASS}" \
				 -keypass "${PASS}" \
         -noprompt

	# Create CSR, sign the key and import back into keystore
	keytool -keystore $i.keystore.jks -alias $i -certreq -file $i.csr -storepass "${PASS}" -keypass "${PASS}" -noprompt

	openssl x509 -req -CA ${CA_NAME}.crt -CAkey ${CA_NAME}.key -in $i.csr -out $i-ca-signed.crt -days ${DAYS} -CAcreateserial -passin "pass:${PASS}"

	keytool -keystore $i.keystore.jks -alias CARoot -import -file ${CA_NAME}.crt -storepass "${PASS}" -keypass "${PASS}" -noprompt

	keytool -keystore $i.keystore.jks -alias $i -import -file $i-ca-signed.crt -storepass "${PASS}" -keypass "${PASS}" -noprompt

	# Create truststore and import the CA cert.
	keytool -keystore $i.truststore.jks -alias CARoot -import -file ${CA_NAME}.crt -storepass "${PASS}" -keypass "${PASS}" -noprompt

done

