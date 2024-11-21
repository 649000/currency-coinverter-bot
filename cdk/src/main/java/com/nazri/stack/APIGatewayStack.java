package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.TagUtil;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.HttpNoneAuthorizer;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

import java.util.List;

public class APIGatewayStack extends Stack {
    private final StackConfig stackConfig;
    private final HttpApi httpApi;

    public APIGatewayStack(final Construct scope, final String id, final StackProps props, StackConfig stackConfig, Function function) {
        super(scope, id, props);
        this.stackConfig = stackConfig;
        this.httpApi = createHTTPAPIGateway();
        addTelegramWebhookRoute(function);
        TagUtil.addTags(this.httpApi, stackConfig);
    }

    private HttpApi createHTTPAPIGateway() {
        return HttpApi.Builder.create(this, "currencycoinverter-api-gateway")
                .apiName("Currency Coinverter HTTP API Gateway")
                .description("Currency Coinverter: HTTP API Gateway for REST")
                .build();
    }

    private void addTelegramWebhookRoute(Function function) {
        httpApi.addRoutes(AddRoutesOptions.builder()
                .authorizer(new HttpNoneAuthorizer())
                .path("/api/telegram/webhook")
                .methods(List.of(HttpMethod.POST))
                .integration(HttpLambdaIntegration.Builder
                        .create("currencycoinverter-telegram-webhook", function)
                        .build())
                .build());
    }
}
