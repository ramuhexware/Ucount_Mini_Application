# Database Architecture — Freddie Mac-Style Home Loan Platform

## Overview

The platform uses **exactly 2 PostgreSQL 16 databases**. All services connect to one of these two
databases, using dedicated schemas to keep data logically separated while eliminating the operational
burden of managing multiple heterogeneous database technologies.

| Database | Purpose | Services |
|---|---|---|
| `freddie_customer` | Customer-facing & document data | customer-service, card-service, notification-service, document-service |
| `freddie_loans` | Loan lifecycle & decisioning data | loan-origination-service, underwriting-service, funding-service, appraisal-service, report-service |

---

## Database: `freddie_customer`

### Schemas

| Schema | Owner Service | Tables |
|---|---|---|
| `freddie_customer` | customer-service | `customers`, `customer_addresses`, `customer_documents` |
| `freddie_cards` | card-service | `cards`, `card_transactions` |
| `freddie_customer` | document-service | `loan_documents` (with `file_data bytea` for binary storage) |

---

## Database: `freddie_loans`

### Schemas

| Schema | Owner Service | Tables |
|---|---|---|
| `freddie_loans` | loan-origination-service | `loan_applications`, `loan_status_history` |
| `freddie_uw` | underwriting-service | `underwriting_assessments` |
| `freddie_funding` | funding-service | `funding_disbursements` |
| `freddie_appraisal` | appraisal-service | `appraisals` |
| `freddie_reports` | report-service | `reports` |

---

## Connection Details

| Property | Value |
|---|---|
| Host (local) | `localhost` |
| Host (Docker) | `postgresql` (service name) |
| Port | `5432` |
| Username | `freddie_admin` |
| Password | `freddie_secret` |
| SSL | Disabled (dev); enable for production |

---

## Script Execution Order

Scripts under `database/postgresql/` are executed in order:

```
init_databases.sql          # Creates both databases + grants
ddl/
  01_customer_service_schema.sql
  02_card_service_schema.sql
  03_document_service_schema.sql
  04_loan_origination_schema.sql
  05_underwriting_service_schema.sql
  06_funding_service_schema.sql
dml/
  03_document_service_seed.sql
  04_loan_origination_seed.sql
  05_underwriting_service_seed.sql
```

> **Note:** All DDL scripts are idempotent (`CREATE TABLE IF NOT EXISTS`, `CREATE SCHEMA IF NOT EXISTS`).
> Set `spring.jpa.hibernate.ddl-auto=none` on all services — schema is managed externally via these scripts.

---

## Migration History

| Previous DB | Migrated To | Service |
|---|---|---|
| Oracle 21c | `freddie_loans` (PostgreSQL) | loan-origination-service |
| IBM DB2 | `freddie_loans` (PostgreSQL) | underwriting-service |
| MongoDB GridFS | `freddie_customer` (PostgreSQL bytea) | document-service |
| H2 (dev) | `freddie_loans` (PostgreSQL) | funding, appraisal, report |
