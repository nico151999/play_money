package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class SendMessage {

    private static final String IDENTIFIER = "SEND";
    private String mTargetAddress;
    private Double mAmount;

    private SendMessage(String message) {
        String[] messages = message.substring(IDENTIFIER.length() + 1).split(" ", 2);
        mTargetAddress = messages[1];
        mAmount = Double.parseDouble(messages[0]);
    }

    private SendMessage() {}

    public static SendMessage create(Double amount, String targetAddress) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.mAmount = amount;
        sendMessage.mTargetAddress = targetAddress;
        return sendMessage;
    }

    public static SendMessage parse(String message) {
        if (message.matches(IDENTIFIER + " \\d+\\.\\d+ .{2}:.{2}:.{2}:.{2}:.{2}:.{2}")) {
            return new SendMessage(message);
        } else {
            return null;
        }
    }

    public String getTargetAddress() {
        return mTargetAddress;
    }

    public Double getAmount() {
        return mAmount;
    }

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER + " " + mAmount + " " + mTargetAddress;
    }
}