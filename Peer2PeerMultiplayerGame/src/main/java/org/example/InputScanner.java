package org.example;

import java.util.Scanner;

public class InputScanner extends Thread {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String body = scanner.nextLine();
            Peer self = Peer.getSelf();

            if (self != null && self.getInGame()) {
                try {
                    int column = Integer.parseInt(body);
                    if (column < 1 || column > 7) {
                        System.out.println("<!> Column must be between 1-7");
                    } else {
                        self.sendMessage(new Message(MessageType.GAME_MOVE, body));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("<!> In-game, enter 1-7 for moves");
                }
                continue; // Critical: Skip chat processing
            }

            if (body.trim().equals("/play")) {
                if (self != null && !self.getInQueue()) {
                    GameQueue.joinLobby(self);
                }
            } else {
                PeerList.broadcast(new Message(MessageType.CHAT, body));
            }
        }
    }
}