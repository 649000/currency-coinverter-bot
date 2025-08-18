package com.nazri.config;

import com.nazri.util.Constant;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class StackConfig {

    private static final Logger logger = Logger.getLogger(StackConfig.class.getName());

    private final StackProps.Builder stackProps;
    private Boolean graalvm;
    private Map<String, String> tags;

    public StackConfig(StackProps.Builder stackProps, Map<String, String> tags, Boolean graalvm) {
        this.stackProps = stackProps;
        this.graalvm = graalvm;
        this.tags = tags;
    }

    public StackProps.Builder getStackProps() {
        return stackProps;
    }

    public Boolean getGraalvm() {
        return graalvm;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public static class Builder {
        private final StackProps.Builder stackProps = StackProps.builder();
        private Map<String, String> tags = new HashMap<>();
        private Boolean graalvm;

        public Builder withEnvironment(String environment) {
            tags.put(Constant.PROJECT, Constant.CURRENCYCOINVERTER);
            switch (environment) {
                case Constant.DEV -> {
                    logger.info("DEV StackConfig");
                    // TODO: Retrieve Account and Region from cdk.json
                    tags.put(Constant.ENVIRONMENT, Constant.DEV);
                    stackProps.env(Environment.builder()
                                    .region("ap-southeast-1")
                                    .build()
                            )
                            .tags(tags);
                    graalvm = false;
                }
            }
            return this;
        }

        public StackConfig build() {
            return new StackConfig(stackProps, tags, graalvm);
        }
    }
}
