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

/**
 * API Gateway stack for Currency Coinverter application.
 * 
 * Creates an HTTP API Gateway that serves as the entry point
 * for Telegram webhook requests and other API endpoints.
 */
public class APIGatewayStack extends Stack {
    private final StackConfig stackConfig;
    private final HttpApi httpApi;

    /**
     * Constructs a new API Gateway stack.
     * 
     * @param scope the parent construct
     * @param id the stack identifier
     * @param props stack properties
     * @param stackConfig environment-specific configuration
     */
    public APIGatewayStack(final Construct scope, final String id, final StackProps props, StackConfig stackConfig) {
        super(scope, id, props);
        this.stackConfig = stackConfig;
        this.httpApi = createHTTPAPIGateway();
        TagUtil.addTags(this.httpApi, stackConfig);
    }

    /**
     * Creates the HTTP API Gateway for handling webhook requests.
     * 
     * @return configured HTTP API Gateway
     */
    private HttpApi createHTTPAPIGateway() {
        return HttpApi.Builder.create(this, "currencycoinverter-api-gateway")
                .apiName("Currency Coinverter HTTP API Gateway")
                .description("Currency Coinverter: HTTP API Gateway for REST")
                .build();
    }

    public HttpApi getHttpApi() {
        return httpApi;
    }
}
