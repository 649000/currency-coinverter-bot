package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.assertions.Match;
import software.amazon.awscdk.assertions.Template;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for LambdaStack.
 *
 * Validates that the Lambda stack creates the expected resources,
 * architecture, runtime options, IAM permissions, and API Gateway route.
 */
public class LambdaStackTest {

    @Test
    public void testLambdaStackCreatesNativeFunction_WhenGraalvmTrue() {
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.PRD)
                .build();

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamo-stack-1",
                StackProps.builder().build(), stackConfig);
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack-1",
                StackProps.builder().build(), stackConfig);

        LambdaStack lambdaStack = new LambdaStack(app, "test-lambda-stack-1",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        Template template = Template.fromStack(lambdaStack);

        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Runtime", "provided.al2023",
                "Timeout", 15,
                "MemorySize", 512,
                "Architectures", List.of("arm64"),
                "Environment", Map.of(
                        "Variables", Match.objectLike(Map.of(
                                "DISABLE_SIGNAL_HANDLER", "true",
                                "quarkus_lambda_handler", "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"
                        ))
                )
        ));
    }

    @Test
    public void testLambdaStackCreatesJvmFunction_WhenGraalvmFalse() {
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamo-stack-2",
                StackProps.builder().build(), stackConfig);
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack-2",
                StackProps.builder().build(), stackConfig);

        LambdaStack lambdaStack = new LambdaStack(app, "test-lambda-stack-2",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        Template template = Template.fromStack(lambdaStack);

        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Runtime", "java21",
                "Timeout", 15,
                "MemorySize", 512,
                "Architectures", List.of("arm64"),
                "Environment", Map.of(
                        "Variables", Match.objectLike(Map.of(
                                "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
                        ))
                )
        ));
    }

    @Test
    public void testLambdaStackConfiguresApiGatewayWebhookRoute() {
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.PRD)
                .build();

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamo-stack-3",
                StackProps.builder().build(), stackConfig);
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack-3",
                StackProps.builder().build(), stackConfig);

        LambdaStack lambdaStack = new LambdaStack(app, "test-lambda-stack-3",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        Template template = Template.fromStack(apiGatewayStack);

        template.hasResourceProperties("AWS::ApiGatewayV2::Route", Map.of(
                "RouteKey", "POST /api/telegram/webhook",
                "AuthorizationType", "NONE"
        ));
    }

    @Test
    public void testLambdaStackGrantsDynamoDbReadWrite() {
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.PRD)
                .build();

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamo-stack-4",
                StackProps.builder().build(), stackConfig);
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack-4",
                StackProps.builder().build(), stackConfig);

        LambdaStack lambdaStack = new LambdaStack(app, "test-lambda-stack-4",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        Template template = Template.fromStack(lambdaStack);

        template.hasResourceProperties("AWS::IAM::Policy", Map.of(
                "PolicyDocument", Match.objectLike(Map.of(
                        "Statement", Match.arrayWith(List.of(
                                Match.objectLike(Map.of(
                                        "Action", Match.arrayWith(List.of(
                                                "dynamodb:BatchGetItem",
                                                "dynamodb:GetItem",
                                                "dynamodb:PutItem",
                                                "dynamodb:UpdateItem"
                                        )),
                                        "Effect", "Allow"
                                ))
                        ))
                ))
        ));
    }

    @Test
    public void testGetAPIFunction_ShouldReturnNonNullFunction() {
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "test-dynamo-stack-5",
                StackProps.builder().build(), stackConfig);
        APIGatewayStack apiGatewayStack = new APIGatewayStack(app, "test-api-stack-5",
                StackProps.builder().build(), stackConfig);

        LambdaStack lambdaStack = new LambdaStack(app, "test-lambda-stack-5",
                StackProps.builder().build(), stackConfig,
                dynamoDBStack.getUserTable(), apiGatewayStack.getHttpApi());

        assertNotNull(lambdaStack.getAPIFunction());
    }
}
