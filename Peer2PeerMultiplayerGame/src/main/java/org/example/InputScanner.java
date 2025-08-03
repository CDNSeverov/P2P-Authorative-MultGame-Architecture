package org.example;

import java.util.Scanner;

public class InputScanner extends Thread {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine().trim();
            Peer self = Peer.getSelf();

            if (self.getInGame()) {
                try {
                    int column = Integer.parseInt(input);
                    if (column < 1 || column > 7) {
                        System.out.println("Invalid column! Choose 1-7");
                        continue;
                    }
                    self.sendMessage(new Message(MessageType.GAME_MOVE, input + ";" + self.getPlayerRole()));
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number between 1-7");
                }
            }
            else if (input.equalsIgnoreCase("/play")) {
                if (self.getInQueue() || self.getInGame()) {
                    System.out.println("You're already in queue or in a game");
                    continue;
                }

                System.out.println("Joining matchmaking queue...");
                if (Constants.MY_IP.equals(Constants.BOOTSTRAP_IP)) {
                    GameQueue.joinLobby(self);
                } else {
                    // Find bootstrap peer
                    for (Peer peer : PeerList.getPeers()) {
                        if (peer.getIp().equals(Constants.BOOTSTRAP_IP)) {
                            peer.sendMessage(new Message(MessageType.PLAY_REQUEST, Constants.USERNAME));
                            self.setInQueue(true);
                            System.out.println("Added to queue");
                            break;
                        }
                    }
                }
            }
            else {
                Message chatMsg = new Message(MessageType.CHAT, input);
                PeerList.broadcast(chatMsg);
            }
        }
    }
}