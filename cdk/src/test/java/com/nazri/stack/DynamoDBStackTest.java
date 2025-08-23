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
 * Unit tests for the DynamoDB stack.
 * 
 * These tests validate that the DynamoDB stack creates the expected
 * table resources with correct properties.
 */
public class DynamoDBStackTest {

    /**
     * Tests that the DynamoDB stack creates the user table with correct properties.
     */
    @Test
    public void testDynamoDBStackCreatesUserTable() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When
        DynamoDBStack stack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify user table is created with correct properties
        template.hasResourceProperties("AWS::DynamoDB::Table", Map.of(
                "TableName", "currencycoinverter-user",
                "BillingMode", "PROVISIONED",
                "ProvisionedThroughput", Map.of(
                        "ReadCapacityUnits", 1,
                        "WriteCapacityUnits", 1
                )
        ));
    }

    /**
     * Tests that the user table instance is accessible.
     */
    @Test
    public void testGetUserTable() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();
        DynamoDBStack stack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);

        // When
        var userTable = stack.getUserTable();

        // Then
        assertNotNull(userTable);
    }

    /**
     * Tests that tags are applied to the DynamoDB table.
     */
    @Test
    public void testDynamoDBTableHasTags() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When
        DynamoDBStack stack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify tags are applied to the DynamoDB table
        template.hasResourceProperties("AWS::DynamoDB::Table", Map.of(
                "Tags", Map.of(
                        "project", "currencycoinverter",
                        "environment", "dev"
                )
        ));
    }

    /**
     * Tests that the DynamoDB table has correct partition key configuration.
     */
    @Test
    public void testDynamoDBTableHasCorrectPartitionKey() {
        // Given
        App app = new App();
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When
        DynamoDBStack stack = new DynamoDBStack(app, "test-dynamodb-stack",
                StackProps.builder().build(), stackConfig);

        // Then
        Template template = Template.fromStack(stack);
        
        // Verify table has correct partition key
        template.hasResourceProperties("AWS::DynamoDB::Table", Map.of(
                "KeySchema", Map.of(
                        "AttributeName", "chatId",
                        "KeyType", "HASH"
                ),
                "AttributeDefinitions", Map.of(
                        "AttributeName", "chatId",
                        "AttributeType", "N"
                )
        ));
    }
}
