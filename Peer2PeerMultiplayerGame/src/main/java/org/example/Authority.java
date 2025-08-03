package org.example;

import java.util.HashMap;
import java.util.Map;

public class Authority {
    private static final Map<String, Game> gameSessions = new HashMap<>();
    private static final Map<String, String> sessionToAuthority = new HashMap<>();

    public static void assignAuthority(String sessionId, String authorityIp) {
        sessionToAuthority.put(sessionId, authorityIp);
    }

    public static void verifyMove(String sessionId, String moveData) {
        // For testing
        System.out.println("[AUTHORITY] Verifying move for session: " + sessionId);

        // Simulate validation
        boolean isValid = true;
        String verdict = isValid ? "ACCEPT" : "REJECT";

        // Send verdict to bootstrap
        Peer bootstrap = findBootstrapPeer();
        if (bootstrap != null) {
            String body = sessionId + ";" + verdict;
            bootstrap.sendMessage(new Message(MessageType.AUTHORITY_VERDICT, body));
        }
    }

    private static Peer findBootstrapPeer() {
        for (Peer peer : PeerList.getPeers()) {
            if (peer.getIp().equals(Constants.BOOTSTRAP_IP)) {
                return peer;
            }
        }
        return null;
    }

    public static String getAuthorityIp(String sessionId) {
        return sessionToAuthority.get(sessionId);
    }

    public static void storeGameState(String sessionId, Game game) {
        gameSessions.put(sessionId, game);
    }
}
