curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '
{
  "name": "keycloak-events",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "connection.url": "http://elasticsearch:9200",
    "tasks.max": "1",
    "topics": "keycloak-events",
    "type.name": "_doc",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.ignore": "true",
    "schema.ignore": "true",
    "value.converter.schemas.enable": "false",
    "flush.synchronously": "true",
    "transforms": "TimestampRouter",
    "transforms.TimestampRouter.type": "org.apache.kafka.connect.transforms.TimestampRouter",
    "transforms.TimestampRouter.topic.format": "${topic}-${timestamp}",
    "transforms.TimestampRouter.timestamp.format": "yyyyMMdd"
  }
}'
