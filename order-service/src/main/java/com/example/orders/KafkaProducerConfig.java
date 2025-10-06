package com.example.orders;

// =======================
// ✅ Imports
// =======================
// These are the Kafka and Spring Boot dependencies we need.
import org.apache.kafka.clients.admin.NewTopic;                     // Used to programmatically create a topic if it doesn't exist.
import org.apache.kafka.clients.producer.ProducerConfig;            // Contains predefined config keys for Kafka producers.
import org.apache.kafka.common.serialization.StringSerializer;      // Serializes Java Strings into bytes for Kafka.
import org.springframework.beans.factory.annotation.Value;          // Used to read values from application.yml.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;                 // Simplifies topic creation.
import org.springframework.kafka.core.DefaultKafkaProducerFactory;  // Creates producers based on our config.
import org.springframework.kafka.core.KafkaTemplate;                // Abstraction that simplifies publishing messages.
import org.springframework.kafka.core.ProducerFactory;              // Interface for creating Kafka producers.

import java.util.HashMap;
import java.util.Map;

// =======================
// ⚙️ Spring Configuration Class
// =======================
// This tells Spring Boot that this class defines beans (config objects)
// to set up Kafka producer properties.
@Configuration
public class KafkaProducerConfig {

    // =======================
    // 🔌 Inject configuration values from application.yml
    // =======================
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;   // Kafka broker address (e.g. kafka:9092 inside Docker)

    @Value("${app.topic.orders}")
    private String ordersTopic; // The topic name where we'll send messages (e.g. "orders.v1")

    // =======================
    // 🏭 1. Producer Factory
    // =======================
    // Think of this as the "factory" that creates Kafka producer clients
    // — each one knows *how* to connect and *how* to serialize messages.
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        // Configuration map for Kafka producer.
        Map<String, Object> props = new HashMap<>();

        // 1️⃣ Which Kafka broker(s) to connect to.
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);

        // 2️⃣ Serializer for key/value (must match consumer deserializer).
        // Kafka stores bytes — serializers convert objects → bytes.
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // =======================
        // 🧱 Safe Producer Settings (important for reliability)
        // =======================
        // ACKS_CONFIG: "all" means wait for confirmation from *all* replicas before considering a message sent.
        //   Analogy: You don’t mark a letter as “delivered” until *every post office branch* confirms receipt.
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // ENABLE_IDEMPOTENCE: ensures no duplicate messages even during retries.
        //   Analogy: If a network glitch happens, the same message won’t be sent twice accidentally.
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // RETRIES: number of times to retry sending if it temporarily fails.
        //   A small safety net for transient network or broker hiccups.
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Return a factory that creates producers using this configuration.
        return new DefaultKafkaProducerFactory<>(props);
    }

    // =======================
    // ✉️ 2. KafkaTemplate Bean
    // =======================
    // KafkaTemplate is a Spring abstraction — it hides all the low-level
    // producer API details and gives you a simple `.send(topic, key, message)` method.
    //
    // Analogy: Instead of writing TCP socket code to send a message,
    // you get a ready-made "sendMessage()" button.
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =======================
    // 🪣 3. Optional Topic Creation
    // =======================
    // Kafka can auto-create topics (if allowed), but defining them explicitly
    // ensures consistent partition count and replication settings.
    //
    // Analogy: Instead of waiting for the system to create a mailbox when you send a letter,
    // you create the mailbox upfront with a known size.
    @Bean
    public NewTopic ordersTopic() {
        // TopicBuilder helps define a topic with:
        // - 3 partitions (for scalability)
        // - 1 replica (since we’re in a local/dev setup)
        return TopicBuilder.name(ordersTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
