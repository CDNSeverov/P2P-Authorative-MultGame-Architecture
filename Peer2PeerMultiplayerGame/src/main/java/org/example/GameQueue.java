package org.example;

import java.util.*;

public class GameQueue {
    public static Queue<Peer> waitingPlayers = new LinkedList<>();
    private static List<GameSession> activeSessions = new ArrayList<>();

    public static synchronized void joinLobby(Peer peer) {
        if (waitingPlayers.contains(peer) || peer.getInQueue()) {
            return;
        }

        waitingPlayers.removeIf(p -> !p.isConnectionAlive()) ;
        waitingPlayers.add(peer);

        peer.setInQueue(true);
        System.out.println("Added " + Constants.USERNAME + " to queue");
        PeerList.broadcast(new Message(MessageType.PLAY_REQUEST, Constants.USERNAME));
        checkForMatches();
    }

    public static synchronized void addToWaitingQueue(Peer peer) {
        waitingPlayers.add(peer);
        peer.setInQueue(true);
        checkForMatches();
    }

    public static synchronized void removeFromQueue(Peer peer) {
        waitingPlayers.remove(peer);
        peer.setInQueue(false);
    }
    public static synchronized boolean isInQueue(Peer peer) {
        return waitingPlayers.contains(peer);
    }

    public static void checkForMatches() {
        if (waitingPlayers.size() >= 2) {
            Peer player1 = waitingPlayers.poll();
            Peer player2 = waitingPlayers.poll();

            if (player1.isConnectionAlive() && player2.isConnectionAlive()) {
                player1.sendMessage(new Message(MessageType.GAME_START, player1.getUsername() + ";" + player2.getUsername()));
                player2.sendMessage(new Message(MessageType.GAME_START, player1.getUsername() + ";" + player2.getUsername()));
            }

        }
    }

    public static synchronized void removeSession(GameSession session) {
        activeSessions.remove(session);
    }
}
