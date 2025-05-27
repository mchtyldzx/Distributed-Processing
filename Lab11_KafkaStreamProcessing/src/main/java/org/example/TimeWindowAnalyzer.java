package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class TimeWindowAnalyzer {
    private static final String TOPIC = "stream-test";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_ID = "streams-time-window";

    public static void main(String[] args) {
        // Create properties
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        
        // Create a StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Create stream from input topic
        KStream<String, String> inputStream = builder.stream(TOPIC);
        
        // Set the window size to 1 minute (60000 milliseconds)
        Duration windowSize = Duration.ofMinutes(1);
        
        // Use tumbling time windows to count messages per minute
        KTable<Windowed<String>, Long> windowedCounts = inputStream
                .selectKey((key, value) -> "message-count") // Use a constant key for all messages
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(windowSize))
                .count();
        
        // Format the result for console output
        windowedCounts.toStream().foreach((windowedKey, count) -> {
            String windowStart = Instant.ofEpochMilli(windowedKey.window().start())
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String windowEnd = Instant.ofEpochMilli(windowedKey.window().end())
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            System.out.println("Time window [" + windowStart + " - " + windowEnd + "]: " + count + " messages");
        });

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
            System.out.println("Time window analysis started - counting messages per minute");
            latch.await();
        } catch (Exception e) {
            System.err.println("Error occurred during stream processing: " + e.getMessage());
            System.exit(1);
        }
    }
} 