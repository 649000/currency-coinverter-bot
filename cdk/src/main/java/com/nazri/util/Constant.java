package com.nazri.util;

public final class Constant {

    private Constant() {
        // restrict instantiation
    }

    public static final String PROJECT = "project";
    public static final String CURRENCYCOINVERTER = "currencycoinverter";
    public static final String ENVIRONMENT = "environment";

    public static final String DEV = "dev";
    public static final String PRD = "prd";

    //DynamoDB Table Names
    public static final String USER_TABLE = "currencycoinverter-user";

    public static final String TELEGRAM_BOT_USERNAME = System.getenv("CURRENCYCOINVERTER_TELEGRAM_BOT_USERNAME");
    public static final String TELEGRAM_BOT_TOKEN = System.getenv("CURRENCYCOINVERTER_TELEGRAM_BOT_TOKEN");
    public static final String TELEGRAM_WEBHOOK_URL = System.getenv("CURRENCYCOINVERTER_TELEGRAM_BOT_URL");
    
    static {
        validateRequiredEnvVars();
    }
    
    private static void validateRequiredEnvVars() {
        StringBuilder missing = new StringBuilder();
        if (TELEGRAM_BOT_USERNAME == null) missing.append("CURRENCYCOINVERTER_TELEGRAM_BOT_USERNAME, ");
        if (TELEGRAM_BOT_TOKEN == null) missing.append("CURRENCYCOINVERTER_TELEGRAM_BOT_TOKEN, ");
        if (TELEGRAM_WEBHOOK_URL == null) missing.append("CURRENCYCOINVERTER_TELEGRAM_BOT_URL, ");
        
        if (missing.length() > 0) {
            throw new IllegalStateException("Missing required environment variables: " + 
                missing.substring(0, missing.length() - 2));
        }
    }
}
