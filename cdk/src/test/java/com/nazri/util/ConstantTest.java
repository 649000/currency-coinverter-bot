package com.nazri.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Constant class.
 * 
 * These tests validate the constant values and environment variable validation.
 */
public class ConstantTest {

    /**
     * Tests that all constant values are correctly defined.
     */
    @Test
    public void testConstantValues() {
        // Test basic constants
        assertEquals("project", Constant.PROJECT);
        assertEquals("currencycoinverter", Constant.CURRENCYCOINVERTER);
        assertEquals("environment", Constant.ENVIRONMENT);
        assertEquals("dev", Constant.DEV);
        assertEquals("prd", Constant.PRD);
        
        // Test DynamoDB table name
        assertEquals("currencycoinverter-user", Constant.USER_TABLE);
    }

    /**
     * Tests that the Constant class cannot be instantiated.
     */
    @Test
    public void testConstantClassCannotBeInstantiated() {
        // This test verifies the private constructor prevents instantiation
        assertThrows(InstantiationException.class, () -> {
            Constant.class.getDeclaredConstructor().newInstance();
        });
    }
}
