package com.nazri.util;

import com.nazri.config.StackConfig;
import software.amazon.awscdk.Tags;
import software.constructs.IConstruct;

/**
 * Utility class for applying standard tags to AWS resources.
 * 
 * This class provides helper methods to consistently apply project
 * and environment tags to CDK constructs across all stacks.
 */
public class TagUtil {

    /**
     * Applies standard tags to the specified construct.
     * 
     * Adds project and environment tags based on the stack configuration.
     * 
     * @param iConstruct the CDK construct to tag
     * @param stackConfig the stack configuration containing tag values
     */
    public static void addTags(IConstruct iConstruct, StackConfig stackConfig) {
        Tags.of(iConstruct).add(Constant.PROJECT, Constant.CURRENCYCOINVERTER);
        Tags.of(iConstruct).add(Constant.ENVIRONMENT, stackConfig.getTags().get(Constant.ENVIRONMENT));
    }
}
