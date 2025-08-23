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
 * Unit tests for the API Gateway stack.
 * 
 * These tests validate that the API Gateway stack creates the expected
 * resources with correct properties.
 */
public class APIGatewayStackTest {

    /**
     * Tests that the API Gateway stack creates the expected HTTP API resource.
     */
    @Test
    public void testAPIGatewayStackCreatesHttpApi() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When
        APIGatewayStack stack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify HTTP API is created with correct properties
        template.hasResourceProperties("AWS::ApiGatewayV2::Api", Map.of(
                "Name", "Currency Coinverter HTTP API Gateway",
                "ProtocolType", "HTTP"
        ));
    }

    /**
     * Tests that the API Gateway instance is accessible.
     */
    @Test
    public void testGetHttpApi() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();
        APIGatewayStack stack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // When
        var httpApi = stack.getHttpApi();

        // Then
        assertNotNull(httpApi);
    }

    /**
     * Tests that tags are applied to the API Gateway resource.
     */
    @Test
    public void testAPIGatewayHasTags() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When
        APIGatewayStack stack = new APIGatewayStack(app, "test-api-stack",
                StackProps.builder().build(), stackConfig);

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify tags are applied to the HTTP API
        template.hasResourceProperties("AWS::ApiGatewayV2::Api", Map.of(
                "Tags", Map.of(
                        "project", "currencycoinverter",
                        "environment", "dev"
                )
        ));
    }
}
