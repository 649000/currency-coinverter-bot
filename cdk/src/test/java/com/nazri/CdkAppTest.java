package com.nazri;

import com.nazri.config.StackConfig;
import com.nazri.util.Constant;
import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CDK application.
 * 
 * These tests validate the core functionality of the CDK application,
 * including environment handling, stack creation, and resource configuration.
 */
public class CdkAppTest {

    /**
     * Tests stack configuration creation for different environments.
     */
    @Test
    public void testStackConfigCreation() {
        // Test production configuration
        StackConfig prdConfig = CdkApp.getStackConfig(Constant.PRD);
        assertNotNull(prdConfig);
        assertTrue(prdConfig.getGraalvm());
        
        // Test development configuration
        StackConfig devConfig = CdkApp.getStackConfig(Constant.DEV);
        assertNotNull(devConfig);
        assertFalse(devConfig.getGraalvm());
        
        // Test default configuration for unknown environment
        StackConfig unknownConfig = CdkApp.getStackConfig("unknown");
        assertNotNull(unknownConfig);
        assertFalse(unknownConfig.getGraalvm());
    }
}
