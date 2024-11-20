package com.nazri.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

import java.util.ArrayList;

@DynamoDbBean
public class User {

    //Partition Key: chatId
    private long chatId;
    private String telegramUsername;
    private String inputCurrency;
    private ArrayList<String> outputCurrency;
    private String createdDate;
    private String updatedDate;
    private boolean betaTester;

    @DynamoDbPartitionKey
    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }

    public String getInputCurrency() {
        return inputCurrency;
    }

    public void setInputCurrency(String inputCurrency) {
        this.inputCurrency = inputCurrency;
    }

    public ArrayList<String> getOutputCurrency() {
        return outputCurrency;
    }

    public void setOutputCurrency(ArrayList<String> outputCurrency) {
        this.outputCurrency = outputCurrency;
    }

    @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)
    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isBetaTester() {
        return betaTester;
    }

    public void setBetaTester(boolean betaTester) {
        this.betaTester = betaTester;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", telegramUsername='" + telegramUsername + '\'' +
                ", inputCurrency='" + inputCurrency + '\'' +
                ", outputCurrency=" + outputCurrency +
                ", createdDate='" + createdDate + '\'' +
                ", updatedDate='" + updatedDate + '\'' +
                ", betaTester=" + betaTester +
                '}';
    }


}
