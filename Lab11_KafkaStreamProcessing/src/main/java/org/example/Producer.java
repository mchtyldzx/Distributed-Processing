package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class Producer {
    private static final String TOPIC = "stream-test";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        // Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter messages to send (type 'exit' to quit):");

        // Read input and send messages
        while (true) {
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }

            // Create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);

            // Send data - asynchronous
            producer.send(record, (recordMetadata, e) -> {
                if (e == null) {
                    System.out.println("Message sent successfully to: " + recordMetadata.topic());
                } else {
                    System.err.println("Error while producing: " + e.getMessage());
                }
            });
            
            // Flush data
            producer.flush();
        }

        // Close producer
        producer.close();
        scanner.close();
    }
} 