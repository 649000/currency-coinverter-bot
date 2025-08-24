# Currency Coinverter - Quarkus Backend

This is the Quarkus backend service for the Currency Coinverter Telegram bot.

## Key Features

- **Telegram Webhook Processing**: Handles incoming messages and commands
- **Currency Conversion**: Real-time conversion with external API integration
- **User Preference Management**: Stores user settings in DynamoDB
- **Multi-language Support**: Internationalized bot responses

## Architecture Overview

```
Telegram → API Gateway → Lambda (Quarkus) → DynamoDB
                                    ↓
                            Exchange Rate API
```

## Development Setup

### Prerequisites
- Java 21+
- Maven 3.9+ (system installed)
- Docker (for native compilation)

### Environment Variables
Create a `.env` file with:
```bash
CURRENCYCOINVERTER_TELEGRAM_BOT_TOKEN=your_bot_token
CURRENCYCOINVERTER_TELEGRAM_BOT_USERNAME=your_bot_username
CURRENCYCOINVERTER_TELEGRAM_BOT_URL=https://your-domain/webhook
AWS_REGION=ap-southeast-1
```

### Running Locally
```bash
# Development mode with live reload
mvn compile quarkus:dev

# Run tests
mvn test

# Package for deployment
mvn package
```

## Deployment

This service is designed to run as an AWS Lambda function. See the CDK project for infrastructure setup.

### Native Compilation
```bash
# Build native executable
mvn package -Dnative

# Build in container (no local GraalVM needed)
mvn package -Dnative -Dquarkus.native.container-build=true
```

## Technical Highlights

### Performance Optimizations
- **GraalVM Native Image**: Sub-second cold starts (~150ms vs 2000ms with JVM)
- **Connection Pooling**: Efficient HTTP client usage
- **Response Caching**: Exchange rate caching to reduce external calls

### Resilience Patterns
- **Circuit Breaker**: Prevents cascade failures from external APIs
- **Retry Logic**: Automatic retries with exponential backoff
- **Timeout Handling**: Graceful degradation on slow responses

### Data Management
- **DynamoDB Enhanced Client**: Type-safe database operations
- **Efficient Queries**: Optimized access patterns for user data
- **Data Validation**: Built-in validation for user inputs

## API Endpoints

- `POST /webhook` - Telegram webhook endpoint

## Testing

```bash
# Run unit tests
mvn test
```

## Monitoring & Observability

- **Structured Logging**: JSON formatted logs for easy parsing

## Related Documentation

- [Root Project README](../README.md) - Complete project overview
- [Quarkus Guides](https://quarkus.io/guides/) - Framework documentation
