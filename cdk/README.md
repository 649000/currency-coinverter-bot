# Currency Coinverter CDK Infrastructure

This CDK project defines the AWS infrastructure for the Currency Coinverter Telegram bot application.

## Architecture

The application consists of three main stacks:

- **DynamoDB Stack**: User data storage with a single table for chat/user information
- **API Gateway Stack**: HTTP API Gateway for webhook endpoints
- **Lambda Stack**: Quarkus-based Lambda function handling Telegram bot logic

## Prerequisites

- AWS CLI configured with appropriate credentials
- Node.js and npm installed
- AWS CDK CLI installed (`npm install -g aws-cdk`)
- Java 21 and Maven
- Quarkus application built and packaged (see `../quarkus/` directory)

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Build the Quarkus application first:
   ```bash
   cd ../quarkus
   mvn clean package
   cd ../cdk
   ```

3. Bootstrap CDK (first time only):
   ```bash
   cdk bootstrap
   ```

## Deployment

Deploy all stacks:
```bash
cdk deploy --all
```

Deploy specific stack:
```bash
cdk deploy currencycoinverter-dynamodb-stack
cdk deploy currencycoinverter-api-stack
cdk deploy currencycoinverter-api-lambda-stack
```

## Configuration

Environment variables required:
- `CURRENCYCOINVERTER_TELEGRAM_BOT_USERNAME`
- `CURRENCYCOINVERTER_TELEGRAM_BOT_TOKEN`
- `CURRENCYCOINVERTER_TELEGRAM_BOT_URL`

## Useful Commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk destroy`     remove the deployed stacks
 * `cdk docs`        open CDK documentation

## Stack Dependencies

1. DynamoDB Stack (independent)
2. API Gateway Stack (independent)
3. Lambda Stack (depends on DynamoDB and API Gateway stacks)
