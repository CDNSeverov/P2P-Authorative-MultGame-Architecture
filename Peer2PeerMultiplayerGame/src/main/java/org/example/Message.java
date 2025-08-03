package org.example;

import java.io.IOException;
import java.util.UUID;

public class Message {
    public final String id;
    public final MessageType type;
    public String sender;
    public String signature;
    public final String body;

    public Message(String rawMessage) throws IOException {
        String[] parts = rawMessage.split(" ", 5);

        if (parts.length != 5) {
            throw new IOException("<!> Invalid message structure");
        }

        this.id = parts[0];
        try {
            this.type = MessageType.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new IOException("<!> Invalid message structure");
        }
        this.sender = parts[2];
        this.signature = parts[3];
        this.body = parts[4];
    }

    public Message(MessageType type, String body) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.body = body;
    }

    public String toString() {
        return id + " " + type + " " + sender + " " + signature + " " + body;
    }
}