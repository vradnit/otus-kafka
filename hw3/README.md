## Homework 3

### Задание:
```console
Разработка приложения с транзакциями
Цель: Научиться самостоятельно разрабатывать и запускать приложения с транзакциями

Описание/Пошаговая инструкция выполнения домашнего задания:
1. Запустить Kafka
2. Создать два топика: topic1 и topic2
3. Разработать приложение, которое:
    открывает транзакцию
    отправляет по 5 сообщений в каждый топик
    подтверждает транзакцию
    открывает другую транзакцию
    отправляет по 2 сообщения в каждый топик
    отменяет транзакцию
4. Разработать приложение, которое будет читать сообщения из топиков topic1 и topic2 так, 
чтобы сообщения из подтверждённой транзакции были выведены, а из неподтверждённой - нет
```

### Решение:

### Список файлов домашней работы:
```console
# cd hw3
# tree
.
├── images
│   ├── kafdrop_transaction1.png
│   ├── kafdrop_transaction2.png
│   └── kafdrop_transaction3.png
├── java
│   ├── Makefile
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── ru
│           │       └── otus
│           │           ├── transaction
│           │           │   ├── Consumer.java
│           │           │   └── Producer.java
│           │           └── utils
│           │               └── Utils.java
│           └── resources
│               └── logback.xml
├── kafka
│   └── docker-compose.yml
└── README.md
```

### Запускаем кластер kafka:
  За основу был взят docker-compose.yml из hw2.
```console
# docker-compose -f ./kafka/docker-compose.yml up -d
Creating network "kafka_default" with the default driver
Creating kafka1 ... done
Creating kafka2 ... done
Creating kafka3 ... done
Creating kafka-ui ... done
Creating kafdrop  ... done

# docker ps
CONTAINER ID   IMAGE                            COMMAND                  CREATED              STATUS              PORTS                                                     NAMES
42ff89b4718d   obsidiandynamics/kafdrop:4.0.1   "/kafdrop.sh"            About a minute ago   Up About a minute   0.0.0.0:9000->9000/tcp, :::9000->9000/tcp                 kafdrop
aa290311b215   provectuslabs/kafka-ui:v0.7.1    "/bin/sh -c 'java --…"   About a minute ago   Up About a minute   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp                 kafka-ui
f7bc1fa60467   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   About a minute ago   Up About a minute   9092/tcp, 0.0.0.0:39091->39091/tcp, :::39091->39091/tcp   kafka1
ba7e40696fe6   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   About a minute ago   Up About a minute   9092/tcp, 0.0.0.0:39092->39092/tcp, :::39092->39092/tcp   kafka2
a4edd48665ad   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   About a minute ago   Up About a minute   9092/tcp, 0.0.0.0:39093->39093/tcp, :::39093->39093/tcp   kafka3
```

### Собираем код наших приложений для producer и consumer в один jar файл:
```console
# cd ./java/

# make build 
mvn compile assembly:single
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------------< org.example:java >--------------------------
[INFO] Building java 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ java ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] Copying 1 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ java ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 3 source files to /usr/local/src/otus/otus-kafka/hw3/java/target/classes
[INFO] 
[INFO] --- maven-assembly-plugin:2.2-beta-5:single (default-cli) @ java ---
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] ch/ already added, skipping
[INFO] ch/qos/ already added, skipping
[INFO] ch/qos/logback/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/ch.qos.logback/ already added, skipping
[INFO] META-INF/INDEX.LIST already added, skipping
[INFO] module-info.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] Building jar: /usr/local/src/otus/otus-kafka/hw3/java/target/java-1.0-SNAPSHOT-jar-with-dependencies.jar
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] ch/ already added, skipping
[INFO] ch/qos/ already added, skipping
[INFO] ch/qos/logback/ already added, skipping
[INFO] META-INF/maven/ already added, skipping
[INFO] META-INF/maven/ch.qos.logback/ already added, skipping
[INFO] META-INF/INDEX.LIST already added, skipping
[INFO] module-info.class already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] META-INF/services/ already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] META-INF/ already added, skipping
[INFO] META-INF/MANIFEST.MF already added, skipping
[INFO] org/ already added, skipping
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.543 s
[INFO] Finished at: 2024-04-28T23:47:49+03:00
[INFO] ------------------------------------------------------------------------
```

### Запускаем consumer:
Уточнение: вывод о полученных сообщениях появится, только после запуска продюсера.
```console
# make consumer
[root@test2 java]# make consumer
java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.transaction.Consumer
2024-04-28 23:48:43,290 [main] INFO  Consumer - Start: consumer
2024-04-28 23:49:40,253 [main] INFO  Consumer - [topic: topic1, partition: 0, offset: 0 key: 0] good-02e14d7e-1081-4502-a22f-51853e0ca689
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic1, partition: 0, offset: 1 key: 1] good-7b1e86b6-2316-4a73-8a45-3728c09cd52c
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic1, partition: 0, offset: 2 key: 2] good-c21888b3-41fb-48f1-9a5e-e09884778749
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic1, partition: 0, offset: 3 key: 3] good-0d4d6dc5-0354-44cc-8dd9-cd98623f1bfd
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic1, partition: 0, offset: 4 key: 4] good-bf3d755a-160e-477b-8d21-c05bd894ed27
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic2, partition: 0, offset: 0 key: 0] good-f7a66e0a-1690-4d97-945b-c14f20810112
2024-04-28 23:49:40,254 [main] INFO  Consumer - [topic: topic2, partition: 0, offset: 1 key: 1] good-dbf91e43-d0f5-40b2-82cc-5415d304fef8
2024-04-28 23:49:40,255 [main] INFO  Consumer - [topic: topic2, partition: 0, offset: 2 key: 2] good-98b2be37-b625-4802-8c3e-3880b26ac1ce
2024-04-28 23:49:40,255 [main] INFO  Consumer - [topic: topic2, partition: 0, offset: 3 key: 3] good-8fb2962d-8da9-439d-a79f-e043f23b63e2
2024-04-28 23:49:40,255 [main] INFO  Consumer - [topic: topic2, partition: 0, offset: 4 key: 4] good-0ea12360-ee88-43d6-b8b7-a44e046b44bd
```

### Запускаем producer:
```console
# make producer
java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.transaction.Producer
2024-04-28 23:49:40,125 [main] INFO  Producer - Start: committed transaction
2024-04-28 23:49:40,164 [main] INFO  Producer - [topic: topic1, key: 0] good-02e14d7e-1081-4502-a22f-51853e0ca689
2024-04-28 23:49:40,167 [main] INFO  Producer - [topic: topic2, key: 0] good-f7a66e0a-1690-4d97-945b-c14f20810112
2024-04-28 23:49:40,167 [main] INFO  Producer - [topic: topic1, key: 1] good-7b1e86b6-2316-4a73-8a45-3728c09cd52c
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic2, key: 1] good-dbf91e43-d0f5-40b2-82cc-5415d304fef8
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic1, key: 2] good-c21888b3-41fb-48f1-9a5e-e09884778749
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic2, key: 2] good-98b2be37-b625-4802-8c3e-3880b26ac1ce
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic1, key: 3] good-0d4d6dc5-0354-44cc-8dd9-cd98623f1bfd
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic2, key: 3] good-8fb2962d-8da9-439d-a79f-e043f23b63e2
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic1, key: 4] good-bf3d755a-160e-477b-8d21-c05bd894ed27
2024-04-28 23:49:40,168 [main] INFO  Producer - [topic: topic2, key: 4] good-0ea12360-ee88-43d6-b8b7-a44e046b44bd
2024-04-28 23:49:40,226 [main] INFO  Producer - End: committed transaction
2024-04-28 23:49:40,227 [main] INFO  Producer - Start: aborted transaction
2024-04-28 23:49:40,227 [main] INFO  Producer - [topic: topic1, key: 0] fail-0930d103-32a3-4ad5-bf7e-8ef2c8f4fea2
2024-04-28 23:49:40,227 [main] INFO  Producer - [topic: topic2, key: 0] fail-d304a8b7-c914-4ccf-a38b-7c671ad6cff7
2024-04-28 23:49:40,227 [main] INFO  Producer - [topic: topic1, key: 1] fail-4d05bfd0-ccfe-407a-8bf8-83a8ce4beff7
2024-04-28 23:49:40,228 [main] INFO  Producer - [topic: topic2, key: 1] fail-5cbef79a-c41b-4efc-9039-9e4eef1c0c38
2024-04-28 23:49:40,280 [main] INFO  Producer - End: Aborted transaction
```

### Выводы:
  Из лога consumer видно, что он "получил" только сообщения из "закоммиченной" транзакции.
  В директории "images" приложены скрины из "kafdrop"
