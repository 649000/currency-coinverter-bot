package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import com.nazri.util.TagUtil;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.Map;

public class LambdaStack extends Stack {
    private final Function restAPIFunction;
    private final StackConfig stackConfig;

    public LambdaStack(final Construct scope, final String id, final StackProps props, StackConfig stackConfig, Table table) {
        super(scope, id, props);
        this.stackConfig = stackConfig;

        if (this.stackConfig.getGraalvm()) {
            this.restAPIFunction = createNativeFunction();
        } else {
            this.restAPIFunction = createNonNativeFunction();
        }

        table.grantReadWriteData(this.restAPIFunction);
        TagUtil.addTags(this.restAPIFunction, stackConfig);
    }

    /**
     * Deploying native images compiled by GraalVM
     *
     * @return
     */
    private Function createNativeFunction() {
        return Function.Builder.create(this, "currencycoinverter-quarkus")
                .description("Currency Coinverter: REST API via AWS HTTP API Gateway")
                .code(Code.fromAsset("../quarkus/target/function.zip"))
                .timeout(Duration.seconds(15))
                .memorySize(512)
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

    private Function createNonNativeFunction() {
        return Function.Builder.create(this, "currencycoinverter-quarkus")
                .description("Currency Coinverter: REST API via AWS HTTP API Gateway")
                .code(Code.fromAsset("../quarkus/target/function.zip"))
                .timeout(Duration.seconds(15))
                .memorySize(512)
                .runtime(Runtime.JAVA_21)
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

    public Function getAPIFunction() {
        return restAPIFunction;
    }
}
