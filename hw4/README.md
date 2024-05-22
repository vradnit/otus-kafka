## Homework 4

### Задание:
```console
akka streams, alpakka
Цель:
закрепить знания, полученные на занятии

Описание/Пошаговая инструкция выполнения домашнего задания:

в репозитории https://github.com/ValentinShilinDe/kafka-mooc-2023-04 есть темплэйт домашней работы
kafka-mooc-2023-04\src\main\scala\akka_akka_streams\homework\homeworktemplate.scala
задание из 2х частей
1)
    написать граф дсл, где есть какой то входной поток(целочисленный), он должен быть разделен на 3 (broadcast).
    первый поток - все элементы умножаем на 10
    второй поток - все элементы умножаем на 2
    третий поток - все элементы умножаем на 3
    потом собираем это все в один поток (zip) в котором эти 3 подпотока должны быть сложены
    1 2 3 4 5 -> 1 2 3 4 5-> 10 20 30 40 50

-> 1 2 3 4 5-> 2 4 6 8 10 -> (10,2,3), (20,4,6),(30,6,9),(40,8,12),(50,10,15)-> 15, 30, 45, 60, 75
-> 1 2 3 4 5 -> 3 6 9 12 15
2) не обязательно, задача со *. входной поток должен идти из кафки (тоесть написать продьюсер, и в консьюмере реализовать логику из пункта 1)
```

### Решение:

#### Список файлов:
```console
├── kafka
│   └── docker-compose.yml
├── README.md
├── scala-alpakka
│   ├── build.sbt
│   └── src
│       └── main
│           ├── resources
│           │   └── application.conf
│           └── scala
│               └── homework
│                   ├── ConsumerApp.scala
│                   └── ProducerApp.scala
└── scala-graphdsl
    ├── build.sbt
    └── src
        └── main
            └── scala
                └── homework
                    └── hwGraphDSL.scala
```

#### Создание GraphDSL для выполнения п.1 выполнено в директории "hw4/scala-alpakka"
  При запуске "hwGraphDSL.scala" в "IDEA" получаем следующий вывод в консоль:
```console
11:24:28.408 [fusion-akka.actor.default-dispatcher-4] INFO akka.event.slf4j.Slf4jLogger - Slf4jLogger started
15
30
45
60
75
90
105
120
135
150
165
180
195
210
225
240
255
270
285
300
```

#### Создание "консюмера" и "продюсера" для выполения задания из п.1 с использованием Alpakka выполнено в директории "hw4/scala-alpakka"

  Сначала запускаем кафку + zookeeper:
```console
# docker-compose -f ./kafka/docker-compose.yml up -d 
Creating network "kafka_default" with the default driver
Creating kafka_zookeeper_1 ... done
Creating kafka_kafka_1     ... done
```

  Затем в "IDEA" запускаем "консюмер" и "продюсер":

  Вывод "ProducerApp":
```console
11:31:10.920 [producer-sys-akka.actor.default-dispatcher-4] INFO akka.event.slf4j.Slf4jLogger - Slf4jLogger started
11:31:11.249 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 1
11:31:11.249 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 2
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 3
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 4
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 5
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 6
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 7
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 8
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 9
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 10
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 11
11:31:11.250 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 12
11:31:11.253 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 13
11:31:11.253 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 14
11:31:11.254 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 15
11:31:11.254 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 16
11:31:11.592 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 17
11:31:11.592 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 18
11:31:11.592 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 19
11:31:11.593 [producer-sys-akka.actor.default-dispatcher-7] WARN akka.stream.Materializer - [LOG] Element: 20
Done

Process finished with exit code 0
```

  Вывод "ConsumerApp":
```console
11:30:56.529 [consumer-sys-akka.actor.default-dispatcher-4] INFO akka.event.slf4j.Slf4jLogger - Slf4jLogger started
11:30:57.488 [consumer-sys-akka.kafka.default-dispatcher-12] WARN org.apache.kafka.clients.NetworkClient - [Consumer clientId=consumer-group1-1, groupId=group1] Error while fetching metadata with correlation id 2 : {test=LEADER_NOT_AVAILABLE}
15
30
45
60
75
90
105
120
135
150
165
180
195
210
225
240
255
270
285
300
```

  Т.е. мы получили вывод совпадающий с п.1

