package org.example;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeerList {
    private static ConcurrentLinkedQueue<Peer> waitingPlayers = new ConcurrentLinkedQueue<>();

    public synchronized static void addPlayer(Peer peer) {
        // add registration logic or something
    }

    public synchronized static void removePlayer(Peer peer) {
        removeFromQueue(peer);
    }

    public synchronized static void addToQueue(Peer peer) {
        if (!waitingPlayers.contains(peer)) {
            waitingPlayers.add(peer);
            checkForMatches();
        }
    }

    public synchronized static void removeFromQueue(Peer peer) {
        waitingPlayers.remove(peer);
    }

    private static void checkForMatches() {
        while (waitingPlayers.size() >= 2) {
            Peer player1 = waitingPlayers.poll();
            Peer player2 = waitingPlayers.poll();

            if (player1 != null && player2 != null) {
                try {
                    player1.sendMessage("MATCH_FOUND" + player2.getPlayerName());
                    player2.sendMessage("MATCH_FOUND" + player1.getPlayerName());

                    startGameSession(player1, player2);
                } catch (IOException e) {
                    System.out.println("Error matching players: " + e.getMessage());

                    if (player1 != null) waitingPlayers.add(player1);
                    if (player2 != null) waitingPlayers.add(player2);
                }
            }
        }
    }

    private static void startGameSession(Peer player1, Peer player2) {
        System.out.println("Starting game between " + player1.getPlayerName() + " and " + player2.getPlayerName());

        // Add instance of game
    }
}
