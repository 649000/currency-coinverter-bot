package com.nazri.util;

import com.nazri.config.StackConfig;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.assertions.Template;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for the TagUtil class.
 * 
 * These tests validate that tags are correctly applied to AWS resources.
 */
public class TagUtilTest {

    /**
     * Tests that tags are correctly applied to constructs.
     */
    @Test
    public void testAddTagsToConstruct() {
        // Given
        App app = new App();
        Stack stack = new Stack(app, "TestStack", StackProps.builder().build());
        Bucket bucket = Bucket.Builder.create(stack, "TestBucket").build();
        
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When & Then
        assertDoesNotThrow(() -> {
            TagUtil.addTags(bucket, stackConfig);
        });
        
        // Verify tags are applied
        Template template = Template.fromStack(stack);
        template.hasResourceProperties("AWS::S3::Bucket", Map.of(
                "Tags", Map.of(
                        Constant.PROJECT, Constant.CURRENCYCOINVERTER,
                        Constant.ENVIRONMENT, Constant.DEV
                )
        ));
    }
}
