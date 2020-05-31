package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class SendMessage {

    private static final String IDENTIFIER = "SEND";
    private String mTargetAddress;
    private Double mAmount;

    public SendMessage(Double amount, String targetAddress) {
        mTargetAddress = targetAddress;
        mAmount = amount;
    }

    public static SendMessage parse(String message) {
        if (message.matches(IDENTIFIER + " \\d+\\.\\d+ .{2}:.{2}:.{2}:.{2}:.{2}:.{2}")) {
            String[] messages = message.substring(IDENTIFIER.length() + 1).split(" ", 2);
            return new SendMessage(Double.parseDouble(messages[0]), messages[1]);
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