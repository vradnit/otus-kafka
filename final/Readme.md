## Применение kafka-streams и kafka-connect для детектирования мошеннической активности в событиях Keycloak


### Краткий план:
```console
1. Все сервисы запускаются в docker-compose.
2. Keycloak + модуль для отправки событий в Kafka
3. Kafka-connect + elasticsearch + kibana для визуализации событий из keycloak
4. Kafka-streams для детектирования аномальной активности:
    . "подбор пользователя"
    . "подбор пароля"
    . "Успешное подключение с новой локации"
5. Ksqldb для тестирования "удаления" записи из "таблицы локаций" 
```

### Краткое описание

#### 1. В качестве модуля для отправки событий из Keycloak в Kafka будем использовать проект:
```console
  https://github.com/SnuK87/keycloak-kafka.git
```

#### 2. Формат сообщений "json", в каждом сообщении есть поле "error".
  Виды сообщений "error":
```console
  . "user_not_found" ( кратко UNF )
  . "invalid_user_credentials" ( кратко IUC )
  . null - при успешной аутенификации
```

#### 3. Варианты детектируемых аномалий:
```console
 . "Подбор пользователя"
   MAXCOUNT=5
   IF count(UNF)[5m] > MAXCOUNT => MALICIOUS_USER_NOT_FOUND_TOPIC
   
   Пример использования: "Временная блокировка авторизации по IP"

 . "Подбор пароля"
   MAXCOUNT=5
   IF count(IUC with KEY "userid:realmid:ip" )[5m] > MAXСOUNT => MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC
   
   Пример использования:
   "Временная блокировка авторизации по IP" + "Сообщение пользователю о возможном мошенническом подключении к его аккаунту"


 . "Успешное подключение с новой локации"
   IF error==null AND KEY with "userid:realmid:location" notexist in topic "KNOWN_USER_LOCATION_TOPIC" => NEW_LOCATIONS_TOPICS
   
   Пример использования: "Сообщение пользователю об успешном подключении к его аккаунту из новой локации"

   Для упрощения в качестве "location" будем использовать "ip", в реальных условиях необходимо использовать например "GeoLite".
   GeoLite позволяет получить дополнительную информацию по IP адресу "провайдер" + "asn" + "координаты"
```

#### 4. Используемые топики в Kafka:
```console
 . KEYCLOAK_EVENT_TOPIC:
   key => empty
   value => KeycloakDto

 . MALICIOUS_USER_NOT_FOUND_TOPIC:
   key => "%s@<%s,%s>",ipAddress, window().startTime(), window().endTime()
   value => IpDto

 . MALICIOUS_INVALID_USER_CREDENTIALS_TOPIC:
   key => "%s@<%s,%s>",userId:realmId:ipAddress, window().startTime(), window().endTime()
   value => IpIUCDto

 . KNOWN_USER_LOCATION_TOPIC:
   key => userId:realmId:location
   value => LocationDto

 . NEW_LOCATION_TOPIC:
   key => userId:realmId:location
   value => KeycloakDto
```

### Старт и конфигурация всех сервисов:  
```console
# make up

# docker logs keycloak
# docker logs kafka
# docker logs kafka-connect

# make config
```

### Запуск тестов
```console
# make test-malicious-user-not-found

# make test-malicious-invalid-user-credentials

# make test-new-user-location
```

### Пример "удаления" записи из топика KNOWN_USER_LOCATION_TOPIC: 
```console
# docker exec -it ksqldb-cli ksql http://ksqldb:8088

CREATE STREAM test_deletes (key VARCHAR KEY, DUMMY VARCHAR)  WITH (KAFKA_TOPIC='known-user-location', VALUE_FORMAT='KAFKA');
SELECT * from TEST_DELETES emit changes limit 100;
INSERT INTO TEST_DELETES (KEY,DUMMY) VALUES ('b0ea40ba-aa14-4158-855b-ef43a7a2eadb:a31feaed-cee1-43ba-86dc-6f8a5330b03c:10.90.99.10',CAST(NULL AS VARCHAR));
```

### Проверка "собранной статистики" из топика "KEYCLOAK_EVENT_TOPIC" в "kibana":
```console
http://127.0.0.1:5601/
```

### Пример вывода при старте проекта (kafka-stream, keycloak, kafka-connect уже были предварительно собраны):
```console
# make up
docker-compose -f ./docker-compose.yml up -d
Creating network "kafka-otus-final_default" with the default driver
Creating zookeeper     ... done
Creating elasticsearch ... done
Creating kafka         ... done
Creating kibana        ... done
Creating kafdrop         ... done
Creating keycloak        ... done
Creating schema-registry ... done
Creating kafka-stream    ... done
Creating kafka-connect   ... done
Creating ksqldb          ... done
Creating ksqldb-cli      ... done
```

### Пример вывода проверки "статуса" запущенных контейнеров:
```console
# docker ps 
CONTAINER ID   IMAGE                                                  COMMAND                  CREATED              STATUS                        PORTS                                                 NAMES
f598aa316270   confluentinc/ksqldb-cli:0.29.0                         "/bin/sh"                About a minute ago   Up About a minute                                                                   ksqldb-cli
215de4a8fad3   confluentinc/ksqldb-server:0.29.0                      "/usr/bin/docker/run"    About a minute ago   Up About a minute             0.0.0.0:8088->8088/tcp, :::8088->8088/tcp             ksqldb
862fc505cf99   kafka-otus-final_kafka-connect                         "/etc/confluent/dock…"   About a minute ago   Up About a minute (healthy)   0.0.0.0:8083->8083/tcp, :::8083->8083/tcp, 9092/tcp   kafka-connect
c07e527cc3fd   kafka-otus-final_kafka-stream                          "java -cp /app/targe…"   About a minute ago   Up 35 seconds                                                                       kafka-stream
39605badd96c   confluentinc/cp-schema-registry:7.5.4                  "/etc/confluent/dock…"   About a minute ago   Up About a minute             0.0.0.0:8081->8081/tcp, :::8081->8081/tcp             schema-registry
bf9379f8591f   obsidiandynamics/kafdrop:4.0.1                         "/kafdrop.sh"            About a minute ago   Up About a minute             0.0.0.0:9000->9000/tcp, :::9000->9000/tcp             kafdrop
dc750f2bce7f   kafka-otus-final_keycloak                              "/opt/keycloak/bin/k…"   About a minute ago   Up About a minute             0.0.0.0:8080->8080/tcp, :::8080->8080/tcp, 8443/tcp   keycloak
b654b27d84b6   docker.elastic.co/kibana/kibana:7.11.0                 "/bin/tini -- /usr/l…"   About a minute ago   Up About a minute             0.0.0.0:5601->5601/tcp, :::5601->5601/tcp             kibana
9cd3ab356920   confluentinc/cp-kafka:7.5.4                            "/etc/confluent/dock…"   About a minute ago   Up About a minute             0.0.0.0:9092->9092/tcp, :::9092->9092/tcp             kafka
dd27cf0b41c1   confluentinc/cp-zookeeper:7.5.4                        "/etc/confluent/dock…"   About a minute ago   Up About a minute             2181/tcp, 2888/tcp, 3888/tcp                          zookeeper
bb02a402e64e   docker.elastic.co/elasticsearch/elasticsearch:7.11.0   "/bin/tini -- /usr/l…"   About a minute ago   Up About a minute             0.0.0.0:9200->9200/tcp, :::9200->9200/tcp, 9300/tcp   elasticsearch
```

### Пример вывода при запуске конфигурации проекта:
```console
# make config 
./scripts/kafka-create-topics.sh
Create if not exists topic:[keycloak-events]
Created topic keycloak-events.
Create if not exists topic:[malicious-user-not-found]
Created topic malicious-user-not-found.
Create if not exists topic:[malicious-invalid-user-credentials]
Created topic malicious-invalid-user-credentials.
Create if not exists topic:[known-user-location]
Created topic known-user-location.
Create if not exists topic:[new-user-location]
Created topic new-user-location.

./scripts/keycloak-events-ctl.sh events-provider-add

./scripts/connector.sh
{"name":"keycloak-events","config":{"connector.class":"io.confluent.connect.elasticsearch.ElasticsearchSinkConnector","connection.url":"http://elasticsearch:9200","tasks.max":"1","topics":"keycloak-events","type.name":"_doc","key.converter":"org.apache.kafka.connect.storage.StringConverter","value.converter":"org.apache.kafka.connect.json.JsonConverter","key.ignore":"true","schema.ignore":"true","value.converter.schemas.enable":"false","flush.synchronously":"true","transforms":"TimestampRouter","transforms.TimestampRouter.type":"org.apache.kafka.connect.transforms.TimestampRouter","transforms.TimestampRouter.topic.format":"${topic}-${timestamp}","transforms.TimestampRouter.timestamp.format":"yyyyMMdd","name":"keycloak-events"},"tasks":[],"type":"sink"}

./scripts/elasticsearch.sh
{"acknowledged":true}

./scripts/kibana.sh
{"type":"index-pattern","id":"index-pattern-id","attributes":{"fieldAttrs":"{}","title":"keycloak-events-*","timeFieldName":"time","fields":"[]"},"references":[],"migrationVersion":{"index-pattern":"7.11.0"},"updated_at":"2024-06-27T07:00:30.057Z","version":"WzksMV0=","namespaces":["default"]}
```
