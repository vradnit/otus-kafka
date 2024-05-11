Домашнее задание
Разработка приложения Kafka Streams.

Цель:
Разработать приложение Kafka Streams

Описание/Пошаговая инструкция выполнения домашнего задания:
  Разработка приложения Kafka Streams:
    Запустить Kafka
    Создать топик events
    Разработать приложение, которое подсчитывает количество событий с одинаковыми key в рамках сессии 5 минут
    Для проверки отправлять сообщения, используя console producer


```console
# docker-compose -f ./kafka/docker-compose.yml up -d 
Creating kafka2 ... done
Creating kafka3 ... done
Creating kafka1 ... done
Creating kafdrop  ... done
Creating kafka-ui ... done
```
