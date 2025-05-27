# Kafka Stream Processing Application

This project is a Kafka-based distributed data processing system. The project consists of several components for processing data in streams.

## Project Components

1. **Producer**: A producer that sends messages to the "stream-test" topic
2. **Consumer**: A consumer that reads messages from the "stream-test" topic
3. **StreamProcessor**: A processor that counts words in messages
4. **MessageFilter**: A filter that filters messages containing specific keywords and sends them to the "stream-output" topic
5. **FilteredConsumer**: A consumer that reads filtered messages from the "stream-output" topic
6. **TimeWindowAnalyzer**: A time window analyzer that calculates the number of messages per minute

## Setup

1. Clone the project
2. Start Kafka and Zookeeper:

```bash
docker-compose up -d
```

3. Run these commands to create the necessary Kafka topics:

```bash
docker exec kafka kafka-topics --create --topic stream-test --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
docker exec kafka kafka-topics --create --topic stream-output --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

4. Compile the project:

```bash
mvn clean compile
```

## Running the Applications

Each component can be run in a separate terminal window:

1. Consumer:
```bash
mvn exec:java -Dexec.mainClass="org.example.Consumer"
```

2. FilteredConsumer:
```bash
mvn exec:java -Dexec.mainClass="org.example.FilteredConsumer"
```

3. MessageFilter (you will be prompted to enter a keyword):
```bash
mvn exec:java -Dexec.mainClass="org.example.MessageFilter"
```

4. StreamProcessor:
```bash
mvn exec:java -Dexec.mainClass="org.example.StreamProcessor"
```

5. TimeWindowAnalyzer:
```bash
mvn exec:java -Dexec.mainClass="org.example.TimeWindowAnalyzer"
```

6. Producer (for sending messages):
```bash
mvn exec:java -Dexec.mainClass="org.example.Producer"
```

## Test Scenarios

### Test 1: Basic Messaging
- Tests the basic messaging between Consumer and Producer
- Test messages: "Test message 1", "Hello world", "12345", "special characters !@#$"

### Test 2: Word Counting
- Tests the word counting function of StreamProcessor
- Test messages: "hello world", "hello kafka", "test test test", "one two three"

### Test 3: Message Filtering
- Tests message filtering based on a specific keyword
- Enter "test" as the filter keyword
- Test messages: "this is a test message", "normal message", "another message containing test", "unfiltered message"

### Test 4: Time Window
- Tests message counting in specific time intervals
- Send a few messages in the first 30 seconds, then send more messages in the next 30 seconds

## Comprehensive Test
The entire system can be tested by running all components together. Various messages are sent and it is verified that each component works correctly.

## Project Structure

- **src/main/java/org/example/**: Java source files
  - Producer.java: Message producer
  - Consumer.java: Message consumer
  - StreamProcessor.java: Stream processing
  - MessageFilter.java: Message filtering
  - FilteredConsumer.java: Filtered message consumer
  - TimeWindowAnalyzer.java: Time window analysis
- **docker-compose.yml**: Docker configuration for Kafka and Zookeeper
- **pom.xml**: Maven configuration and dependencies 