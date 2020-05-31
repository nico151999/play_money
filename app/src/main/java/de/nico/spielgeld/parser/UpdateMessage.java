package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class UpdateMessage {

    private static final String IDENTIFIER = "UPDATE";
    private Double mAccount;
    private String mDeviceAddress;

    public UpdateMessage(Double account, String deviceAddress) {
        mAccount = account;
        mDeviceAddress = deviceAddress;
    }

    public static UpdateMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2} -?\\d+\\.\\d+")) {
            String[] messages = message.substring(IDENTIFIER.length() + 1).split(" ", 2);
            return new UpdateMessage(Double.parseDouble(messages[1]), messages[0]);
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