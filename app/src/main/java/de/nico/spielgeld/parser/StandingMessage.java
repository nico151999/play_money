package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class StandingMessage {

    private static final String IDENTIFIER = "STANDING";
    Map<String, Double> mAccounts;

    public static StandingMessage create(Map<String, Double> accounts) {
        StandingMessage standingMessage = new StandingMessage();
        standingMessage.mAccounts = accounts;
        return standingMessage;
    }

    private StandingMessage() {}

    private StandingMessage(String message) {
        String[] entries = message.split("\\n");
        mAccounts = new HashMap<>();
        String[] entry;
        for (int i = 1; i < entries.length; i++) {
            entry = entries[i].split(" ", 2);
            mAccounts.put(entry[0], Double.parseDouble(entry[1]));
        }
    }

    public static StandingMessage parse(String message) {
        if (message.matches(IDENTIFIER + "(\\n(.{2}:){5}.{2} \\d+\\.\\d+)+")) {
            return new StandingMessage(message);
        } else {
            return null;
        }
    }

    public Map<String, Double> getAccounts() {
        return mAccounts;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(IDENTIFIER);
        for (Map.Entry<String, Double> entry : mAccounts.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey());
            builder.append(" ");
            builder.append(entry.getValue());
        }
        return builder.toString();
    }
}
