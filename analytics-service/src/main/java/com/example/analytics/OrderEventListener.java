package com.example.analytics;
// 📦 The package groups related code logically.
// In a large project, this helps structure components by domain (here, analytics service).

import org.apache.kafka.clients.consumer.ConsumerRecord;
// 📨 Represents one message fetched from a Kafka topic (like an email with metadata).

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 🪵 Logging API – used for structured runtime logs instead of println().
// It’s a facade (front-end) for different logging frameworks (Logback, Log4j, etc.).

import org.springframework.beans.factory.annotation.Value;
// 💡 Lets you inject values from configuration files (application.yml / properties).
// Example: If config says app.topic.orders=orders.v1 → this will inject "orders.v1" into a variable.

import org.springframework.kafka.annotation.KafkaListener;
// 🎧 Annotation that tells Spring Boot this method listens to Kafka messages automatically.
// It sets up background threads, polling loops, error handling, and deserialization.

import org.springframework.stereotype.Component;
// 🧩 Marks this class as a Spring-managed bean so it’s auto-detected during component scanning.

@Component
// ✅ Spring will create one instance of this class at startup.
// Think of it as plugging in a “message receiver” device when your service boots.

public class OrderEventListener {

    // 🎤 Logger helps track message processing and debug issues.
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    // 🧩 The topic name is injected from configuration (externalized, not hardcoded).
    @Value("${app.topic.orders}")
    private String ordersTopic;

    // 🧠 @KafkaListener creates a background thread that subscribes to a Kafka topic.
    // It automatically polls messages and invokes this method for each message.
    @KafkaListener(
            topics = "#{@environment.getProperty('app.topic.orders')}",  // 👂 topic name from environment (dynamic config)
            groupId = "${app.consumer.group}",                           // 👥 consumer group ID for load balancing
            properties = {
                    "max.poll.records:100",                               // 🧺 Max messages to fetch per poll
                    "auto.offset.reset:earliest"                          // ⏮ Start from earliest offset if no commit exists
            })
    public void onMessage(ConsumerRecord<String, String> record) {
        // 💬 Kafka passes a record = single message + metadata (key, value, partition, offset)

        log.info(
                "📦 Received OrderEvent | key={} | partition={} | offset={} | payload={}",
                record.key(),        // 🔑 message key (usually symbol or orderId)
                record.partition(),  // 🧭 which partition this message came from
                record.offset(),     // 📑 sequential offset in that partition
                record.value()       // 🧾 the message itself (JSON string)
        );

        // ✅ Example output:
        // 📦 Received OrderEvent | key=o-1001 | partition=0 | offset=15 | payload={"orderId":"o-1001",...}
    }
}
