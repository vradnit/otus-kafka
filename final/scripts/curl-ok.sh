#!/bin/bash

IPAddressKeycloak=$(docker inspect keycloak | jq -r '.[].NetworkSettings.Networks."kafka-otus-final_default".IPAddress')

curl -D - -X POST "http://${IPAddressKeycloak}:8080/realms/master/protocol/openid-connect/token" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=admin" \
 -d 'password=admin' \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli'
