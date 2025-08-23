package com.nazri.config;

import com.nazri.util.Constant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the StackConfig class.
 * 
 * These tests validate the configuration builder pattern and
 * environment-specific settings.
 */
public class StackConfigTest {

    /**
     * Tests that the StackConfig builder creates configurations with correct DEV settings.
     */
    @Test
    public void testStackConfigBuilderWithDevEnvironment() {
        // When
        StackConfig config = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // Then
        assertNotNull(config);
        assertNotNull(config.getStackProps());
        assertFalse(config.getGraalvm());
        assertNotNull(config.getTags());
        assertEquals(Constant.DEV, config.getTags().get(Constant.ENVIRONMENT));
        assertEquals(Constant.CURRENCYCOINVERTER, config.getTags().get(Constant.PROJECT));
    }

    /**
     * Tests that the StackConfig builder creates configurations with correct PRD settings.
     */
    @Test
    public void testStackConfigBuilderWithPrdEnvironment() {
        // When
        StackConfig config = new StackConfig.Builder()
                .withEnvironment(Constant.PRD)
                .build();

        // Then
        assertNotNull(config);
        assertNotNull(config.getStackProps());
        assertTrue(config.getGraalvm());
        assertNotNull(config.getTags());
        assertEquals(Constant.PRD, config.getTags().get(Constant.ENVIRONMENT));
        assertEquals(Constant.CURRENCYCOINVERTER, config.getTags().get(Constant.PROJECT));
    }

    /**
     * Tests that the StackConfig has correct getter methods.
     */
    @Test
    public void testStackConfigGetters() {
        // Given
        StackConfig config = new StackConfig.Builder()
                .withEnvironment(Constant.DEV)
                .build();

        // When & Then
        assertNotNull(config.getStackProps());
        assertNotNull(config.getTags());
        assertFalse(config.getGraalvm());
    }
}
