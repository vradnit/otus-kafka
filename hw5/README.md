## Homework 5

### Задание:
```console
Разработка приложения Kafka Streams.

Цель:
Разработать приложение Kafka Streams

Описание/Пошаговая инструкция выполнения домашнего задания:
  Разработка приложения Kafka Streams:
    Запустить Kafka
    Создать топик events
    Разработать приложение, которое подсчитывает количество событий с одинаковыми key в рамках сессии 5 минут
    Для проверки отправлять сообщения, используя console producer
```

### Решение:

#### Список файлов:
```console
├── examples
│   ├── consumer.out
│   ├── eventsCounter.out
│   └── producer.out
├── java
│   ├── Makefile
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── ru
│           │       └── otus
│           │           ├── kafkastream
│           │           │   ├── Consumer.java
│           │           │   ├── EventsCounter.java
│           │           │   └── Producer.java
│           │           └── utils
│           │               └── Utils.java
│           └── resources
│               └── logback.xml
├── kafka
│   └── docker-compose.yml
└── README.md
```

#### Поднимаем кафку:
```console
# docker-compose -f ./kafka/docker-compose.yml up -d
Creating kafka2 ... done
Creating kafka3 ... done
Creating kafka1 ... done
Creating kafdrop  ... done
Creating kafka-ui ... done

# docker ps -a
CONTAINER ID   IMAGE                            COMMAND                  CREATED          STATUS          PORTS                                                     NAMES
dc564ceff0c6   provectuslabs/kafka-ui:v0.7.1    "/bin/sh -c 'java --…"   22 seconds ago   Up 20 seconds   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp                 kafka-ui
fc8ac97f7987   obsidiandynamics/kafdrop:4.0.1   "/kafdrop.sh"            22 seconds ago   Up 20 seconds   0.0.0.0:9000->9000/tcp, :::9000->9000/tcp                 kafdrop
535584ad9d2a   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   23 seconds ago   Up 21 seconds   9092/tcp, 0.0.0.0:39092->39092/tcp, :::39092->39092/tcp   kafka2
84071c05d77e   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   23 seconds ago   Up 21 seconds   9092/tcp, 0.0.0.0:39093->39093/tcp, :::39093->39093/tcp   kafka3
330d3df5bf7b   confluentinc/cp-kafka:7.5.3      "/etc/confluent/dock…"   23 seconds ago   Up 21 seconds   9092/tcp, 0.0.0.0:39091->39091/tcp, :::39091->39091/tcp   kafka1
```

#### Компилируем java проект в jar:
(при этом не информативный вывод упущен)
```console
# cd java/

# make build
mvn compile assembly:single
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------------< org.example:java >--------------------------
[INFO] Building java 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] Building jar: /usr/local/src/otus/otus-kafka/hw5/java/target/java-1.0-SNAPSHOT-jar-with-dependencies.jar
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.295 s
[INFO] Finished at: 2024-05-22T23:22:39+03:00
[INFO] ------------------------------------------------------------------------
```

#### В трех разных консолях запускаем "producer", "eventsCounter" и "consumer":
```console
$ make producer
```

```console
$ make eventsCounter
```

```console
$ make consumer
```

#### Пример вывода представлен в директории "./examples"
```console
$ ls -1 ./examples/
consumer.out
eventsCounter.out
producer.out
```
 
  Если грепнуть вывод консюмера например по ключу "Bayasabhad", то получим следующий результат:
```console
$ grep Bayasabhad ./examples/consumer.out
2024-06-02 23:30:11,489 [main] INFO  Consumer - [topic: out, partition: 0, offset: 0 key: Bayasabhad@<2024-06-02T20:25:00Z,2024-06-02T20:30:00Z>] 1
2024-06-02 23:30:41,600 [main] INFO  Consumer - [topic: out, partition: 0, offset: 48 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 1
2024-06-02 23:31:11,639 [main] INFO  Consumer - [topic: out, partition: 0, offset: 85 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 2
2024-06-02 23:32:11,801 [main] INFO  Consumer - [topic: out, partition: 0, offset: 140 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 3
2024-06-02 23:32:41,855 [main] INFO  Consumer - [topic: out, partition: 0, offset: 176 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 4
2024-06-02 23:33:11,970 [main] INFO  Consumer - [topic: out, partition: 0, offset: 208 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 5
2024-06-02 23:33:42,018 [main] INFO  Consumer - [topic: out, partition: 0, offset: 231 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 6
2024-06-02 23:34:12,140 [main] INFO  Consumer - [topic: out, partition: 0, offset: 268 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 7
2024-06-02 23:34:42,174 [main] INFO  Consumer - [topic: out, partition: 0, offset: 300 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 8
2024-06-02 23:35:12,261 [main] INFO  Consumer - [topic: out, partition: 0, offset: 322 key: Bayasabhad@<2024-06-02T20:30:00Z,2024-06-02T20:35:00Z>] 10
2024-06-02 23:35:42,321 [main] INFO  Consumer - [topic: out, partition: 0, offset: 359 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 3
2024-06-02 23:36:42,398 [main] INFO  Consumer - [topic: out, partition: 0, offset: 415 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 5
2024-06-02 23:37:12,483 [main] INFO  Consumer - [topic: out, partition: 0, offset: 444 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 7
2024-06-02 23:37:42,601 [main] INFO  Consumer - [topic: out, partition: 0, offset: 481 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 9
2024-06-02 23:38:12,644 [main] INFO  Consumer - [topic: out, partition: 0, offset: 500 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 10
2024-06-02 23:39:12,871 [main] INFO  Consumer - [topic: out, partition: 0, offset: 576 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 11
2024-06-02 23:39:42,931 [main] INFO  Consumer - [topic: out, partition: 0, offset: 585 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 12
2024-06-02 23:40:13,019 [main] INFO  Consumer - [topic: out, partition: 0, offset: 634 key: Bayasabhad@<2024-06-02T20:35:00Z,2024-06-02T20:40:00Z>] 16
2024-06-02 23:40:43,059 [main] INFO  Consumer - [topic: out, partition: 0, offset: 659 key: Bayasabhad@<2024-06-02T20:40:00Z,2024-06-02T20:45:00Z>] 1
2024-06-02 23:41:13,102 [main] INFO  Consumer - [topic: out, partition: 0, offset: 700 key: Bayasabhad@<2024-06-02T20:40:00Z,2024-06-02T20:45:00Z>] 2
2024-06-02 23:41:43,162 [main] INFO  Consumer - [topic: out, partition: 0, offset: 723 key: Bayasabhad@<2024-06-02T20:40:00Z,2024-06-02T20:45:00Z>] 4
```
