package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class RemoveMessage {

    private static final String IDENTIFIER = "REMOVE";
    private String mDeviceAddress;

    public static RemoveMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2}")) {
            return new RemoveMessage(message.substring(IDENTIFIER.length() + 1));
        } else {
            return null;
        }
    }

    public RemoveMessage(String deviceAddress) {
        mDeviceAddress = deviceAddress;
    }

    public String getDevice() {
        return mDeviceAddress;
    }

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER + " " + mDeviceAddress;
    }
}