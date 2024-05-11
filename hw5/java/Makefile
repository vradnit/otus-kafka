.PHONY: build
build:
	mvn compile assembly:single

.PHONY: eventsCounter
eventsCounter:
	java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.kafkastream.EventsCounter

.PHONY: producer
producer:
	java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.kafkastream.Producer

.PHONY: consumer
consumer:
	java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.kafkastream.Consumer

.PHONY: clean
clean:
	rm -rf target
