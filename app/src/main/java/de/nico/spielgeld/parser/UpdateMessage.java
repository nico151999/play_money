package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class UpdateMessage {

    private static final String IDENTIFIER = "UPDATE";
    private Integer mAccount;
    private String mDeviceAddress;

    public UpdateMessage(Integer account, String deviceAddress) {
        mAccount = account;
        mDeviceAddress = deviceAddress;
    }

    public static UpdateMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2} -?\\d+")) {
            String[] messages = message.substring(IDENTIFIER.length() + 1).split(" ", 2);
            return new UpdateMessage(Integer.parseInt(messages[1]), messages[0]);
        } else {
            return null;
        }
    }

    public Integer getAccount() {
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