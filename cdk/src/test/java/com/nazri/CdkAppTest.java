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
     * Tests environment normalization logic with various inputs.
     */
    @Test
    public void testEnvironmentNormalization() {
        // Test null input
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment(null));
        
        // Test empty input
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment(""));
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment("   "));
        
        // Test production environment
        assertEquals(Constant.PRD, CdkApp.normalizeEnvironment("prd"));
        assertEquals(Constant.PRD, CdkApp.normalizeEnvironment("PRD"));
        assertEquals(Constant.PRD, CdkApp.normalizeEnvironment("PrD"));
        
        // Test non-production environments default to dev
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment("staging"));
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment("test"));
        assertEquals(Constant.DEV, CdkApp.normalizeEnvironment("uat"));
    }

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
