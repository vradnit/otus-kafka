.PHONY: up
up:
	docker-compose -f ./docker/docker-compose.yml up -d

.PHONY: connector-create
connector-create:
	curl -s -X POST -d @debezium/connector.json -H "Content-Type: application/json" "http://localhost:8083/connectors" | jq

.PHONY: connector-status
connector-status:
	curl -s -X GET "http://localhost:8083/connectors/postgres-otus-source/status" | jq

.PHONY: load
load:
	docker exec -it postgres psql -U postgres -d otus \
    -c "WITH data AS (SELECT generate_series(1, 20) i) INSERT INTO customers(name) SELECT format('name-%s', i) FROM data;"


.PHONY: list-topics
list-topics:
	docker exec -it kafka1 /bin/kafka-topics --list --bootstrap-server=kafka1:9092

.PHONY: list-customers
list-customers:
	docker exec -it kafka1 /bin/kafka-console-consumer --bootstrap-server=kafka1:9092 --topic=postgres.public.customers --from-beginning --timeout-ms=1000

.PHONY: clean
clean:
	docker-compose -f ./docker/docker-compose.yml down -v
