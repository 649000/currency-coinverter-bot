package com.nazri.config;

import com.nazri.util.Constant;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Configuration class for CDK stacks that provides environment-specific settings.
 * 
 * This class encapsulates stack properties, deployment environment configuration,
 * and runtime options such as GraalVM vs JVM mode. It uses a builder pattern
 * to create environment-specific configurations.
 */
public class StackConfig {

    private static final Logger logger = Logger.getLogger(StackConfig.class.getName());

    private final StackProps.Builder stackProps;
    private Boolean graalvm;
    private Map<String, String> tags;

    /**
     * Constructs a new StackConfig with the specified properties.
     * 
     * @param stackProps the CDK stack properties builder
     * @param tags resource tags to apply to AWS resources
     * @param graalvm whether to use GraalVM native compilation
     */
    public StackConfig(StackProps.Builder stackProps, Map<String, String> tags, Boolean graalvm) {
        this.stackProps = stackProps;
        this.graalvm = graalvm;
        this.tags = tags;
    }

    /**
     * Gets the stack properties builder.
     * 
     * @return the CDK stack properties builder
     */
    public StackProps.Builder getStackProps() {
        return stackProps;
    }

    /**
     * Gets whether GraalVM native compilation is enabled.
     * 
     * @return true if GraalVM native compilation is enabled, false otherwise
     */
    public Boolean getGraalvm() {
        return graalvm;
    }

    /**
     * Gets the resource tags to apply to AWS resources.
     * 
     * @return map of tag key-value pairs
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Builder class for creating StackConfig instances.
     * 
     * Provides a fluent API for configuring stack properties based on
     * the target deployment environment.
     */
    public static class Builder {
        private final StackProps.Builder stackProps = StackProps.builder();
        private Map<String, String> tags = new HashMap<>();
        private Boolean graalvm;

        /**
         * Configures the stack for a specific deployment environment.
         * 
         * Sets up environment-specific settings including AWS region,
         * resource tags, and GraalVM compilation mode.
         * 
         * @param environment the target environment (dev or prd)
         * @return this builder instance for method chaining
         */
        public Builder withEnvironment(String environment) {
            tags.put(Constant.PROJECT, Constant.CURRENCYCOINVERTER);
            switch (environment) {
                case Constant.DEV -> {
                    logger.info("DEV StackConfig");
                    tags.put(Constant.ENVIRONMENT, Constant.DEV);
                    stackProps.env(Environment.builder()
                                    .region("ap-southeast-1")
                                    .build()
                            )
                            .tags(tags);
                    graalvm = false;
                }
                case Constant.PRD -> {
                    logger.info("PRD StackConfig");
                    tags.put(Constant.ENVIRONMENT, Constant.PRD);
                    stackProps.env(Environment.builder()
                                    .region("ap-southeast-1")
                                    .build()
                            )
                            .tags(tags);
                    graalvm = true;
                }
            }
            return this;
        }

        /**
         * Builds and returns a new StackConfig instance.
         * 
         * @return configured StackConfig instance
         */
        public StackConfig build() {
            return new StackConfig(stackProps, tags, graalvm);
        }
    }
}
