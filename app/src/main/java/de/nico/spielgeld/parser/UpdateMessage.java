package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class UpdateMessage {

    private static final String IDENTIFIER = "UPDATE";
    private Double mAccount;
    private String mDeviceAddress;

    public static UpdateMessage create(Double account, String deviceAddress) {
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.mAccount = account;
        updateMessage.mDeviceAddress = deviceAddress;
        return updateMessage;
    }

    private UpdateMessage() {}

    private UpdateMessage(String message) {
        String[] messages = message.substring(IDENTIFIER.length() + 1).split(" ", 2);
        mDeviceAddress = messages[0];
        mAccount = Double.parseDouble(messages[1]);
    }

    public static UpdateMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2} -?\\d+\\.\\d+")) {
            return new UpdateMessage(message);
        } else {
            return null;
        }
    }

    public Double getAccount() {
        return mAccount;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER + " " + mDeviceAddress + " " + mAccount;
    }
}