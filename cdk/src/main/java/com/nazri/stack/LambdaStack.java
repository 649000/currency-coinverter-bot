package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import com.nazri.util.TagUtil;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.HttpNoneAuthorizer;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

/**
 * Lambda stack for Currency Coinverter application.
 * 
 * Creates a Lambda function running the Quarkus application that handles
 * Telegram bot logic and currency conversion. Supports both native
 * (GraalVM) and JVM runtime configurations.
 */
public class LambdaStack extends Stack {
    private final Function restAPIFunction;

    /**
     * Constructs a new Lambda stack.
     * 
     * @param scope the parent construct
     * @param id the stack identifier
     * @param props stack properties
     * @param stackConfig environment-specific configuration
     * @param table DynamoDB table for data access
     * @param httpApi HTTP API Gateway for routing
     */
    public LambdaStack(final Construct scope, final String id, final StackProps props, StackConfig stackConfig, Table table, HttpApi httpApi) {
        super(scope, id, props);

        if (stackConfig.getGraalvm()) {
            this.restAPIFunction = createNativeFunction();
        } else {
            this.restAPIFunction = createNonNativeFunction();
        }

        addTelegramWebhookRoute(httpApi);
        table.grantReadWriteData(this.restAPIFunction);
        TagUtil.addTags(this.restAPIFunction, stackConfig);
    }

    /**
     * Creates a Lambda function using GraalVM native image.
     * 
     * Native images provide faster cold start times but require
     * compilation with GraalVM native-image tool.
     *
     * @return configured Lambda function for native runtime
     */
    private Function createNativeFunction() {
        return Function.Builder.create(this, "currencycoinverter-quarkus")
                .description("Currency Coinverter: REST API via AWS HTTP API Gateway")
                .code(Code.fromAsset("../quarkus/target/function.zip"))
                .timeout(Duration.seconds(15))
                .memorySize(512)
                // If building on M series mac
                .architecture(Architecture.ARM_64)
                // GraalVM specific
                .runtime(Runtime.PROVIDED_AL2023)
                .handler("not.used.for.native")
                .environment(Map.of(
                        "DISABLE_SIGNAL_HANDLER", "true",
                        "quarkus_lambda_handler", "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest",
                        "telegram_bot_username", Constant.TELEGRAM_BOT_USERNAME,
                        "telegram_bot_token", Constant.TELEGRAM_BOT_TOKEN,
                        "telegram_webhook_url", Constant.TELEGRAM_WEBHOOK_URL
                ))
                .build();
    }

    /**
     * Creates a Lambda function using standard JVM runtime.
     * 
     * Uses Java 21 runtime with optimized JVM settings for
     * Lambda cold start performance.
     *
     * @return configured Lambda function for JVM runtime
     */
    private Function createNonNativeFunction() {
        return Function.Builder.create(this, "currencycoinverter-quarkus")
                .description("Currency Coinverter: REST API via AWS HTTP API Gateway")
                .code(Code.fromAsset("../quarkus/target/function.zip"))
                .timeout(Duration.seconds(15))
                .memorySize(512)
                .runtime(Runtime.JAVA_21)
                // If building on M series mac
                .architecture(Architecture.ARM_64)
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
                .environment(Map.of(
                        "quarkus_lambda_handler", "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest",
                        "telegram_bot_username", Constant.TELEGRAM_BOT_USERNAME,
                        "telegram_bot_token", Constant.TELEGRAM_BOT_TOKEN,
                        "telegram_webhook_url", Constant.TELEGRAM_WEBHOOK_URL,
                        // For JVM only
                        "JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
                ))
                .build();
    }

    /**
     * Configures the Telegram webhook route in the API Gateway.
     * 
     * @param httpApi the HTTP API Gateway to configure
     */
    private void addTelegramWebhookRoute(HttpApi httpApi) {
        httpApi.addRoutes(AddRoutesOptions.builder()
                .authorizer(new HttpNoneAuthorizer())
                .path("/api/telegram/webhook")
                .methods(List.of(HttpMethod.POST))
                .integration(HttpLambdaIntegration.Builder
                        .create("currencycoinverter-telegram-webhook", restAPIFunction)
                        .build())
                .build());
    }

    public Function getAPIFunction() {
        return restAPIFunction;
    }
}
