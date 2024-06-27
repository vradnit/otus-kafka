.PHONY: up
up:
	docker-compose -f ./docker-compose.yml up -d

.PHONY: config
config: create-topics keycloak-events-add connector-create indextemplate-create

.PHONY: connector-create
connector-create:
	./scripts/connector.sh

.PHONY: connector-status
connector-status:
	curl -s -X GET "http://localhost:8083/connectors/keycloak-events/status" | jq

.PHONY: connector-delete
connector-delete:
	curl -X DELETE http://localhost:8083/connectors/keycloak-events

.PHONY: indextemplate-create
indextemplate-create:
	./scripts/elasticsearch.sh
	./scripts/kibana.sh

.PHONY: keycloak-events-add-kafka
keycloak-events-add:
	./scripts/keycloak-events-ctl.sh events-provider-add

.PHONY: keycloak-events-show-config
keycloak-events-show-config:
	./scripts/keycloak-events-ctl.sh events-provider-show-config

.PHONY: test-malicious-user-not-found
test-malicious-user-not-found:
	./scripts/keycloak-login.sh start

.PHONY: test-malicious-invalid-user-credentials
test-malicious-invalid-user-credentials:
	./scripts/keycloak-login-fail.sh start

.PHONY: test-new-user-location
test-new-user-location:
	./scripts/keycloak-login-ok.sh start

.PHONY: list-topics
list-topics:
	docker exec -it kafka /bin/kafka-topics --bootstrap-server=kafka:29092 --list

.PHONY: create-topics
create-topics:
	./scripts/kafka-create-topics.sh

.PHONY: list-customers
list-customers:
	docker exec -it kafka /bin/kafka-console-consumer --bootstrap-server=kafka:29092 --topic=keycloak-events --from-beginning --timeout-ms=1000

.PHONY: clean
clean:
	docker-compose -f ./docker-compose.yml down -v --remove-orphans
