# Telegram Currency Conversion Bot

This repository contains a Telegram Currency Conversion Bot built with a focus on scalability, efficiency, and modern serverless technologies. It demonstrates my expertise in backend development, infrastructure as code (IaC), and handling NoSQL databases, while highlighting key learning experiences with frameworks and tools like Quarkus, AWS CDK, and DynamoDB.

## Features

- **Real-time Currency Conversion**: Users can define their input and output currencies via chat commands. Upon entering a numerical value, the bot performs the conversion based on pre-defined rates.
- **Efficient Communication**: Uses Telegram webhooks instead of polling for enhanced efficiency and reduced latency.
- **Scalable Serverless Architecture**: Built with AWS Lambda and API Gateway, ensuring high availability and low operational overhead.
- **Infrastructure as Code**: The project infrastructure is managed using AWS Cloud Development Kit (CDK) for repeatability and ease of updates.
- **Blazing Startup Time**: Compiled using GraalVM creating native images that dramatically reduce AWS Lambda cold start time

## How to Use

### Quick Start
1. Start a conversation with [@CurrencyCoinvertBot](https://t.me/CurrencyCoinvertBot)
2. Send `/start` to begin
3. Set your input currency: `/from USD`
4. Set your output currency: `/to SGD`
5. Send any number (e.g., `100`) to get instant conversion

### Available Commands
- `/start` - Welcome message and setup instructions
- `/from <currency>` - Set input currency (e.g., `/from USD`, `/from Malaysia`)
- `/to <currency>` - Set output currency (e.g., `/to SGD`, `/to Singapore`)
- `/help` - Show all available commands
- `/deletecurrency` - Remove saved output currencies

## Technology Stack

| Component                 | Technology                              |
|---------------------------|-----------------------------------------|
| **Programming Language**  | Java                                   |
| **Framework**             | Quarkus                                |
| **Hosting**               | AWS Lambda                             |
| **API Management**        | AWS HTTP API Gateway                   |
| **Database**              | AWS DynamoDB                           |
| **IaC Tool**              | AWS Cloud Development Kit (CDK)        |
| **Compiler**              | GraalVM for native image compilation   |

## Project Structure

```
├── cdk/                    # AWS CDK infrastructure code
│   ├── src/main/java/
│   └── cdk.json
├── quarkus/               # Quarkus application
│   ├── src/main/java/
│   └── pom.xml
└── README.md
```

## Architecture

The project is designed as a serverless system to ensure cost efficiency and scalability. Below is an overview of the architecture:

1. **AWS API Gateway**: Routes incoming requests from Telegram to the Quarkus app hosted on AWS Lambda.
2. **AWS Lambda**: Hosts the Quarkus app, handling incoming updates from the API Gateway and executes business logic
4. **AWS DynamoDB**: Serves as the NoSQL database for storing user preferences, such as input and output currencies.
5. **AWS CDK**: Manages the infrastructure as code, ensuring consistent deployments.

### Architecture Diagram

![image](https://github.com/user-attachments/assets/2480c16b-a756-4a4c-9453-2686d7e036f4)

## Getting Started

### Prerequisites

- Node.js and npm installed (for AWS CDK)
- AWS CDK installed (`npm install -g aws-cdk`)
- AWS CLI configured
- GraalVM installed for native image compilation
- Java 17+
- Docker


## Key Technical Decisions & Learnings

### Why Quarkus over Spring Boot?
- **Cold Start Performance**: 10x faster Lambda cold starts with native compilation
- **Memory Efficiency**: 50% lower memory footprint
- **Developer Experience**: Live reload and excellent AWS Lambda integration

### DynamoDB Design Patterns
- **Single Table Design**: All entities in one table with composite keys
- **Access Pattern First**: Designed schema around query patterns, not entities
- **GSI Strategy**: Used Global Secondary Indexes for alternate access patterns

### Infrastructure Choices
- **AWS CDK over Terraform**: Type-safe infrastructure with IDE support
- **HTTP API vs REST API**: Lower latency and cost for simple routing needs

### Transitioning from Relational Databases to DynamoDB
As someone with a relational database background, I initially found it challenging to adapt to DynamoDB's NoSQL design principles. Understanding the importance of access patterns and designing the schema to support these patterns was a significant learning curve. This experience underscored the necessity of thinking ahead about the app's requirements, which contrasts with the flexibility typically associated with agile development.

### Adopting Quarkus
Quarkus proved to be an excellent choice for building a lightweight, serverless application. Its native image compilation using GraalVM significantly reduced Lambda's cold start times, resulting in faster response times and better scalability.

### Implementing IaC with AWS CDK
Using AWS CDK simplified infrastructure management and made deployments seamless. It reinforced my belief in the power of IaC tools for creating and maintaining robust cloud environments.

## Future Enhancements
1. Integrate live exchange rates using a currency API (e.g., OpenExchangeRates or XE).
2. Implement caching layer for frequently used conversions.
3. Implement rate-limiting to prevent abuse of the bot.
4.  Expand to support other chat platforms like Slack or Discord.

## Conclusion
Building this currency converter bot taught me a lot about serverless architecture and cloud development. The switch from Spring to Quarkus, and learning DynamoDB coming from a SQL background, were challenging but rewarding. While there's still room for improvement, I'm happy with how the project turned out - it works reliably, starts up quickly, and serves its purpose well.

Feel free to check out the code or try the bot yourself. I'm always open to feedback and suggestions!
