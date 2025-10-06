package com.example.orders;

// ================================
// 📦 Imports
// ================================
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;   // Used to convert Java objects into JSON strings.
import org.apache.kafka.clients.producer.ProducerRecord; // Represents a message that will be sent to Kafka.
import org.apache.kafka.clients.producer.RecordMetadata; // Metadata returned after message is sent (topic, partition, offset).
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;  // To read topic name from application.yml
import org.springframework.kafka.core.KafkaTemplate;      // High-level API to send messages to Kafka.
import org.springframework.stereotype.Component;         // Marks this as a Spring-managed bean (auto-detected).

// ================================
// 🧱 Component: OrderPublisher
// ================================
// This class is responsible for PUBLISHING messages (order events) to Kafka.
// Think of it as the "post office clerk" that takes your letter (event),
// wraps it nicely (JSON), and sends it to the correct mailbox (topic).
@Component
public class OrderPublisher {

    // 🔍 Logger to print info & errors — helps track message publishing.
    private static final Logger log = LoggerFactory.getLogger(OrderPublisher.class);

    // ✅ KafkaTemplate is injected — it’s the helper object used to send messages.
    private final KafkaTemplate<String, String> template;

    // 🧩 ObjectMapper converts our OrderEvent Java object → JSON text.
    private final ObjectMapper mapper = new ObjectMapper();

    // 🧭 The Kafka topic name, loaded dynamically from application.yml
    @Value("${app.topic.orders}")
    private String ordersTopic;

    // 🧱 Constructor-based dependency injection — Spring injects the KafkaTemplate bean we defined in KafkaProducerConfig.
    public OrderPublisher(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    // ===============================================
    // 🚀 Core Logic: Publish an OrderEvent to Kafka
    // ===============================================
    public void publish(OrderEvent event) {
        try {
            // 1️⃣ Convert our OrderEvent (Java object) → JSON string.
            // Example:
            // OrderEvent{id="o-1001", symbol="AAPL"} → {"orderId":"o-1001","symbol":"AAPL"}
            String json = mapper.writeValueAsString(event);

            // 2️⃣ Use orderId as the message key — ensures all messages for the same order go to the same Kafka partition.
            String key = event.getOrderId();

            // 3️⃣ Create a Kafka message (ProducerRecord)
            // A ProducerRecord contains:
            //   - Topic: where to send it (orders.v1)
            //   - Key: message key (used for partitioning)
            //   - Value: actual message payload (our JSON string)
            ProducerRecord<String, String> record = new ProducerRecord<>(ordersTopic, key, json);

            // 4️⃣ Send the record using KafkaTemplate
            // This is asynchronous — we get a CompletableFuture-like callback.
            template.send(record).whenComplete((result, ex) -> {
                // When send completes, we either get metadata or an exception.
                if (ex != null) {
                    // ❌ If something failed (e.g. broker down, timeout)
                    log.error("Failed to publish order {}", event.getOrderId(), ex);
                } else {
                    // ✅ If success, log topic, partition, and offset.
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("Published order key={} topic={} partition={} offset={}",
                            key, metadata.topic(), metadata.partition(), metadata.offset());
                }
            });

        } catch (JsonProcessingException e) {
            // 🎯 If the event couldn’t be serialized to JSON
            throw new RuntimeException("Error serializing order event", e);
        }
    }
}
