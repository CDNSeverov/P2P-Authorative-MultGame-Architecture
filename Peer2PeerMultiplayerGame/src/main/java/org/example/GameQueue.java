package org.example;

import java.util.*;

public class GameQueue {
    private static Queue<Peer> waitingPlayers = new LinkedList<>();
    private static List<GameSession> activeSessions = new ArrayList<>();

    public static synchronized void joinLobby(Peer peer) {
        if (waitingPlayers.contains(peer) || peer.getInQueue()) {
            return;
        }

        waitingPlayers.add(peer);

        System.out.println("Added " + Constants.USERNAME + " to queue");
        peer.setInQueue(true);
        PeerList.broadcast(new Message(MessageType.PLAY_REQUEST, Constants.USERNAME));
        checkForMatches();
    }

    public static synchronized void addToWaitingQueue(Peer peer) {
        waitingPlayers.add(peer);
    }

    public static synchronized void removeFromQueue(Peer peer) {
        waitingPlayers.remove(peer);
        peer.setInQueue(false);
    }
    public static synchronized boolean isInQueue(Peer peer) {
        return waitingPlayers.contains(peer);
    }

    public static synchronized void checkForMatches() {
        if (waitingPlayers.size() >= 2) {
            Peer p1 = waitingPlayers.poll();
            Peer p2 = waitingPlayers.poll();

            if (p1 != null && p2 != null) {
                new GameSession(p1, p2);
            }
        }
    }

    public static synchronized void removeSession(GameSession session) {
        activeSessions.remove(session);
    }
}