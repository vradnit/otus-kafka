## Homework 2

### Задание:
```console
Развернуть Kafka с KRaft и настроить безопасность:
1. Запустить Kafka с Kraft:
    Сгенерировать UUID кластера
    Отформатировать папки для журналов
    Запустить брокер
2. Настроить аутентификацию SASL/PLAIN. 
   Создать трёх пользователей с произвольными именами.
3. Настроить авторизацию:
   Создать топик. 
   Первому пользователю выдать права на запись в этот топик. 
   Второму пользователю выдать права на чтение этого топика. 
   Третьему пользователю не выдавать никаких прав на этот топик.
4. От имени каждого пользователя выполнить команды:
    Получить список топиков
    Записать сообщения в топик
    Прочитать сообщения из топика
```

### 1. Запускаем Kafka с Kraft 

#### 1.1 Генерируем UUID кластера
```console
# docker run --rm --name cp-kafka-1 confluentinc/cp-kafka:7.5.3 kafka-storage random-uuid
YDUtH4RNTJSLYDHLP489Yg
```

#### 1.2 Запуск Kafka кластера с Kraft

  Используя UUID из предыдущего шага, формируем docker-compose.yml для поднятия кластера Kafka с Kraft:
  ( для варианта "без sasl" будем использовать поддиректорию "plain" )
```console
# ls hw2/plain/docker-compose.yml
```
  Поднимаем кластер Kafka с Kraft:
```console
# cd hw2/plain/
# docker-compose up -d 
Creating network "plain_default" with the default driver
Creating volume "plain_kafka1-data" with default driver
Creating volume "plain_kafka2-data" with default driver
Creating volume "plain_kafka3-data" with default driver
Creating kafka3 ... done
Creating kafka2 ... done
Creating kafka1 ... done
Creating kafdrop  ... done
Creating kafka-ui ... done

# docker ps 
CONTAINER ID   IMAGE                            COMMAND                  CREATED          STATUS          PORTS                                                     NAMES
4d31cfbb706b   obsidiandynamics/kafdrop:4.0.1   "/kafdrop.sh"            38 seconds ago   Up 37 seconds   0.0.0.0:9000->9000/tcp, :::9000->9000/tcp                 kafdrop
82315c443885   provectuslabs/kafka-ui:v0.7.1    "/bin/sh -c 'java --…"   38 seconds ago   Up 37 seconds   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp                 kafka-ui
b90be06f19d5   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   39 seconds ago   Up 37 seconds   9092/tcp, 0.0.0.0:39092->39092/tcp, :::39092->39092/tcp   kafka2
c8009d61095f   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   39 seconds ago   Up 38 seconds   9092/tcp, 0.0.0.0:39093->39093/tcp, :::39093->39093/tcp   kafka3
d0d1f254290c   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   39 seconds ago   Up 38 seconds   9092/tcp, 0.0.0.0:39091->39091/tcp, :::39091->39091/tcp   kafka1
```
  Проверяем доступность кластера:
```console
# docker run --rm --network=plain_default confluentinc/cp-kafka:7.5.3 kafka-metadata-quorum --bootstrap-server 192.168.0.61:39091 describe --status

ClusterId:              YDUtH4RNTJSLYDHLP489Yg
LeaderId:               3
LeaderEpoch:            5
HighWatermark:          17338
MaxFollowerLag:         0
MaxFollowerLagTimeMs:   66
CurrentVoters:          [1,2,3]
CurrentObservers:       []
```
  Примечание:
  Опция '--network=plain_default' необходима, чтобы "наш dokcer клиент" мог общаться с кафкой по "внутренним доменным именам",
  иначе получаем "Exception":
```console
[2024-04-06 19:58:35,327] WARN [AdminClient clientId=adminclient-1] Error connecting to node kafka1:39091 (id: 1 rack: null) (org.apache.kafka.clients.NetworkClient)
java.net.UnknownHostException: kafka1
```


### 2. Настройка аутентификацию SASL/PLAIN + авторизация
  ( для варианта "sasl_plain" будем использовать поддиректорию "plain" )
  ( предварительно нужно остановить (или удалить) docker-ы поднятые на предыдущем шаге )

#### 2.1 Создаем конфигурационные файлы
  Создаем файлы ".properties" для аутенификации пользователей "admin" "userA" "userB" "userC",
а также "jaas" конфиг для Kafka:
```console
# cat ./sasl_plain/client.admin.properties
sasl.mechanism=PLAIN
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="admin" \
  password="adminPa%3";
 
# cat ./sasl_plain/client.userA.properties
sasl.mechanism=PLAIN
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="userA" \
  password="userAPa%4";
 
# cat ./sasl_plain/client.userB.properties
sasl.mechanism=PLAIN
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="userB" \
  password="userBPa%5";
 
# cat ./sasl_plain/client.userC.properties
sasl.mechanism=PLAIN
security.protocol=SASL_PLAINTEXT
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
  username="userC" \
  password="userCPa%6";

# cat ./sasl_plain/kafka.jaas.conf
KafkaServer {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="admin"
  password="adminPa%3"
  user_admin="adminPa%3"
  user_userA="userAPa%4"
  user_userB="userBPa%5"
  user_userC="userCPa%6";
};
```

#### 2.2 Используя docker-compose.yml из предыдущего шага, добавляем в него "sasl_plain" и "авторизацию".
  Ниже представлен "diff" предыдущено и нового варианта:
```console
--- ./plain/docker-compose.yml	2024-04-05 22:52:26.292934582 +0300
+++ ./sasl_plain/docker-compose.yml	2024-04-05 22:52:53.109952322 +0300
@@ -3,7 +3,7 @@
 x-common-variables: &common-variables
   KAFKA_INTER_BROKER_LISTENER_NAME: BROKER
   KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
-  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,BROKER:PLAINTEXT,EXTERNAL:PLAINTEXT
+  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:SASL_PLAINTEXT,BROKER:SASL_PLAINTEXT,EXTERNAL:SASL_PLAINTEXT
   KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
   KAFKA_PROCESS_ROLES: 'controller,broker'
   CLUSTER_ID: 'YDUtH4RNTJSLYDHLP489Yg'
@@ -12,6 +12,15 @@
   KAFKA_METADATA_MAX_RETENTION_MS: 1200000
   KAFKA_METADATA_LOG_MAX_RECORD_BYTES_BETWEEN_SNAPSHOTS: 2800
   KAFKA_LOG_DIRS: '/var/lib/kafka/data'
+  KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
+  KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL: PLAIN
+  KAFKA_SASL_MECHANISM_CONTROLLER_PROTOCOL: PLAIN
+  KAFKA_SUPER_USERS: User:admin
+  KAFKA_CFG_ALLOW_EVERYONE_IF_NO_ACL_FOUND: "false"
+  KAFKA_AUTHORIZER_CLASS_NAME: "org.apache.kafka.metadata.authorizer.StandardAuthorizer"
+  KAFKA_OPTS: "-Djava.security.auth.login.config=/etc/kafka/jaas.conf"
+  KAFKA_LOG4J_LOGGERS: "kafka.authorizer.logger=DEBUG"
+  KAFKA_LOG4J_ROOT_LOGLEVEL: INFO
 
 services:
   kafdrop:
@@ -22,11 +31,14 @@
       - 9000:9000
     environment:
       KAFKA_BROKERCONNECT: kafka1:9092,kafka2:9092,kafka3:9092
+      KAFKA_PROPERTIES_FILE: /client.admin.properties
       JVM_OPTS: "-Xms128M -Xmx256M -XX:-TieredCompilation -XX:+UseStringDeduplication -noverify"
     depends_on:
       - kafka1
       - kafka2
       - kafka3
+    volumes:
+      - ./client.admin.properties:/client.admin.properties
 
   kafka-ui:
     image: provectuslabs/kafka-ui:v0.7.1
@@ -41,6 +53,9 @@
     environment:
       KAFKA_CLUSTERS_0_NAME: home
       KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka1:9092,kafka2:9092,kafka3:9092
+      KAFKA_CLUSTERS_0_PROPERTIES_SECURITY_PROTOCOL: SASL_PLAINTEXT
+      KAFKA_CLUSTERS_0_PROPERTIES_SASL_MECHANISM: PLAIN
+      KAFKA_CLUSTERS_0_PROPERTIES_SASL_JAAS_CONFIG: 'org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="adminPa%3";'
 
   kafka1:
     image: confluentinc/cp-kafka:7.5.3
@@ -55,6 +70,11 @@
       KAFKA_NODE_ID: 1
     volumes:
       - kafka1-data:/var/lib/kafka/data
+      - ./kafka.jaas.conf:/etc/kafka/jaas.conf
 
   kafka2:
     image: confluentinc/cp-kafka:7.5.3
@@ -69,6 +89,7 @@
       KAFKA_NODE_ID: 2
     volumes:
       - kafka2-data:/var/lib/kafka/data
+      - ./kafka.jaas.conf:/etc/kafka/jaas.conf
 
   kafka3:
     image: confluentinc/cp-kafka:7.5.3
@@ -83,6 +104,7 @@
       KAFKA_NODE_ID: 3
     volumes:
       - kafka3-data:/var/lib/kafka/data
+      - ./kafka.jaas.conf:/etc/kafka/jaas.conf
 volumes:
   kafka1-data:
   kafka2-data:
```

#### 2.3 Запускаем кластер Kafka c Kraft + SASL_PLAIN:
```console
# docker-compose up -d
Creating network "sasl_plain_default" with the default driver
Creating volume "sasl_plain_kafka1-data" with default driver
Creating volume "sasl_plain_kafka2-data" with default driver
Creating volume "sasl_plain_kafka3-data" with default driver
Creating kafka1 ... done
Creating kafka3 ... done
Creating kafka2 ... done
Creating kafdrop  ... done
Creating kafka-ui ... done

# docker ps 
CONTAINER ID   IMAGE                            COMMAND                  CREATED          STATUS          PORTS                                                     NAMES
df59c2093a7e   provectuslabs/kafka-ui:v0.7.1    "/bin/sh -c 'java --…"   12 seconds ago   Up 11 seconds   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp                 kafka-ui
00885c138048   obsidiandynamics/kafdrop:4.0.1   "/kafdrop.sh"            12 seconds ago   Up 11 seconds   0.0.0.0:9000->9000/tcp, :::9000->9000/tcp                 kafdrop
e5c26e7b4fe4   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   14 seconds ago   Up 12 seconds   9092/tcp, 0.0.0.0:39092->39092/tcp, :::39092->39092/tcp   kafka2
6466573acbc7   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   14 seconds ago   Up 12 seconds   9092/tcp, 0.0.0.0:39093->39093/tcp, :::39093->39093/tcp   kafka3
587f6c8e687c   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   14 seconds ago   Up 12 seconds   9092/tcp, 0.0.0.0:39091->39091/tcp, :::39091->39091/tcp   kafka1
```


### 3. Настройка авторизации

#### 3.1 Проверяем доступность кластера суперпользователю "admin", а также недоступность "метаинформации" для остальных пользователей:
```console
# docker run --rm --network=sasl_plain_default -v ./client.admin.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-metadata-quorum --command-config /client.properties --bootstrap-server 192.168.0.61:39091 describe --status

ClusterId:              YDUtH4RNTJSLYDHLP489Yg
LeaderId:               1
LeaderEpoch:            2
HighWatermark:          2042
MaxFollowerLag:         0
MaxFollowerLagTimeMs:   0
CurrentVoters:          [1,2,3]
CurrentObservers:       []


# docker run --rm --network=sasl_plain_default -v ./client.userA.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-metadata-quorum --command-config /client.properties --bootstrap-server 192.168.0.61:39091 describe --status

org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
	at java.base/java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:395)
	at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2005)
	at org.apache.kafka.common.internals.KafkaFutureImpl.get(KafkaFutureImpl.java:165)
	at org.apache.kafka.tools.MetadataQuorumCommand.handleDescribeStatus(MetadataQuorumCommand.java:173)
	at org.apache.kafka.tools.MetadataQuorumCommand.execute(MetadataQuorumCommand.java:101)
	at org.apache.kafka.tools.MetadataQuorumCommand.mainNoExit(MetadataQuorumCommand.java:56)
	at org.apache.kafka.tools.MetadataQuorumCommand.main(MetadataQuorumCommand.java:51)
Caused by: org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
```

#### 3.2 Создаем топик
```console
# docker run --rm --network=sasl_plain_default -v ./client.admin.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-topics --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --create --replication-factor 3 --partitions 5 --topic home
Created topic home.

# # docker run --rm --network=sasl_plain_default -v ./client.admin.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-topics --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --list
home
```

#### 3.3 Выдаем права "write" для userA, права "read" для userC
```console
# docker run --rm --network=sasl_plain_default -v ./client.admin.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-acls --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --add --allow-principal User:userA --operation WRITE --topic home
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=home, patternType=LITERAL)`: 
 	(principal=User:userA, host=*, operation=WRITE, permissionType=ALLOW) 

Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=home, patternType=LITERAL)`: 
 	(principal=User:userA, host=*, operation=WRITE, permissionType=ALLOW) 

# docker run --rm --network=sasl_plain_default -v ./client.admin.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-acls --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --add --allow-principal User:userB --operation READ --topic home --group "homhom"
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=home, patternType=LITERAL)`: 
 	(principal=User:userB, host=*, operation=READ, permissionType=ALLOW) 

Adding ACLs for resource `ResourcePattern(resourceType=GROUP, name=homhom, patternType=LITERAL)`: 
 	(principal=User:userB, host=*, operation=READ, permissionType=ALLOW) 

Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=home, patternType=LITERAL)`: 
 	(principal=User:userB, host=*, operation=READ, permissionType=ALLOW)
	(principal=User:userA, host=*, operation=WRITE, permissionType=ALLOW) 

Current ACLs for resource `ResourcePattern(resourceType=GROUP, name=homhom, patternType=LITERAL)`: 
 	(principal=User:userB, host=*, operation=READ, permissionType=ALLOW) 
```


### 4. Проверка авторизации для каждого топика

#### 4.1 Получение списка топикоп:
```console
# docker run --rm --network=sasl_plain_default -v ./client.userA.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-topics --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --list
home
 
# docker run --rm --network=sasl_plain_default -v ./client.userB.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-topics --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --list
home
 
# docker run --rm --network=sasl_plain_default -v ./client.userC.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-topics --command-config /client.properties --bootstrap-server 192.168.0.61:39092 --list

```
Итог: пользователи userA и userB получили список топиков, а userC нет.


#### 4.2 Запись сообщений в топик:
  Пользователь userA:
```console
# docker run -it --rm --network=sasl_plain_default -v ./client.userA.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-producer --producer.config /client.properties --bootstrap-server 192.168.0.61:39091 --topic=home
>m1
>m2
>m3
>m4
>m5
```
  Пользователь userB:
```console
# docker run -it --rm --network=sasl_plain_default -v ./client.userB.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-producer --producer.config /client.properties --bootstrap-server 192.168.0.61:39091 --topic=home
>b1
[2024-04-06 20:23:20,117] ERROR [Producer clientId=console-producer] Aborting producer batches due to fatal error (org.apache.kafka.clients.producer.internals.Sender)
org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
[2024-04-06 20:23:20,119] ERROR Error when sending message to topic home with key: null, value: 2 bytes with error: (org.apache.kafka.clients.producer.internals.ErrorLoggingCallback)
org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
org.apache.kafka.common.KafkaException: Cannot execute transactional method because we are in an error state
	at org.apache.kafka.clients.producer.internals.TransactionManager.maybeFailWithError(TransactionManager.java:1010)
	at org.apache.kafka.clients.producer.internals.TransactionManager.maybeAddPartition(TransactionManager.java:328)
	at org.apache.kafka.clients.producer.KafkaProducer.doSend(KafkaProducer.java:1061)
	at org.apache.kafka.clients.producer.KafkaProducer.send(KafkaProducer.java:962)
	at kafka.tools.ConsoleProducer$.send(ConsoleProducer.scala:117)
	at kafka.tools.ConsoleProducer$.loopReader(ConsoleProducer.scala:90)
	at kafka.tools.ConsoleProducer$.main(ConsoleProducer.scala:99)
	at kafka.tools.ConsoleProducer.main(ConsoleProducer.scala)
Caused by: org.apache.kafka.common.errors.ClusterAuthorizationException: Cluster authorization failed.
```
  Пользователь userC:
```console
# docker run -it --rm --network=sasl_plain_default -v ./client.userC.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-producer --producer.config /client.properties --bootstrap-server 192.168.0.61:39091 --topic=home
>c1
[2024-04-06 20:23:36,996] WARN [Producer clientId=console-producer] Error while fetching metadata with correlation id 4 : {home=TOPIC_AUTHORIZATION_FAILED} (org.apache.kafka.clients.NetworkClient)
[2024-04-06 20:23:37,003] ERROR [Producer clientId=console-producer] Topic authorization failed for topics [home] (org.apache.kafka.clients.Metadata)
[2024-04-06 20:23:37,006] ERROR Error when sending message to topic home with key: null, value: 2 bytes with error: (org.apache.kafka.clients.producer.internals.ErrorLoggingCallback)
org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [home]
```
  
  Итог: пользователь userA успешно записаль в топик, а пользователи userB и userC нет


#### 4.2 Чтение сообщений из топика:
  Пользователь userA:
```console
# docker run -it --rm --network=sasl_plain_default -v ./client.userA.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-consumer --consumer.config /client.properties --bootstrap-server 192.168.0.61:39092 --topic=home --group "homhom" --from-beginning
[2024-04-06 20:32:27,434] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
org.apache.kafka.common.errors.GroupAuthorizationException: Not authorized to access group: homhom
Processed a total of 0 messages
```
  Пользователь userB: 
```console 
[root@test2 sasl_plain]# docker run -it --rm --network=sasl_plain_default -v ./client.userB.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-consumer --consumer.config /client.properties --bootstrap-server 192.168.0.61:39092 --topic=home --group "homhom" --from-beginning
m1
m2
m3
m4
m5
^CProcessed a total of 5 messages
```
  Пользователь userC:
```console 
# docker run -it --rm --network=sasl_plain_default -v ./client.userC.properties:/client.properties confluentinc/cp-kafka:7.5.3 kafka-console-consumer --consumer.config /client.properties --bootstrap-server 192.168.0.61:39092 --topic=home --group "homhom" --from-beginning
[2024-04-06 20:32:57,399] WARN [Consumer clientId=console-consumer, groupId=homhom] Error while fetching metadata with correlation id 2 : {home=TOPIC_AUTHORIZATION_FAILED} (org.apache.kafka.clients.NetworkClient)
[2024-04-06 20:32:57,400] ERROR [Consumer clientId=console-consumer, groupId=homhom] Topic authorization failed for topics [home] (org.apache.kafka.clients.Metadata)
[2024-04-06 20:32:57,401] ERROR Error processing message, terminating consumer process:  (kafka.tools.ConsoleConsumer$)
org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [home]
Processed a total of 0 messages
```

  Итог: пользователи userA и userC не имеют прав чтения из топика, а пользователь userB успешно прочиталь сообщения.
