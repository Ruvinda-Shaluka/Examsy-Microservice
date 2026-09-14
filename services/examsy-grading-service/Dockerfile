# Multi-stage build for Grading & AI Service container with native Tesseract OCR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install native Tesseract OCR libraries for Tess4J PDF extraction
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
