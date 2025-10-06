#############################################
# 🏗️ ===== STAGE 1: BUILD STAGE =====
#############################################
# 🎯 Purpose:
# This stage is used only to **compile and package** the Java app into a JAR.
# It’s like your kitchen — where you prepare the meal before serving.

# 🧰 Use an official Maven image that comes with JDK 17 (Temurin = Eclipse OpenJDK build)
FROM maven:3.9.6-eclipse-temurin-17 AS build

# 🏠 Define the working directory inside the container (like doing `cd /app`)
WORKDIR /app

# 📦 Copy only the Maven descriptor file first
# This allows Docker to cache dependencies if pom.xml hasn’t changed.
COPY pom.xml .

# ⚙️ Pre-download dependencies for faster builds (cached layer)
RUN mvn dependency:go-offline -B
# `-B` means "batch mode" (no interactive prompts)

# 🧩 Now copy your actual source code
# Doing this after dependencies means that only code changes trigger rebuilds
COPY src ./src

# 🏗️ Build the project (compile + package into JAR)
# - `clean` removes old builds
# - `package` creates target/*.jar
# - `-DskipTests` avoids running tests (useful in CI/CD or demo builds)
RUN mvn clean package -DskipTests


#############################################
# 🚀 ===== STAGE 2: RUNTIME STAGE =====
#############################################
# 🎯 Purpose:
# This stage is lightweight and used only to **run** the prebuilt JAR.
# Think of it as the “serving plate” — no need for kitchen tools here.

# ☕ Use a JRE-only image (no Maven or compiler needed)
# Eclipse Temurin (OpenJDK) — lightweight, secure, and optimized for production
FROM eclipse-temurin:17-jre-jammy

# 🏠 Create a working directory for the runtime container
WORKDIR /app

# 📥 Copy the built JAR from the build stage
# --from=build → references the previous stage name
COPY --from=build /app/target/order-service-0.0.1-SNAPSHOT.jar app.jar

# 🌐 Document which port the app will listen on
# (Does not actually open it — this is metadata for humans & tools)
EXPOSE 8081

# 🚪 Define what command should run when the container starts
# It executes your Spring Boot JAR with the Docker profile enabled
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=docker"]
