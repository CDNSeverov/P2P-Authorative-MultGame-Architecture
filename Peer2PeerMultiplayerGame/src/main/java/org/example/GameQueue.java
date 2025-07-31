package org.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameQueue {
    private static Queue<Peer> waitingPlayers = new LinkedList<>();
    private static List<GameSession> activeSessions = new ArrayList<>();

    public static synchronized void joinLobby(Peer peer) {
        if (!waitingPlayers.contains(peer)) {
            waitingPlayers.add(peer);
            peer.setInQueue(true);
            peer.sendMessage(new Message(MessageType.PLAY_RESPONSE, "Waiting"));
            checkForMatches();
        }
    }

    public static synchronized void removeFromQueue(Peer peer) {
        waitingPlayers.remove(peer);
    }

    public static void checkForMatches() {
        while (waitingPlayers.size() >= 2) {
            Peer p1 = waitingPlayers.poll();
            Peer p2 = waitingPlayers.poll();

            GameSession session = new GameSession(p1, p2);
            activeSessions.add(session);
        }
    }

    public static synchronized void removeSession(GameSession session) {
        activeSessions.remove(session);
    }
}
