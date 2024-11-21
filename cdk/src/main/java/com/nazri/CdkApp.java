package com.nazri;

import com.nazri.config.StackConfig;
import com.nazri.stack.APIGatewayStack;
import com.nazri.stack.DynamoDBStack;
import com.nazri.stack.LambdaStack;
import com.nazri.util.Constant;
import software.amazon.awscdk.App;

public class CdkApp {
    public static void main(final String[] args) {
        App app = new App();

        // cdk synth --context env=dev OR --context env=sit OR --context env=uat
        String environment = (String) app.getNode().tryGetContext("env");
        StackConfig stackConfig = getStackConfig(environment);

        DynamoDBStack dynamoDBStack = new DynamoDBStack(app, "currencycoinverter-dynamodb-stack", stackConfig.getStackProps()
                .stackName("currencycoinverter-dynamodb-stack")
                .description("DynamoDB Stack for Currency Coinverter")
                .build(),
                stackConfig);

        LambdaStack apiLambdaStack = new LambdaStack(app, "currencycoinverter-api-lambda-stack", stackConfig.getStackProps()
                .stackName("currencycoinverter-api-lambda-stack")
                .description("Quarkus API Stack for Currency Coinverter")
                .build(),
                stackConfig,
                dynamoDBStack.getUserTable()
        );

        new APIGatewayStack(app, "currencycoinverter-api-stack", stackConfig.getStackProps()
                .stackName("currencycoinverter-api-stack")
                .description("HTTP API Gateway Stack for Currency Coinverter")
                .build(),
                stackConfig,
                apiLambdaStack.getAPIFunction());
//        new CdkStack(app, "CdkStack", StackProps.builder()
        // If you don't specify 'env', this stack will be environment-agnostic.
        // Account/Region-dependent features and context lookups will not work,
        // but a single synthesized template can be deployed anywhere.

        // Uncomment the next block to specialize this stack for the AWS Account
        // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

        // Uncomment the next block if you know exactly what Account and Region you
        // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */

        // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html
//                .build());

        app.synth();
    }

    public static StackConfig getStackConfig(String environment) {
        StackConfig stackConfig;
        switch (environment) {
            case Constant.SIT:
                stackConfig = new StackConfig.Builder()
                        .withEnvironment(Constant.SIT)
                        .build();
                break;
            case Constant.UAT:
                stackConfig = new StackConfig.Builder()
                        .withEnvironment(Constant.UAT)
                        .build();
                break;
            default:
                stackConfig = new StackConfig.Builder()
                        .withEnvironment(Constant.DEV)
                        .build();
        }
        return stackConfig;
    }
}

