package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.assertions.Template;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the Lambda stack.
 * 
 * These tests validate that the Lambda stack creates the expected
 * function resources with correct properties for both JVM and native modes.
 */
public class LambdaStackTest {

    /**
     * Tests that the Lambda stack creates a JVM-based function with correct properties.
     */
    @Test
    public void testLambdaStackCreatesJvmFunction() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV) // DEV uses JVM mode
                .build();

        // Create dependencies
        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);
        
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // When
        LambdaStack stack = new LambdaStack(app, "test-lambda-stack",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify Lambda function is created with correct properties
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Runtime", "java21",
                "Handler", "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest",
                "MemorySize", 512,
                "Architectures", java.util.List.of("arm64")
        ));
    }

    /**
     * Tests that the Lambda stack creates a native function with correct properties.
     */
    @Test
    public void testLambdaStackCreatesNativeFunction() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.PRD) // PRD uses native mode
                .build();

        // Create dependencies
        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);
        
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // When
        LambdaStack stack = new LambdaStack(app, "test-lambda-stack",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify Lambda function is created with correct properties
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Runtime", "provided.al2023",
                "Handler", "not.used.for.native",
                "MemorySize", 512,
                "Architectures", java.util.List.of("arm64")
        ));
    }

    /**
     * Tests that the Lambda function instance is accessible.
     */
    @Test
    public void testGetAPIFunction() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // Create dependencies
        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);
        
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        LambdaStack stack = new LambdaStack(app, "test-lambda-stack",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        // When
        var apiFunction = stack.getAPIFunction();

        // Then
        assertNotNull(apiFunction);
    }

    /**
     * Tests that the Lambda function has the correct environment variables.
     */
    @Test
    public void testLambdaFunctionHasEnvironmentVariables() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // Create dependencies
        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);
        
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // When
        LambdaStack stack = new LambdaStack(app, "test-lambda-stack",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify function has environment variables
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Environment", Map.of(
                        "Variables", Map.of(
                                "telegram_bot_username", Constant.TELEGRAM_BOT_USERNAME,
                                "telegram_bot_token", Constant.TELEGRAM_BOT_TOKEN,
                                "telegram_webhook_url", Constant.TELEGRAM_WEBHOOK_URL
                        )
                )
        ));
    }

    /**
     * Tests that the API Gateway route is created for the Telegram webhook.
     */
    @Test
    public void testTelegramWebhookRouteIsCreated() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // Create dependencies
        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);
        
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // When
        LambdaStack stack = new LambdaStack(app, "test-lambda-stack",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify HTTP route is created for Telegram webhook
        template.hasResourceProperties("AWS::ApiGatewayV2::Route", Map.of(
                "RouteKey", "POST /api/telegram/webhook"
        ));
    }
}
