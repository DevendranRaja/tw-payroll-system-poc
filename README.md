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
# configure your database connection in application.properties / .env
./gradlew bootRun


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
