## Homework 1

### Задание:
```console
Самостоятельно запустить Kafka по предложенному алгоритму:
    Установить Java JDK
    Скачать Kafka с сайта kafka.apache.org и развернуть на локальном диске
    Запустить Zookeeper
    Запустить Kafka Broker
    Создать топик test
    Записать несколько сообщений в топик
    Прочитать сообщения из топика
```

### 1. Скачивам JDK 17.0.2
```console
# mkdir ~/kafka
# cd ~/kafka
# curl -O https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
# tar -xzf openjdk-17.0.2_linux-x64_bin.tar.gz -C /usr/lib/jvm/ ^C
# /usr/lib/jvm/jdk-17.0.2/bin/java -version
openjdk version "17.0.2" 2022-01-18
OpenJDK Runtime Environment (build 17.0.2+8-86)
OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)
```

### 2. Скачиваем и устанавливаем кафку
```console
# cd ~/kafka
# curl -O https://downloads.apache.org/kafka/3.6.1/kafka_2.13-3.6.1.tgz

# adduser -s /sbin/nologin kafka
# tar -xzf kafka_2.13-3.6.1.tgz -C /opt/
# mv /opt/kafka_2.13-3.6.1 /opt/kafka
# chown -R kafka:kafka /opt/kafka

# mkdir -p {/data/kafka,/data/zookeeper}
# chown -R kafka:kafka {/data/kafka,/data/zookeeper}
```

### 3. Устанавливаем Zookeeper

  Создаем systemd unit zookeeper.service: 
```console
# cat /etc/systemd/system/zookeeper.service
[Unit]
Requires=network.target remote-fs.target
After=network.target remote-fs.target

[Service]
Environment=JAVA_HOME=/usr/lib/jvm/jdk-17.0.2
Type=simple
User=kafka
ExecStart=/opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
ExecStop=/opt/kafka/kafka/bin/zookeeper-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multy-user.target
```

  Изменяем (создаем) конфигурационный файл для zookeeper:
```console
# cat /opt/kafka/config/zookeeper.properties
# the directory where the snapshot is stored.
dataDir=/data/zookeeper
# the port at which the clients will connect
clientPort=2181
# disable the per-ip limit on the number of connections since this is a non-production config
maxClientCnxns=0
# Disable the adminserver by default to avoid port conflicts.
# Set the port to something non-conflicting if choosing to enable this
admin.enableServer=false
# admin.serverPort=8080
```

  Запускаем zookeeper и проверяем успешность его запуска:
```console
# systemctl daemon-reload
# systemctl start zookeeper.service
# systemctl status zookeeper.service
● zookeeper.service
     Loaded: loaded (/etc/systemd/system/zookeeper.service; enabled; preset: disabled)
     Active: active (running) since Tue 2024-03-12 18:44:13 MSK; 4s ago
   Main PID: 348808 (java)
      Tasks: 31 (limit: 38040)
     Memory: 65.5M
        CPU: 1.569s
     CGroup: /system.slice/zookeeper.service
             └─348808 /usr/lib/jvm/jdk-17.0.2/bin/java -Xmx512M -Xms512M -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:MaxInlineLe>


# echo srvr | nc localhost 2181
Zookeeper version: 3.8.3-6ad6d364c7c0bcf0de452d54ebefa3058098ab56, built on 2023-10-05 10:34 UTC
Latency min/avg/max: 0/0.0/0
Received: 1
Sent: 0
Connections: 1
Outstanding: 0
Zxid: 0x0
Mode: standalone
Node count: 5
```

### 4. Устанавливаем Kafka

  Создаем systemd unit kafka.service
```console
# cat /etc/systemd/system/kafka.service
[Unit]
Requires=zookeeper.service
After=zookeeper.service

[Service]
Environment=JAVA_HOME=/usr/lib/jvm/jdk-17.0.2
Type=simple
User=kafka
ExecStart=/bin/sh -c '/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties > /opt/kafka/logs/kafka.log 2>&1'
ExecStop=/opt/kafka/bin/kafka-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multy-user.target
```

  Изменяем(создаем) конфигурационный файл для кафки:
```console
# grep -vE '^(#|$)' /opt/kafka/config/server.properties 
broker.id=0
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=/data/kafka
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=18000
group.initial.rebalance.delay.ms=0
```

  Запускаем кафку и проверяем успешность запуска:
```console
# systemctl daemon-reload
# systemctl start kafka.service
# systemctl status kafka.service
● kafka.service
     Loaded: loaded (/etc/systemd/system/kafka.service; disabled; preset: disabled)
     Active: active (running) since Tue 2024-03-12 18:51:57 MSK; 5s ago
   Main PID: 349830 (sh)
      Tasks: 84 (limit: 38040)
     Memory: 324.6M
        CPU: 3.855s
     CGroup: /system.slice/kafka.service
             ├─349830 /bin/sh -c "/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties > /opt/kafka/logs/kafka.log 2>&1"
             └─349831 /usr/lib/jvm/jdk-17.0.2/bin/java -Xmx1G -Xms1G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:MaxInlineLevel=>


# /opt/kafka/bin/zookeeper-shell.sh localhost:2181 get /brokers/ids/0
Connecting to localhost:2181

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
{"features":{},"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://localhost:9092"],"jmx_port":-1,"port":9092,"host":"localhost","version":5,"timestamp":"1710258719676"}
```

### 5. Создаем топик "test"
```console
# export JAVA_HOME=/usr/lib/jvm/jdk-17.0.2
# /opt/kafka/bin/kafka-topics.sh --bootstrap-server=127.0.0.1:9092 --create --replication-factor 1 --partitions 1 --topic test
Created topic test.
# /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server=127.0.0.1:9092
test
```

### 6. Записывем и читаем тестовые сообщения

  В двух разных консолях запускаем consumer и producer.
  После этого в producer создаем сообщения, а в consumer читаем их.
```console
[root@kafka]# /opt/kafka/bin/kafka-console-producer.sh  --bootstrap-server=127.0.0.1:9092 --topic=test
>hello1
>hello2
>hellohello3
>

[root@kafka]                                                                                                                                                                                      
# /opt/kafka/bin/kafka-console-consumer.sh  --bootstrap-server=127.0.0.1:9092 --topic=test --from-beginning
hello1
hello2
hellohello3
```

  Также запускаем "kafdrop" и проверяем "наличие" сообщений.
```console
# podman  run --rm --name kafdrop --net=host -e KAFKA_BROKERCONNECT="127.0.0.1:9092" -p 9000:9000 -e VM_OPTS="-Xms128M -Xmx256M -Xss180K -XX:-TieredCompilation -XX:+UseStringDeduplication -noverify" obsidiandynamics/kafdrop:3.31.0
```
  Скрин "kafdrop" сохранен в поддиректории:
```console
hw1/images/kafdrop.png
````

