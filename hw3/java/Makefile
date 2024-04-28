.PHONY: build
build:
	mvn compile assembly:single

.PHONY: producer
producer:
	java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.transaction.Producer

.PHONY: consumer
consumer:
	java -cp target/java-1.0-SNAPSHOT-jar-with-dependencies.jar ru.otus.transaction.Consumer

.PHONY: clean
clean:
	rm -rf target
