#############################################
# 🏗️ ===== STAGE 1: BUILD STAGE =====
#############################################
# In Docker, we can create images in multiple stages.
# This first stage is used *only* to compile and package the app using Maven.
# We’ll throw away everything else later to keep the final image small.

# ✅ Use the official Maven image with JDK 17 (Temurin = OpenJDK variant from Eclipse)
# The image comes preinstalled with Maven and JDK → no need to install manually.
FROM maven:3.9.6-eclipse-temurin-17 AS build

# 🏠 Set the working directory inside the container.
# All commands after this (like COPY, RUN) will run inside /app.
WORKDIR /app

# ⚡ Copy the pom.xml first to download dependencies before copying source code.
# Why? → So Docker can use cached layers.
# If source code changes but pom.xml doesn’t, dependencies won’t re-download.
COPY pom.xml .

# 📦 Download dependencies without building or running tests.
# This ensures all required libs are cached.
RUN mvn dependency:go-offline -B

# 🧩 Now copy the entire source code (since dependencies are already cached).
COPY src ./src

# 🧱 Run the actual Maven build — compile + package into a .jar file.
# The `-DskipTests` flag skips test execution for faster build times.
RUN mvn clean package -DskipTests


#############################################
# 🚀 ===== STAGE 2: RUNTIME STAGE =====
#############################################
# In this stage, we use a lightweight image (only JRE, no Maven or build tools).
# This ensures our container is small, fast, and secure.

# ✅ Eclipse Temurin JRE (Java Runtime Environment) 17 on Ubuntu Jammy (22.04)
FROM eclipse-temurin:17-jre-jammy

# 🏠 Create a new working directory for runtime.
WORKDIR /app

# 📥 Copy only the final JAR file from the build stage.
# The "--from=build" references the previous stage (build container).
COPY --from=build /app/target/analytics-service-0.0.1-SNAPSHOT.jar app.jar

# 🌍 Expose the port that the service will listen on.
# This doesn’t actually “open” the port — it’s documentation for Docker users.
EXPOSE 8082

# 🚪 Define the container entrypoint — the command that runs when container starts.
# This launches the Spring Boot app using Java.
# `--spring.profiles.active=docker` tells Spring to load docker-specific config if available.
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=docker"]
