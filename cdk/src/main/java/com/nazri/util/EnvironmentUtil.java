package com.nazri.util;

/**
 * Utility class for environment-related operations.
 * 
 * This class provides helper methods for normalizing and validating
 * deployment environments across the application.
 */
public class EnvironmentUtil {

    /**
     * Normalizes the environment string to supported values.
     * 
     * @param env the environment string from context
     * @return normalized environment (DEV or PRD)
     */
    public static String normalizeEnvironment(String env) {
        if (env == null || env.trim().isEmpty()) {
            System.out.println("No environment specified, defaulting to: " + Constant.DEV);
            return Constant.DEV;
        }
        
        String normalized = env.trim().toLowerCase();
        if (Constant.PRD.equals(normalized)) {
            return Constant.PRD;
        } else {
            System.out.println("Environment '" + env + "' not recognized as production, using: " + Constant.DEV);
            return Constant.DEV;
        }
    }

    // Private constructor to prevent instantiation
    private EnvironmentUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
}
