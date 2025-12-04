# One Payroll

A modern payroll management application built with a full‑stack architecture.

🚀 Features

Employee management (create, update, delete employees)

Payroll processing (compute salary, deductions, net pay)

Front‑end client interface for staff or HR access

Back‑end server API for business logic and data persistence

Built with scalable technologies and modular structure

🧱 Technology Stack
Client

JavaScript / TypeScript

HTML / CSS / SCSS

Modern front‑end framework (React, Vue, or similar)

package.json & package‑lock.json present

Server

Java with Spring Boot (or similar)

Gradle build system (.gradle folder present)

REST API endpoints for payroll workflows

Other

Husky hooks for git pre‑commit / code quality (.husky folder present)

Project directory structure: client/ and server/

📦 Installation

Prerequisites: Node.js, Java (11+), Gradle, a database (MySQL/PostgreSQL), and Git.

Clone the repo:

git clone https://github.com/DevendranRaja/one‑payroll.git
cd one‑payroll


Setup the server:

cd server
# configure your database connection in application.properties 

⚙️ Configuration – application.yaml

Your Spring Boot service requires the following DB settings:

spring:
datasource:
url: jdbc:postgresql://postgres:5432/payroll_db
username: payroll_user
password: payroll_pass
driver-class-name: org.postgresql.Driver

jpa:
hibernate:
ddl-auto: update
properties:
hibernate:
format_sql: true
show-sql: true

server:
port: 8080

🐳 Running the Application Using Docker Compose

1️⃣ Build JAR

./gradlew clean build

./gradlew bootRun

2️⃣ Start containers

docker-compose down -v
docker-compose up -d

This starts:

Spring Boot app → http://localhost:8080

PostgreSQL DB → port 5432



Setup the client:

cd ../client
npm install
npm start


Open your browser at http://localhost:3000 (or configured port) and login / use the application.

🛠 Usage

Add employees: name, designation, salary, joining date, etc.

Process payroll for a given month: calculate gross salary, deductions (tax, provident fund), net pay.

Generate payroll reports: PDF/CSV export capability (planned).

Manage user roles (HR, Admin, Employee) (future feature).

🧪 Running Tests

Server:

./gradlew test


Client:

npm test

📁 Project Structure
one‑payroll/
│
├── client/               # front‑end code
│   ├── src/
│   ├── package.json
│   └── …
├── server/               # back‑end code
│   ├── src/
│   ├── build.gradle
│   └── …
├── .husky/               # git hooks
└── README.md             # this doc

🧭 Roadmap & Future Enhancements

User authentication & role‑based access control

Multi‑branch support (for businesses with multiple locations)

Integration with banking/payment APIs for automatic disbursement

Mobile responsive UI or separate mobile app

Automated payroll scheduling & notifications

Audit logs and compliance reporting

🤝 Contributing

Contributions are welcome! Please follow these steps:

Fork the repository.

Create your feature branch (git checkout -b feature/YourFeature).

Commit your changes (git commit -m 'Add feature …').

Push to the branch (git push origin feature/YourFeature).

Submit a pull request describing your changes.




flowchart LR

    subgraph UI["Frontend / External Clients"]
        A1[Admin UI]
        A2[Employee UI]
        A3[Scheduler Trigger]
    end

    subgraph API["REST API Layer"]
        C1[EmployeeMasterController]
        C2[PayGroupController]
        C3[PayrollCalculationController]
        C4[PayslipController]
        C5[IntegrationController]
    end

    subgraph Domain["Domain Services"]
        D1[Employee Master Service]
        D2[Pay Group Service]
        D3[Payroll Calculation Engine]
        D4[Payslip Metadata Builder]
        D5[Timesheet Service]
        D6[Mock SAP/Bank Integration Service]
        D7[Validation Manager]
    end

    subgraph DB["PostgreSQL Database"]
        T1[(employee_master)]
        T2[(pay_group)]
        T3[(pay_period)]
        T4[(timesheet_summary)]
        T5[(payroll_run)]
        T6[(payslip)]
        T7[(payroll_batch)]
        T8[(payroll_batch_log)]
        T9[(error_log)]
    end

    subgraph Infra["Infrastructure & External"]
        S1[Scheduler (Spring Cron)]
        S2[Mock SAP/Bank API]
        S3[CI/CD - GitHub Actions]
        S4[Sonar + Jacoco + Checkstyle]
    end

    UI --> API
    API --> Domain
    Domain --> DB
    Domain --> Infra
    Infra --> DB


