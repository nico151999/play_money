package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class StartMessage {
    private static final String IDENTIFIER = "START";

    public static StartMessage parse(String message) {
        if (message.equals(IDENTIFIER)) {
            return new StartMessage();
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
