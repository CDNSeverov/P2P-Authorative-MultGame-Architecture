package org.example;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeerList {
    private static LinkedList<Peer> peers = new LinkedList<>();
    private static ConcurrentLinkedQueue<Peer> waitingPlayers = new ConcurrentLinkedQueue<>();

    public synchronized static void addPlayer(Peer peer) {
        peers.add(peer);
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

        GameSession gameSession = new GameSession(player1, player2);
        new Thread(gameSession).start();
    }

    public synchronized static void endGameSession(GameSession session){
        Peer player1 = session.getPlayer1();
        Peer player2 = session.getPlayer2();

        removeFromQueue(player1);
        removeFromQueue(player2);

        player1.setInQueue(false);
        player2.setInQueue(false);

        try {
            session.cleanup();
        } catch (Exception e) {
            System.out.println("Error cleaning up game session: " + e.getMessage());
        }

        System.out.println("Game session ended between " + player1.getPlayerName() + " and " + player2.getPlayerName());
    }
}
