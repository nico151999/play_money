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

    public static StartMessage create() {
        return new StartMessage();
    }

    private StartMessage() {}

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
