package com.nazri.stack;

import com.nazri.util.Constant;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.dynamodb.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

import static com.nazri.util.Constant.USER_TABLE;

public class DynamoDBStack extends Stack {

    private Table userTable;
    private final Map<String, Table> tableMap;

    public DynamoDBStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        tableMap = new HashMap<>();
        createUserTable();
    }

    private void createUserTable() {
        userTable = Table.Builder.create(
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

        Tags.of(userTable).add(Constant.ENVIRONMENT, stackConfig.getTags().get(Constant.ENVIRONMENT));
        Tags.of(userTable).add(Constant.PROJECT, Constant.CURRENCYCOINVERTER);
    }
}
