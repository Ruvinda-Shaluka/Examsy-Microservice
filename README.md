# Examsy — Microservices Architecture

This directory houses the microservices migration for the **Examsy Examination Platform**.

## Project Structure

```
Examsy-Microservice/
├── services/                     # Spring Boot microservices
│   ├── examsy-config-server/
│   ├── examsy-eureka-server/
│   ├── examsy-api-gateway/
│   ├── examsy-auth-service/
│   ├── examsy-profile-service/
│   ├── examsy-class-service/
│   ├── examsy-exam-service/
│   ├── examsy-grading-service/
│   ├── examsy-notification-service/
│   ├── examsy-admin-service/
│   └── examsy-analytics-service/
├── config-repo/                  # Git-backed config repo for Spring Cloud Config Server
├── infra/                        # Infrastructure definitions
│   └── docker/
│       └── mysql/
│           └── init.sql          # Multi-database initialization script
├── .github/
│   └── workflows/                # CI/CD action pipelines
├── docker-compose.yml            # Local dev stack (MySQL, Redis, Kafka, Services)

# Flow of the system
## System Architecture

```mermaid
flowchart TD

subgraph group_edge["Entry and Identity"]
  node_gateway["API Gateway"]
  node_auth["Authentication"]
  node_authdb[("User Accounts")]
  node_authproducer["Registration Events"]
end

subgraph group_learning["Classes and Exams"]
  node_classes["Class Management"]
  node_classdb[("Class Records")]
  node_exam["Exam Lifecycle"]
  node_examdb[("Exams and Submissions")]
end

subgraph group_assessment["Grading and Results"]
  node_submissionevent["Submission Event"]
  node_grading["Smart Grading"]
  node_ocr["Answer OCR"]
  node_gradeevent["Grade Release Event"]
end

subgraph group_operations["Profiles and Administration"]
  node_profile["User Profiles"]
  node_profiledb[("Profile Records")]
  node_admin["Reports and Dashboard"]
  node_metricsdb[("Platform Metrics")]
  node_notifications["Notifications"]
  node_email["Email Delivery"]
end

subgraph group_platform["Platform Services"]
  node_kafka["Kafka Broker"]
  node_discovery["Service Discovery"]
  node_config["Central Configuration"]
end

node_user(("Student / Teacher / Admin"))

node_user -->|"Sends Requests"| node_gateway

node_gateway -->|"Routes"| node_auth
node_gateway -->|"Routes"| node_classes
node_gateway -->|"Routes"| node_exam
node_gateway -->|"Routes"| node_admin

node_auth -->|"Reads / Writes"| node_authdb
node_auth -->|"Publishes"| node_authproducer
node_authproducer -->|"Registration Event"| node_kafka

node_kafka -->|"Profile Event"| node_profile
node_profile -->|"Writes"| node_profiledb

node_classes -->|"Reads / Writes"| node_classdb

node_exam -->|"Reads / Writes"| node_examdb
node_exam -->|"Submission Event"| node_submissionevent
node_submissionevent -->|"Publishes"| node_kafka

node_kafka -->|"Submission Event"| node_grading
node_grading -->|"Extracts Answers"| node_ocr
node_grading -->|"Grade Release"| node_gradeevent

node_gradeevent -->|"Publishes"| node_kafka
node_kafka -->|"Notification Events"| node_notifications
node_notifications -->|"Sends"| node_email

node_admin -->|"Reads / Writes"| node_metricsdb

node_gateway -.->|"Service Discovery"| node_discovery

node_auth -.->|"Configuration"| node_config
node_classes -.->|"Configuration"| node_config
node_exam -.->|"Configuration"| node_config
node_grading -.->|"Configuration"| node_config
node_notifications -.->|"Configuration"| node_config

classDef edge fill:#dbeafe,stroke:#2563eb,stroke-width:1.5px,color:#172554
classDef learning fill:#fef3c7,stroke:#d97706,stroke-width:1.5px,color:#78350f
classDef assessment fill:#dcfce7,stroke:#16a34a,stroke-width:1.5px,color:#14532d
classDef operations fill:#ffe4e6,stroke:#e11d48,stroke-width:1.5px,color:#881337
classDef platform fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#312e81

class node_gateway,node_auth,node_authdb,node_authproducer edge
class node_classes,node_classdb,node_exam,node_examdb learning
class node_submissionevent,node_grading,node_ocr,node_gradeevent assessment
class node_profile,node_profiledb,node_admin,node_metricsdb,node_notifications,node_email operations
class node_kafka,node_discovery,node_config,node_user platform
```

└── .gitignore
```
