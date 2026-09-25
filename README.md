# Examsy — Microservices Architecture

[![Watch a one-minute video tour of examsy-microservice](https://gitdiagram.com/video-badge.svg)](https://gitdiagram.com/ruvinda-shaluka/examsy-microservice/video)

This directory houses the microservices migration for the **Examsy Examination Platform**.

## Project Structure

```text
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
└── .gitignore
```

## System Architecture

> 🎬 **Architecture Walkthrough:** [![Watch a one-minute video tour of examsy-microservice](https://gitdiagram.com/video-badge.svg)](https://gitdiagram.com/ruvinda-shaluka/examsy-microservice/video) *(Local video file: [`media/ruvinda-shaluka-examsy-microservice-explained.mp4`](media/ruvinda-shaluka-examsy-microservice-explained.mp4))*

```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'primaryColor': '#ffffff',
    'fontSize': '15px'
  },
  'flowchart': {
    'defaultRenderer': 'dagre',
    'nodeSpacing': 50,
    'rankSpacing': 90,
    'curve': 'basis',
    'useMaxWidth': false
  }
}}%%
flowchart TD

subgraph group_edge["Entry and identity"]
  node_gateway["API gateway"]
  node_auth["Authentication"]
  node_authdb[("User accounts")]
  node_authproducer["Registration events"]
end

subgraph group_learning["Classes and exams"]
  node_classes["Class management"]
  node_classdb[("Class records<br/>[CourseRepo.java]")]
  node_exam["Exam lifecycle"]
  node_examdb[("Exams and submissions")]
end

subgraph group_assessment["Grading and results"]
  node_submissionevent["Submission event"]
  node_grading["Smart grading"]
  node_ocr["Answer OCR"]
  node_gradeevent["Grade release event"]
end

subgraph group_operations["Profiles and administration"]
  node_profile["User profiles"]
  node_profiledb[("Profile records<br/>[StudentRepo.java]")]
  node_admin["Reports and dashboard"]
  node_metricsdb[("Platform metrics")]
  node_notifications["Notifications"]
  node_email["Email delivery"]
end

subgraph group_platform["Platform services"]
  node_kafka["Kafka broker"]
  node_discovery["Service discovery"]
  node_config["Central configuration"]
end

node_user(("Student / teacher / admin"))

node_user -->|"sends requests"| node_gateway
node_gateway -->|"routes requests"| node_auth
node_gateway -->|"routes requests"| node_classes
node_gateway -->|"routes requests"| node_exam
node_gateway -->|"routes requests"| node_admin
node_auth -->|"reads/writes"| node_authdb
node_auth -->|"publishes registration"| node_authproducer
node_authproducer -->|"publishes events"| node_kafka
node_kafka -->|"delivers registration"| node_profile
node_profile -->|"writes profiles"| node_profiledb
node_classes -->|"reads/writes"| node_classdb
node_exam -->|"reads/writes"| node_examdb
node_exam -->|"publishes submission"| node_submissionevent
node_submissionevent -->|"publishes event"| node_kafka
node_kafka -->|"delivers submission"| node_grading
node_grading -->|"extracts answers"| node_ocr
node_grading -->|"publishes release"| node_gradeevent
node_gradeevent -->|"publishes event"| node_kafka
node_kafka -->|"delivers events"| node_notifications
node_notifications -->|"sends email"| node_email
node_admin -->|"reads/writes metrics"| node_metricsdb
node_gateway -.->|"uses discovery"| node_discovery
node_auth -.->|"loads configuration"| node_config
node_classes -.->|"loads configuration"| node_config
node_exam -.->|"loads configuration"| node_config

click node_gateway "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-api-gateway/src/main/java/lk/ijse/examsy/gateway/config/GatewayRoutesConfig.java"
click node_auth "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-auth-service/src/main/java/lk/ijse/examsy/auth/service/impl/AuthServiceImpl.java"
click node_authdb "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-auth-service/src/main/java/lk/ijse/examsy/auth/repository/UserAccountRepo.java"
click node_authproducer "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-auth-service/src/main/java/lk/ijse/examsy/auth/kafka/AuthEventProducer.java"
click node_classes "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-class-service/src/main/java/lk/ijse/examsy/classservice/service/impl/StudentClassServiceImpl.java"
click node_classdb "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-class-service/src/main/java/lk/ijse/examsy/classservice/repository/CourseRepo.java"
click node_exam "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-exam-service/src/main/java/lk/ijse/examsy/examservice/service/impl/StudentExamServiceImpl.java"
click node_examdb "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-exam-service/src/main/java/lk/ijse/examsy/examservice/repository/ExamSubmissionRepo.java"
click node_submissionevent "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-exam-service/src/main/java/lk/ijse/examsy/examservice/kafka/ExamEventProducer.java"
click node_grading "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-grading-service/src/main/java/lk/ijse/examsy/gradingservice/service/impl/SmartGradingServiceImpl.java"
click node_ocr "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-grading-service/src/main/java/lk/ijse/examsy/gradingservice/service/impl/OCRServiceImpl.java"
click node_gradeevent "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-grading-service/src/main/java/lk/ijse/examsy/gradingservice/kafka/GradeEventProducer.java"
click node_profile "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-profile-service/src/main/java/lk/ijse/examsy/profile/kafka/AuthEventConsumer.java"
click node_profiledb "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-profile-service/src/main/java/lk/ijse/examsy/profile/repository/StudentRepo.java"
click node_admin "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-admin-service/src/main/java/lk/ijse/examsy/adminservice/service/impl/AdminReportServiceImpl.java"
click node_metricsdb "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-admin-service/src/main/java/lk/ijse/examsy/adminservice/repository/PlatformMetricRepo.java"
click node_notifications "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-notification-service/src/main/java/lk/ijse/examsy/notificationservice/service/impl/NotificationServiceImpl.java"
click node_email "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-notification-service/src/main/java/lk/ijse/examsy/notificationservice/service/impl/EmailServiceImpl.java"
click node_discovery "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-eureka-server/src/main/java/lk/ijse/examsy/eurekaserver/ExamsyEurekaServerApplication.java"
click node_config "https://github.com/ruvinda-shaluka/examsy-microservice/blob/main/services/examsy-config-server/src/main/java/lk/ijse/examsy/configserver/ExamsyConfigServerApplication.java"

classDef toneNeutral fill:#f8fafc,stroke:#334155,stroke-width:1.5px,color:#0f172a
classDef toneBlue fill:#dbeafe,stroke:#2563eb,stroke-width:1.5px,color:#172554
classDef toneAmber fill:#fef3c7,stroke:#d97706,stroke-width:1.5px,color:#78350f
classDef toneMint fill:#dcfce7,stroke:#16a34a,stroke-width:1.5px,color:#14532d
classDef toneRose fill:#ffe4e6,stroke:#e11d48,stroke-width:1.5px,color:#881337
classDef toneIndigo fill:#e0e7ff,stroke:#4f46e5,stroke-width:1.5px,color:#312e81
classDef toneTeal fill:#ccfbf1,stroke:#0f766e,stroke-width:1.5px,color:#134e4a
class node_gateway,node_auth,node_authdb,node_authproducer toneBlue
class node_classes,node_classdb,node_exam,node_examdb toneAmber
class node_submissionevent,node_grading,node_ocr,node_gradeevent toneMint
class node_profile,node_profiledb,node_admin,node_metricsdb,node_notifications,node_email toneRose
class node_kafka,node_discovery,node_config,node_user toneIndigo
```
