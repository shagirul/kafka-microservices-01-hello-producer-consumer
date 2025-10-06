# 🚀 Kafka Microservices Lab

A lightweight **event-driven microservices demo** built with **Spring Boot**, **Apache Kafka (KRaft mode)**, and **Docker Compose**. It demonstrates **producer-consumer communication**, **topic management**, and **real-time analytics** using a local Kafka cluster.

---

## 🧩 Architecture Overview

| Service | Role | Description |
|----------|------|-------------|
| **kafka** | Broker + Controller | Core Kafka node (runs in KRaft mode, no Zookeeper). |
| **init-topics** | Topic initializer | Creates topics (`orders.v1`) before services start. |
| **kafka-ui** | Visualization UI | Web dashboard to explore topics, partitions, messages, and consumer groups. |
| **order-service** | Producer | Publishes order events to Kafka (`orders.v1`). |
| **analytics-service** | Consumer | Listens to `orders.v1` and logs received events. |

---

## 🏗️ System Flow

```
[Order Service]  →  [Kafka Topic: orders.v1]  →  [Analytics Service]
                         |
                         └──→ View events in [Kafka UI]
```

---

## ⚙️ How It Works

### 1. Kafka Broker
Runs in **KRaft mode** (modern Kafka without ZooKeeper). Exposes port `9092` for communication between producer and consumer.

### 2. Topic Initialization
The `init-topics` container creates Kafka topics before other services start:
```bash
/opt/kafka/bin/kafka-topics.sh --create --topic orders.v1 --partitions 3 --replication-factor 1
```

### 3. Order Producer
Exposes a REST API (`POST /orders`) that sends order events to Kafka.

### 4. Analytics Consumer
Subscribes to the topic `orders.v1` and logs events with partition and offset metadata.

---

## 🧠 Tech Stack
- **Java 17 / Spring Boot 3**
- **Apache Kafka 3.9 (KRaft mode)**
- **Docker & Docker Compose**
- **Maven 3.9**
- **Provectus Kafka UI**

---

## 🐳 Docker Commands Cheat Sheet

### 🧱 Build and Start Services
```bash
docker compose build --no-cache      # Rebuild all services from scratch
docker compose up -d                 # Start all containers in detached mode
docker compose up --build            # Rebuild images + start services
```

### 🧭 Monitor and Debug
```bash
docker ps                            # List running containers
docker ps -a                         # List all containers (including stopped)
docker logs <container_name>         # Show logs of a specific container
docker logs -f <container_name>      # Stream logs live (like tail -f)
docker inspect <container_name>      # Show low-level container details
docker exec -it <container_name> sh  # Open shell inside a running container
```

### 🧼 Clean Up
```bash
docker compose down -v               # Stop and remove containers, networks, and volumes
docker image prune -f                # Remove unused images
docker volume prune -f               # Remove dangling volumes
docker system prune -a -f            # Remove ALL unused data (CAREFUL)
```

### 🧩 Kafka-Specific Commands (inside Kafka container)
```bash
docker exec -it kafka sh                             # Enter Kafka shell
/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server kafka:9092
/opt/kafka/bin/kafka-console-consumer.sh --topic orders.v1 --from-beginning --bootstrap-server kafka:9092
/opt/kafka/bin/kafka-console-producer.sh --topic orders.v1 --bootstrap-server kafka:9092
```

---

## 🧪 Test Flow

### Publish Orders
```bash
curl -X POST http://localhost:8081/orders -H 'Content-Type: application/json' -d '{"orderId":"o-1001","symbol":"AAPL","side":"BUY","qty":10,"price":188.25}'
curl -X POST http://localhost:8081/orders -H 'Content-Type: application/json' -d '{"orderId":"o-1002","symbol":"MSFT","side":"SELL","qty":5,"price":321.10}'
```

### Observe Analytics Logs
```bash
docker logs -f kafka-microservices-lab-analytics-service-1
```

### Check Topics via Kafka UI
Open [http://localhost:8080](http://localhost:8080) and explore:
- **Topics** → `orders.v1`
- **Consumers** → analytics group
- **Messages** → check payload & partition offsets

---

## 🧹 Cleanup
```bash
docker compose down -v         # Remove containers + volumes
docker image prune -f          # Delete dangling images
docker volume prune -f         # Clean up unused volumes
docker system prune -a -f      # Full cleanup of all unused resources
```

---

## 🧩 Future Enhancements
- Add **schema registry** (Avro / JSON Schema)
- Implement **stream processing** with Kafka Streams
- Add **monitoring** via Prometheus + Grafana
- Extend **multi-topic consumers** for analytics modules

---

## 🧭 Learnings
This project helps understand:
- Kafka **producer-consumer patterns**
- Topic creation and partitioning
- Kafka **KRaft mode**
- **Microservice orchestration** with Docker
- **Debugging and monitoring** distributed systems

---

## 📄 License
MIT License © 2025

