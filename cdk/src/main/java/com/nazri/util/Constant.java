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

}