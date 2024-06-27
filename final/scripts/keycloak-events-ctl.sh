#!/bin/bash

OPT="$1"

ACCESS_TOKEN=$( curl -s -X POST 'http://127.0.0.1:8080/realms/master/protocol/openid-connect/token' \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=admin" \
 -d 'password=admin' \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli' | jq -r .access_token )


case ${OPT} in
events-provider-add)
  curl -X PUT -H "Authorization: Bearer ${ACCESS_TOKEN}" -H "Content-Type: application/json" 'http://127.0.0.1:8080/admin/realms/master/events/config' -d '{
  "eventsEnabled": true,
  "eventsListeners": [
    "kafka",
    "jboss-logging"
  ]
}'
  ;;
events-provider-show-config)
  curl -X GET -H "Authorization: Bearer ${ACCESS_TOKEN}" 'http://127.0.0.1:8080/admin/realms/master/events/config' | jq
  ;;
*)
  echo "Unknown param:[${OPT}]. Expect params: [events-provider-add|events-provider-show-config]"
  exit 1
  ;;
esac
