package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamProcessor {
    private static final String TOPIC = "stream-test";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_ID = "streams-word-count";

    public static void main(String[] args) {
        // Create properties
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create a StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Create stream from input topic
        KStream<String, String> inputStream = builder.stream(TOPIC);

        // Process the stream - count words
        KStream<String, Long> wordCounts = inputStream
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .filter((key, word) -> !word.isEmpty())
                .groupBy((key, word) -> word)
                .count()
                .toStream();

        // Print the results to the console
        wordCounts.foreach((word, count) -> System.out.println("Word: " + word + " | Count: " + count));

        // Build the topology
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        // Set up a clean shutdown hook
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
            latch.countDown();
        }));

        try {
            streams.start();
            System.out.println("Stream processing started");
            latch.await();
        } catch (Exception e) {
            System.err.println("Error occurred during stream processing: " + e.getMessage());
            System.exit(1);
        }
    }
} 