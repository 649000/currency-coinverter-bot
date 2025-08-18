package com.nazri.stack;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import com.nazri.util.TagUtil;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.dynamodb.*;
import software.constructs.Construct;

import static com.nazri.util.Constant.USER_TABLE;

/**
 * DynamoDB stack for Currency Coinverter application.
 * 
 * Creates a DynamoDB table for storing user chat data and preferences.
 * The table uses chatId as the partition key and includes basic
 * provisioned capacity settings suitable for a small-scale application.
 */
public class DynamoDBStack extends Stack {

    private Table userTable;
    private final StackConfig stackConfig;

    /**
     * Constructs a new DynamoDB stack.
     * 
     * @param scope the parent construct
     * @param id the stack identifier
     * @param props stack properties
     * @param stackConfig environment-specific configuration
     */
    public DynamoDBStack(final Construct scope, final String id, final StackProps props, final StackConfig stackConfig) {
        super(scope, id, props);
        this.stackConfig = stackConfig;
        this.userTable = createUserTable();
        TagUtil.addTags(this.userTable, stackConfig);
    }

    /**
     * Creates the user table for storing chat and user preferences.
     * 
     * @return configured DynamoDB table
     */
    private Table createUserTable() {
        return Table.Builder.create(
                        this,
                        "currencycoinverter-user-table")
                .tableName(USER_TABLE)
                .billingMode(BillingMode.PROVISIONED)
                .partitionKey(Attribute.builder()
                        .name("chatId")
                        .type(AttributeType.NUMBER)
                        .build())
                .removalPolicy(RemovalPolicy.RETAIN)
                .readCapacity(1)
                .writeCapacity(1)
                .tableClass(TableClass.STANDARD)
                .build();
    }

    public Table getUserTable() {
        return userTable;
    }
}
