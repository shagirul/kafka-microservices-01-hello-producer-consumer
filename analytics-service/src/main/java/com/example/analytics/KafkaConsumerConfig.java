package com.example.analytics;

// --- Kafka & Spring imports ---
import org.apache.kafka.clients.consumer.ConsumerConfig;
//  → Part of Kafka core client library.
//    Defines constants for all standard consumer configuration keys
//    (like bootstrap servers, group.id, auto commit etc).

import org.apache.kafka.common.serialization.StringDeserializer;
//  → Converts byte[] (from Kafka messages) into Java Strings.
//    Kafka sends data as bytes — so deserializers decode it for us.

import org.springframework.beans.factory.annotation.Value;
//  → Lets us inject values from application.properties (e.g. bootstrap servers).

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//  → Marks this class as a configuration source for Spring Boot.
//    It tells Spring to scan it and register the beans defined here.

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//  → Spring abstraction that manages concurrent Kafka message listeners
//    (multiple threads consuming from partitions concurrently).

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//  → Factory classes that create KafkaConsumer instances
//    based on configuration properties.

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConsumerConfig:
 * --------------------
 * This class defines how the analytics service connects to Kafka as a consumer.
 *
 * Think of this as setting up a "radio receiver" to listen to a broadcast.
 * Kafka is the radio station, and this config tells Spring:
 * - what frequency (topic/server) to tune into,
 * - how to decode the message,
 * - and how many receivers (threads) can listen at once.
 */
@Configuration
public class KafkaConsumerConfig {

    // --- Inject properties from application.yml ---
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap; // e.g., "kafka:9092"
    // -> Where Kafka broker is running. It's like the station frequency.

    @Value("${app.consumer.group}")
    private String groupId; // e.g., "analytics-consumer-group"
    // -> Identifies the consumer group. Kafka uses this to coordinate message delivery.
    //    Analogy: If 3 listeners share a group, each gets a part of the playlist.
    //    If each has a different group, all get the same playlist (broadcast mode).

    /**
     * ConsumerFactory bean:
     * ---------------------
     * Creates Kafka Consumer instances using provided configuration.
     *
     * Analogy:
     *  - Factory = kitchen template that knows how to make consumers with your recipe.
     *  - Each consumer created will follow this configuration (bootstrap, deserializer, etc.)
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Core connection settings
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        // → The Kafka broker address.

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // → Group ID to manage partition assignment and offsets.

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // → Kafka messages come as byte arrays. We tell Kafka how to convert them.
        //   Key = e.g., stock symbol; Value = JSON string.

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // → Kafka can auto-commit offsets (like bookmarking progress).
        //   True means: after every poll, it will mark messages as read automatically.

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // → If no offset exists (new consumer), start reading from the beginning.
        //   Alternative: "latest" (only new messages).

        // Return a Spring wrapper factory that builds Kafka consumers.
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory:
     * ---------------------------------
     * This creates the underlying listener mechanism for @KafkaListener methods.
     *
     * Analogy:
     *  - Imagine a "call center" (listener container factory)
     *    that creates and manages multiple operators (Kafka consumers)
     *    who listen to incoming messages.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        // Container factory manages concurrency and error handling.
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // Link the consumer factory we defined above.
        factory.setConsumerFactory(consumerFactory());

        // Control parallelism: how many threads per listener.
        // Each listener will consume from one or more partitions.
        factory.setConcurrency(1);
        // Tip:
        // Increase concurrency to match number of partitions
        // if you want parallel message consumption.

        return factory;
    }
}
