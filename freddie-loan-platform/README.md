# 🏦 Freddie Mac Home Loan Platform (`freddie-loan-platform`)

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://jdk.java.net/17/)
[![Spring Boot 3.1.3](https://img.shields.io/badge/Spring%20Boot-3.1.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![ActiveMQ JMS](https://img.shields.io/badge/ActiveMQ-JMS-blueviolet.svg)](https://activemq.apache.org/)
[![Angular 15](https://img.shields.io/badge/Angular-15.2.0-red.svg)](https://angular.io/)
[![License: Enterprise](https://img.shields.io/badge/License-Freddie%20Mac%20Enterprise-red.svg)]()

Welcome to the **Freddie Mac Home Loan Platform** (UCount Mini Application) — a streamlined, high-performance enterprise 2-microservice ecosystem built with **Java 17**, **Spring Boot 3.1.3**, **ActiveMQ JMS**, **PostgreSQL**, and **Angular 15**, optimized into **EXACTLY 10 Java Files** while maintaining 100% of architectural design patterns and functional business flows.

---

## 1. 🏛️ Architecture of the Application

The platform is structured into **2 core microservice modules**:
1. **`loan-origination-service`** (Port `8082`): Customer onboarding, loan application origination, and PostgreSQL native query status management.
2. **`underwriting-service`** (Port `8083`): Java 17 pattern-matching underwriting risk engine, real-time rate/EMI pricing, 360-month amortization, and ActiveMQ JMS messaging.

```
┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                               UI FRONTEND LAYER                                                   │
│   ┌───────────────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                                   Angular 15 Enterprise Portal (`/frontend`)                              │   │
│   └─────────────────────────────────────────────────────┬─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────┘
                                                          │ (HTTP / REST)
                                                          ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                           MICROSERVICES CORE LAYER                                                │
│                                                                                                                   │
│   ┌──────────────────────────────────────────┐                ┌──────────────────────────────────────────┐        │
│   │ loan-origination-service (Port 8082)     │                │ underwriting-service (Port 8083)         │        │
│   │  • Customer Onboarding & Origination     │                │  • Java 17 Rule Engine & Risk Scoring    │        │
│   │  • Dual JPA ORM & Native SQL Queries     │                │  • Rate Pricing & 360-Mo Amortization    │        │
│   │  • Mass-Assignment Protection (@Valid)   │                │  • ActiveMQ JMS Event Publisher          │        │
│   └────────────────────┬─────────────────────┘                └────────────────────┬─────────────────────┘        │
└────────────────────────┼───────────────────────────────────────────────────────────┼──────────────────────────────┘
                         │                                                           │
                         ▼                                                           ▼
┌──────────────────────────────────────────────┐                ┌──────────────────────────────────────────┐
│ PostgreSQL Database (`freddie_loans` schema) │                │ ActiveMQ JMS Message Broker              │
│  • `loan_applications` Table                 │                │  • `FREDDIE-LOAN-NOTIFICATION-QUEUE`     │
└──────────────────────────────────────────────┘                └──────────────────────────────────────────┘
```

---

## 2. 🔄 Functional Business Flows

The application manages 5 primary business functional flows across the mortgage lifecycle:

```mermaid
flowchart TD
    subgraph Flow 1: Auth & User Security
        A1[Borrower / Officer Login] --> A2[Generate OAuth2 Bearer JWT Token]
    end
    subgraph Flow 2: Loan Origination & Onboarding
        B1[Submit Loan Application Profile] --> B2[Persist via JPA ORM LoanApplicationRepository]
    end
    subgraph Flow 3: PostgreSQL Native Query Transition
        C1[Trigger Underwriting Review] --> C2[Execute PostgreSQL Native UPDATE: SUBMITTED to UNDER_REVIEW]
    end
    subgraph Flow 4: Automated Underwriting & Risk Engine
        D1[Execute Java 17 Switch Pattern Rules Engine] --> D2{Decision?}
        D2 -->|APPROVED| D3[Low/Medium Risk Scoring & Approval]
        D2 -->|REFERRED| D4[Manual Underwriter Review Needed]
        D2 -->|DECLINED| D5[Rejection Notice & High Risk Score]
    end
    subgraph Flow 5: Rate Pricing & ActiveMQ JMS Messaging
        E1[Calculate Benchmark Rate, LTV Surcharge & 360-Mo EMI] --> E2[Publish Event Payload to ActiveMQ Queue]
    end

    A2 --> B1 --> C1 --> D1 --> E1
```

---

### 🔑 2.1 Functional Flow Details

1. **Authentication & Token Issuance (`/api/v1/auth/login`)**:
   - Accepts user credentials and returns signed OAuth2 Bearer JWT access tokens with user roles (`LOAN_OFFICER`, `UNDERWRITER`).
2. **Mortgage Origination (`POST /api/v1/loans`)**:
   - Accepts borrower profile data, loan amount, property value, income, debt, and credit score. Persists record via `LoanApplicationRepository.save()`.
3. **Native SQL Status Transition (`POST /api/v1/loans/{loanId}/submit-underwriting`)**:
   - Executes PostgreSQL Native SQL Query `@Query(value = "UPDATE loan_applications SET status = :status WHERE id = :loanId", nativeQuery = true)` to transition loan state to `UNDER_REVIEW`.
4. **Automated Underwriting & Risk Engine (`POST /api/v1/underwriting/assess`)**:
   - Evaluates DTI and LTV ratios using Java 17 Switch Expressions rule logic to assign decision (`APPROVED`, `REFERRED`, `DECLINED`) and risk level (`LOW`, `MEDIUM`, `HIGH`).
5. **Real-Time Rate Pricing & Amortization (`POST /api/v1/rates/calculate`)**:
   - Calculates benchmark rate, credit score tier adjustments (`PRIME`, `NEAR_PRIME`, `NON_PRIME`, `SUBPRIME`), LTV surcharges, monthly EMI, and 360-month amortization payment schedules.
6. **ActiveMQ JMS Messaging (`POST /api/v1/notifications/publish`)**:
   - Publishes JSON event payloads to ActiveMQ queue (`FREDDIE-LOAN-NOTIFICATION-QUEUE.local`) via Spring `JmsTemplate`.

---

## 3. 🔗 Complete Call Chains: UI Frontend to Database

### 3.1 Standard CRUD & Native Query Call Chain

```mermaid
sequenceDiagram
    autonumber
    actor User as Borrower / Loan Officer
    participant UI as Angular 15 Frontend (Port 4200)
    participant Controller as LoanOriginationController
    participant Service as LoanOriginationService
    participant Repo as LoanApplicationRepository
    participant DB as PostgreSQL Database

    User->>UI: Submit Mortgage Application
    UI->>Controller: POST /api/v1/loans (Include Bearer Token & Loan Request Payload)
    Controller->>Controller: Protect Mass-Assignment (@InitBinder) & Validate Payload (@Valid)
    Controller->>Service: createLoanApplication(request)
    Service->>Service: Calculate DTI and LTV Ratios
    Service->>Repo: save(LoanApplicationEntity)
    alt Standard ORM Path
        Repo->>DB: INSERT INTO loan_applications (...) VALUES (...)
    else PostgreSQL Native SQL Path (Status Update)
        Controller->>Service: submitForUnderwritingNative(loanId)
        Service->>Repo: updateStatusNative(loanId, "UNDER_REVIEW")
        Repo->>DB: UPDATE loan_applications SET status = 'UNDER_REVIEW' WHERE id = loanId
    end
    DB-->>Repo: Return Updated Row Count / Entity ID
    Repo-->>Service: Return LoanApplicationEntity
    Service-->>Controller: Return LoanResponse DTO
    Controller-->>UI: Return HTTP 201 Created / 200 OK
    UI-->>User: Display Origination Confirmation & Amortization
```

---

### 3.2 Automated Underwriting & JMS Notification Call Chain

```mermaid
sequenceDiagram
    autonumber
    actor Officer as Senior Underwriter / System
    participant UI as Angular 15 Frontend (Port 4200)
    participant Controller as UnderwritingController
    participant Engine as UnderwritingRuleProcessor
    participant Math as RateCalculatorProcessor
    participant JMS as NotificationJmsPublisher
    participant MQ as ActiveMQ Message Broker

    Officer->>UI: Trigger Underwriting & Rate Assessment
    UI->>Controller: POST /api/v1/underwriting/assess
    Controller->>Engine: evaluateDecision(creditScore, dtiRatio, ltvRatio)
    Engine->>Engine: Java 17 Switch Pattern Expressions (APPROVED / REFERRED / DECLINED)
    Engine-->>Controller: Return Decision & RiskLevel (LOW/MEDIUM/HIGH)
    Controller->>Math: generateAmortizationSchedule(principal, rate, termMonths)
    Math-->>Controller: Return 360-Month Payment Schedule List
    Controller->>JMS: publishNotification("UNDERWRITING_EVENT", destination, payloadJson)
    JMS->>MQ: jmsTemplate.convertAndSend("FREDDIE-LOAN-NOTIFICATION-QUEUE.local", payloadJson)
    MQ-->>JMS: Message Delivery ACK
    JMS-->>Controller: Return NotificationDTO
    Controller-->>UI: Return HTTP 200 OK (UnderwritingResponse)
    UI-->>Officer: Render Decision, Risk Score & Amortization Table
```

---

## 4. 📂 Complete List of 10 Java Files (2 Microservices)

The entire backend codebase across both microservices consists of **EXACTLY 10 Java Files**:

### 📦 Module 1: `loan-origination-service` (Port 8082)
| # | File Path | Design Pattern / Primary Responsibility |
|---|---|---|
| 1 | [LoanOriginationApplication.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/LoanOriginationApplication.java) | Spring Boot Application Entry & Security Configuration |
| 2 | [LoanApplicationEntity.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/model/LoanApplicationEntity.java) | JPA Entity (`loan_applications`), Enums, and Record DTOs |
| 3 | [LoanApplicationRepository.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/repository/LoanApplicationRepository.java) | **Dual Data Access**: Spring Data ORM + PostgreSQL `@Query(nativeQuery = true)` |
| 4 | [LoanOriginationService.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/service/LoanOriginationService.java) | Business Service Layer orchestrating onboarding & origination |
| 5 | [LoanOriginationController.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/loan-origination-service/src/main/java/com/freddieapp/origination/controller/LoanOriginationController.java) | REST Controller Layer with `@InitBinder` and Exception Handler |

### ⚙️ Module 2: `underwriting-service` (Port 8083)
| # | File Path | Design Pattern / Primary Responsibility |
|---|---|---|
| 6 | [UnderwritingServiceApplication.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/underwriting-service/src/main/java/com/freddieapp/underwriting/UnderwritingServiceApplication.java) | Spring Boot Entry, Security, and ActiveMQ JMS Configuration |
| 7 | [UnderwritingRuleProcessor.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/underwriting-service/src/main/java/com/freddieapp/underwriting/processor/UnderwritingRuleProcessor.java) | **Java 17 Switch Expressions Rule Engine** (`APPROVED`/`REFERRED`/`DECLINED`) |
| 8 | [RateCalculatorProcessor.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/underwriting-service/src/main/java/com/freddieapp/underwriting/processor/RateCalculatorProcessor.java) | **Strategy Math Engine** for pricing tiers, EMI, and 360-mo amortization |
| 9 | [NotificationJmsPublisher.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/underwriting-service/src/main/java/com/freddieapp/underwriting/messaging/NotificationJmsPublisher.java) | **JMS Event Publisher Pattern** wrapping Spring `JmsTemplate` |
| 10 | [UnderwritingController.java](file:///c:/ramu/Project_Assignment/RapidX/FreddeMac_Project_RapidX/Work/UCount_App/Ucount_Mini_Application/freddie-loan-platform/underwriting-service/src/main/java/com/freddieapp/underwriting/controller/UnderwritingController.java) | REST Controller for `/underwriting/assess`, `/rates/calculate`, `/notifications/publish` |

---

## 5. 🛠️ Build & Local Execution Guide

### 📋 Prerequisites
- **Java 17 LTS**: `java -version`
- **Apache Maven 3.8+**: `mvn -version`

### 📦 Build All Microservices
Run clean compile across both microservices from the project root:
```powershell
mvn clean compile
```

### 🚀 Running the Microservices
1. **Loan Origination Service**:
   ```powershell
   cd loan-origination-service
   mvn spring-boot:run
   ```
2. **Underwriting Service**:
   ```powershell
   cd underwriting-service
   mvn spring-boot:run
   ```

### 🌐 Swagger API Portals
- **Loan Origination API**: `http://localhost:8082/swagger-ui.html`
- **Underwriting API**: `http://localhost:8083/swagger-ui.html`
