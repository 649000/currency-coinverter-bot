package com.nazri.util;

public final class Constant {

    private Constant() {
        // restrict instantiation
    }

    public static final String PROJECT = "project";
    public static final String CURRENCYCOINVERTER = "currencycoinverter";
    public static final String ENVIRONMENT = "environment";

    public static final String DEV = "dev";
    public static final String SIT = "sit";
    public static final String UAT = "uat";
    public static final String PRD = "prd";

    //DynamoDB Table Names
    public static final String USER_TABLE = "currencycoinverter-user";

    public static final String TELEGRAM_BOT_TOKEN = System.getenv("CURRENCYCOINVERTER_TELEGRAM_BOT_TOKEN");
}