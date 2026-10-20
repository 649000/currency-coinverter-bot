package com.nazri.util;

import com.nazri.config.StackConfig;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.assertions.Template;
import software.amazon.awscdk.services.s3.Bucket;

import java.util.Map;

public class TagUtilTest {

    @Test
    public void testAddTags_ShouldApplyProjectAndEnvironmentTags() {
        App app = new App();
        Stack stack = new Stack(app, "test-tag-stack");
        StackConfig stackConfig = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        Bucket bucket = Bucket.Builder.create(stack, "test-bucket").build();
        TagUtil.addTags(bucket, stackConfig);

        Template template = Template.fromStack(stack);

        template.hasResourceProperties("AWS::S3::Bucket", Map.of(
                "Tags", software.amazon.awscdk.assertions.Match.arrayWith(java.util.List.of(
                        Map.of("Key", "environment", "Value", "dev"),
                        Map.of("Key", "project", "Value", "currencycoinverter")
                ))
        ));
    }
}
