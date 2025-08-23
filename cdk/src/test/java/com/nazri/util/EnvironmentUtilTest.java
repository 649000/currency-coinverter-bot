package com.nazri.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EnvironmentUtil class.
 * 
 * These tests validate the environment normalization logic
 * used for deployment configuration.
 */
public class EnvironmentUtilTest {

    /**
     * Tests environment normalization logic with various inputs.
     */
    @Test
    public void testEnvironmentNormalization() {
        // Test null input
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment(null));
        
        // Test empty input
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment(""));
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment("   "));
        
        // Test production environment
        assertEquals(Constant.PRD, EnvironmentUtil.normalizeEnvironment("prd"));
        assertEquals(Constant.PRD, EnvironmentUtil.normalizeEnvironment("PRD"));
        assertEquals(Constant.PRD, EnvironmentUtil.normalizeEnvironment("PrD"));
        
        // Test non-production environments default to dev
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment("staging"));
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment("test"));
        assertEquals(Constant.DEV, EnvironmentUtil.normalizeEnvironment("uat"));
    }

    /**
     * Tests that the EnvironmentUtil class cannot be instantiated.
     */
    @Test
    public void testEnvironmentUtilCannotBeInstantiated() {
        assertThrows(UnsupportedOperationException.class, () -> {
            EnvironmentUtil.class.getDeclaredConstructor().newInstance();
        });
    }
}
