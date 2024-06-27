#!/bin/bash

OPT="$1"

USERNAMES=(
  admin
  bob
  alice
  jon
  billy
  eric
  antony
)

sourceIPNet="10.90.90"

IPAddressKeycloak=$(docker inspect keycloak | jq -r '.[].NetworkSettings.Networks."kafka-otus-final_default".IPAddress')
usersCount="${#USERNAMES[*]}"


case $OPT in
config)
	for ii in `seq 1 $[$usersCount+1]`; do
    ip="${sourceIPNet}.${ii}"
    ip a a ${ip}/32 dev lo && echo "Add ipaddress:[${ip}]"
	done
  ;;
clean)
  for ii in `seq 1 $[$usersCount+1]`; do
    ip="${sourceIPNet}.${ii}"
    ip a d ${ip}/32 dev lo && echo "Delete ipaddress:[${ip}]"
  done
  ;;
start)
  $0 clean
  $0 config
	while true; do
		sleep 0.$[$RANDOM % 10]
		iuser=$[$RANDOM%${usersCount}]
		username="${USERNAMES[${iuser}]}"
		srcIP="${sourceIPNet}.$[${iuser}+1]"

		password="random"  
		if [ "x$username" == "xadmin" ]; then
				password="admin"
		fi  

		echo -n "LOG: date:[$(date +%Y%m%dT%H%M%S)] username:[${username}] code: "
		curl --interface ${srcIP} -X POST "http://${IPAddressKeycloak}:8080/realms/master/protocol/openid-connect/token" \
			-H "Content-Type: application/x-www-form-urlencoded" \
			-d "username=${username}" \
			-d "password=${password}" \
			-d 'grant_type=password' \
			-d 'client_id=admin-cli'
		echo ""

	done
  ;;
*)
  echo "Unknown!!! Expect option [config|clean|start]"
  ;;
esac
