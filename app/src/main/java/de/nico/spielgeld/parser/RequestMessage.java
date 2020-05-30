package de.nico.spielgeld.parser;

import androidx.annotation.NonNull;

public class RequestMessage {

    private static final String IDENTIFIER = "REQUEST";

    public static RequestMessage parse(String message) {
        if (message.equals(IDENTIFIER)) {
            return new RequestMessage();
        } else {
            return null;
        }
    }

    public static RequestMessage create() {
        return new RequestMessage();
    }

    private RequestMessage() {}

    @NonNull
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
