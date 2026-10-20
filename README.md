# Telegram Currency Conversion Bot

[![CI](https://github.com/649000/currency-coinverter-bot/actions/workflows/ci.yml/badge.svg)](https://github.com/649000/currency-coinverter-bot/actions/workflows/ci.yml)
[![Security Scan](https://github.com/649000/currency-coinverter-bot/actions/workflows/codeql.yml/badge.svg)](https://github.com/649000/currency-coinverter-bot/actions/workflows/codeql.yml)
![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-3.16-red?logo=quarkus&logoColor=white)
![AWS CDK](https://img.shields.io/badge/AWS%20CDK-v2-FF9900?logo=amazon-aws&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green.svg)

This repository contains a Telegram Currency Conversion Bot built with a focus on scalability, efficiency, and modern serverless technologies. It demonstrates my expertise in backend development, infrastructure as code (IaC), and handling NoSQL databases, while highlighting key learning experiences with frameworks and tools like Quarkus, AWS CDK, and DynamoDB.

## Features

- **Live Currency Conversion**: Fetches real-time exchange rates via Fawaz Ahmed's Currency API with support for 150+ fiat currencies and countries.
- **Smart Country & Geolocation Resolution**: Accepts ISO currency codes (e.g., `USD`, `SGD`), country names (e.g., `Malaysia`, `Japan`), or Telegram GPS location pins (automatically converted to country codes via `coordinates2country`).
- **Interactive Inline UI**: Rich Telegram inline keyboards featuring quick amount selection (`10`, `50`, `100`, `500`) and instant multiplier shortcuts (`1x`, `10x`, `100x`).
- **Resilient & Cached**: Built with Quarkus Caffeine Cache (15-minute TTL) and SmallRye Fault Tolerance (Circuit Breaker, Retries, Timeouts) to prevent cascading failures.
- **Efficient Communication**: Uses Telegram webhooks instead of polling for enhanced efficiency, lower latency, and serverless compatibility.
- **Scalable Serverless Architecture**: Built with AWS Lambda and HTTP API Gateway, ensuring high availability and zero idle costs.
- **Infrastructure as Code**: The full cloud infrastructure is declared in Java using AWS Cloud Development Kit (CDK).
- **Sub-Second Cold Starts**: Compiled with GraalVM to an ARM64 Linux native executable (`provided.al2023`), drastically reducing Lambda cold start overhead.

## How to Use

### Quick Start
1. Start a conversation with [@CurrencyCoinvertBot](https://t.me/CurrencyCoinvertBot)
2. Send `/start` to begin
3. Set your input currency: `/from USD` (or share your GPS location pin)
4. Set your output currency: `/to SGD` (add up to 3 target currencies)
5. Send any number (e.g., `100`) to get instant conversion with interactive multipliers

### Available Commands
- `/start` - Welcome message and setup instructions
- `/from <currency|country>` - Set input currency (e.g., `/from USD`, `/from Malaysia`, or send location)
- `/to <currency|country>` - Set output currency (e.g., `/to SGD`, `/to Singapore`)
- `/convert <amount>` - Convert a specific amount (or simply type a number like `100`)
- `/getcurrencies` - Display your currently saved base and target currencies
- `/deletecurrency` - Remove saved target currencies via interactive buttons
- `/help` - Show all available commands

## Technology Stack

| Component                 | Technology                                                    |
|---------------------------|---------------------------------------------------------------|
| **Programming Language**  | Java 21                                                       |
| **Framework**             | Quarkus 3.16                                                  |
| **Hosting**               | AWS Lambda (ARM64, `provided.al2023` / `java21`)              |
| **API Management**        | AWS HTTP API Gateway (v2)                                     |
| **Database**              | AWS DynamoDB (AWS SDK v2 Enhanced Client)                     |
| **Exchange Rate Provider**| Fawaz Ahmed Currency API (`latest.currency-api.pages.dev`)   |
| **Caching & Resilience**  | Caffeine Cache, MicroProfile Fault Tolerance (Circuit Breaker)|
| **IaC Tool**              | AWS Cloud Development Kit (CDK v2) in Java                    |
| **Compiler**              | GraalVM Native Image                                          |

## Project Structure

```
├── cdk/        # AWS CDK infrastructure code
│   ├── src/main/java/com/nazri/
│   │   ├── config/StackConfig.java
│   │   ├── stack/ (APIGatewayStack, DynamoDBStack, LambdaStack)
│   │   └── CdkApp.java
│   └── cdk.json
├── quarkus/    # Quarkus application
│   ├── src/main/java/com/nazri/
│   │   ├── client/       # MicroProfile REST Client
│   │   ├── command/      # Command pattern implementations & registry
│   │   ├── controller/   # Webhook endpoint (/api/telegram/webhook)
│   │   ├── model/        # DynamoDB User entity & Telegram responses
│   │   ├── repository/   # DynamoDB repository
│   │   ├── service/      # Currency, Telegram bot, User, and Message services
│   │   └── util/         # Helper functions & inline keyboard builder
│   └── pom.xml
├── pom.xml     # Root reactor Maven project
└── README.md
```

## Architecture

The project is designed as an event-driven serverless architecture for cost efficiency and near-instant scalability:

1. **Telegram Webhook**: Telegram pushes updates to an **AWS HTTP API Gateway** endpoint.
2. **AWS Lambda (Quarkus Native)**: Routes incoming updates to `TelegramBot` and executes business logic in sub-second time.
3. **Currency API & Cache**: Fetches live exchange rates with a 15-minute Caffeine cache and fallback circuit breaking.
4. **AWS DynamoDB**: Stores user preferences (base currency, target currencies, timestamps) using single-table design.
5. **AWS CDK**: Orchestrates reproducible, type-safe infrastructure as code.

### Architecture Diagram

![image](https://github.com/user-attachments/assets/2480c16b-a756-4a4c-9453-2686d7e036f4)

## Getting Started

### Prerequisites

- Java 21+
- Node.js & npm (for AWS CDK)
- AWS CDK CLI (`npm install -g aws-cdk`)
- AWS CLI configured with active credentials
- Docker (for GraalVM containerized native builds)

### Running Tests

```bash
# Run tests across all modules (CDK + Quarkus)
mvn test
```

## CI/CD & Security Pipeline

This project implements an automated CI/CD and security pipeline using **GitHub Actions** optimized for serverless deployments and quota efficiency:

- **Continuous Integration (`ci.yml`)**: Runs on pushes to `main`, PRs, and feature branches. Executes unit tests (`mvn test`), packages JVM function artifacts, and verifies CloudFormation template synthesis (`cdk synth`) in **~1.5 minutes** without building heavy native images.
- **Security & SAST Scanning (`codeql.yml` & `dependabot.yml`)**: CodeQL semantic analysis scans Java code for injection flaws, tainted data flows, and CWE vulnerabilities. Dependabot monitors Maven and GitHub Actions dependencies for CVEs with zero CI minute consumption.
- **Manual / Safe Deployments (`deploy-dev.yml` & `deploy-prd.yml`)**: Deployments to DEV (JVM) and PRD (GraalVM ARM64 native image) are on-demand and triggerable via Git release tags (`v*.*.*`) or manual UI dispatch (`workflow_dispatch`).
- **Passwordless Security**: Uses **AWS IAM OpenID Connect (OIDC)** for temporary STS credential assumption instead of long-lived access keys.

📖 For complete documentation on pipeline architecture, quota calculations, AWS OIDC setup, and deployment guides, see the [CI/CD Architecture & GitHub Actions Guide](docs/ci-cd-guide.md).

## Key Technical Decisions & Learnings

### Why Quarkus over Spring Boot?
- **Cold Start Performance**: 10x faster Lambda cold starts with native compilation (~150ms vs ~2000ms on JVM)
- **Memory Efficiency**: ~50% lower memory footprint, lowering AWS Lambda execution costs
- **Developer Experience**: Fast build times, dev mode, and seamless AWS Lambda HTTP integration

### DynamoDB Design Patterns
- **Single Table Design**: User entities stored with `chatId` partition key
- **Enhanced Client**: Type-safe annotations (`@DynamoDbBean`, `@DynamoDbPartitionKey`)
- **Access Pattern First**: Schema tailored to frequent lookup and update patterns

### Infrastructure Choices
- **AWS CDK over Terraform**: Type-safe infrastructure in Java with IDE autocompletion and compile-time validation
- **HTTP API vs REST API**: Significantly lower latency and cost for simple routing needs

## Future Enhancements
1. Implement rate-limiting per chat ID to prevent bot spam.
2. Historical exchange rate charts and trends.
3. Expand bot support to other platforms like Slack or Discord.

## Conclusion
Building this currency converter bot taught me a lot about serverless architecture and cloud development. The switch from Spring to Quarkus, and learning DynamoDB coming from a SQL background, were challenging but rewarding. While there's still room for improvement, I'm happy with how the project turned out as it works reliably, starts up quickly, and serves its purpose well.

Feel free to check out the code or try the bot yourself. I'm always open to feedback and suggestions!
