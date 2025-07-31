package org.example;

import java.util.Scanner;

public class InputScanner extends Thread {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String body = scanner.nextLine();

            if (body.equals("/play")) {
                Peer peer = Peer.getPeer();
                if (peer != null) {
                    peer.getPeer().sendMessage(new Message(MessageType.PLAY_REQUEST, ""));
                } else {
                    System.out.println("<!> Not connected to any peer");
                }
            } else {
                PeerList.broadcast(new Message(MessageType.CHAT, body));
            }
        }
    }
}
