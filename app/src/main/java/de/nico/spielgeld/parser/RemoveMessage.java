package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class RemoveMessage {

    private static final String IDENTIFIER = "REMOVE";
    private String mDevice;

    public static RemoveMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2}")) {
            return new RemoveMessage(message);
        } else {
            return null;
        }
    }

    public static RemoveMessage create(String deviceAddress) {
        RemoveMessage message = new RemoveMessage();
        message.mDevice = deviceAddress;
        return message;
    }

    private RemoveMessage() {}

    private RemoveMessage(String message) {
        mDevice = message.substring(IDENTIFIER.length() + 1);
    }

    public String getDevice() {
        return mDevice;
    }

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER + " " + mDevice;
    }
}