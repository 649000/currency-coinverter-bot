package com.nazri.util;

import com.nazri.config.StackConfig;
import software.amazon.awscdk.Tags;
import software.constructs.IConstruct;

public class TagUtil {

    public static void addTags(IConstruct iConstruct, StackConfig stackConfig) {
        Tags.of(iConstruct).add(Constant.PROJECT, Constant.CURRENCYCOINVERTER);
        Tags.of(iConstruct).add(Constant.ENVIRONMENT, stackConfig.getTags().get(Constant.ENVIRONMENT));
    }
}
