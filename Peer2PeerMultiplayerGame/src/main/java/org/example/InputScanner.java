package org.example;

import java.util.Scanner;

public class InputScanner extends Thread {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String body = scanner.nextLine();

            if (body.equals("/play")) {
                Peer.getSelfPeer().sendMessage(new Message(MessageType.PLAY_REQUEST, ""));
            } else {
                PeerList.broadcast(new Message(MessageType.CHAT, body));
            }

        }
    }
}
