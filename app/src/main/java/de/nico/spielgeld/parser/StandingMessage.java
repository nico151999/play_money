package de.nico.spielgeld.parser;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class StandingMessage {

    private static final String IDENTIFIER = "STANDING";
    private Map<String, Pair<String, Integer>> mAccounts;
    private String mBluetoothAddress;

    public StandingMessage(Map<String, Pair<String, Integer>> accounts, String bluetoothAddress) {
        mAccounts = accounts;
        mBluetoothAddress = bluetoothAddress;
    }

    public static StandingMessage parse(String message) {
        if (message.matches(IDENTIFIER + " (.{2}:){5}.{2}(\\n(.{2}:){5}.{2} -?\\d+ .+)+")) {
            String[] entries = message.split("\\n");
            Map<String, Pair<String, Integer>> accounts = new HashMap<>();
            String bluetoothAddress = entries[0].substring(IDENTIFIER.length() + 1);
            String[] entry;
            for (int i = 1; i < entries.length; i++) {
                entry = entries[i].split(" ", 3);
                accounts.put(entry[0], new Pair<>(entry[2], Integer.parseInt(entry[1])));
            }
            return new StandingMessage(accounts, bluetoothAddress);
        } else {
            return null;
        }
    }

    public Map<String, Pair<String, Integer>> getAccounts() {
        return mAccounts;
    }

    public String getBluetoothAddress() {
        return mBluetoothAddress;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(IDENTIFIER + " " + mBluetoothAddress);
        for (Map.Entry<String, Pair<String, Integer>> entry : mAccounts.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey());
            builder.append(" ");
            builder.append(entry.getValue().second);
            builder.append(" ");
            builder.append(entry.getValue().first);
        }
        return builder.toString();
    }
}
