package org.example;

public class Task {
    public final Peer sender;
    public final Message message;

    public Task(Peer sender, Message message) {
        this.sender = sender;
        this.message = message;
    }
}
