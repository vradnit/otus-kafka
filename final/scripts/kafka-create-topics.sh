#!/bin/bash


TOPICS=(
  keycloak-events
  malicious-user-not-found
  malicious-invalid-user-credentials
  known-user-location
  new-user-location
)


for topic in "${TOPICS[@]}" ; do
  echo "Create if not exists topic:[$topic]"
	docker exec -it kafka /bin/kafka-topics --bootstrap-server=kafka:9092 --create --topic=${topic} --if-not-exists --partitions=1 --replication-factor=1 
done

