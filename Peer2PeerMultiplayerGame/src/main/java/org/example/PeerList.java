package org.example;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class PeerList {
    private static List<Peer> peers = new ArrayList<>();
    private static Map<String, String> contactIps = new HashMap<>();
    private static Map<String, String> contactNames = new HashMap<>();

    // Just adds players to the peers list
    public synchronized static void addPeer(Peer peer) {
        peers.add(peer);
    }

    public static synchronized void removePeer(Peer peer) {
        peers.remove(peer);
        if (peer.isInLobby()) {
            GameLobby.removeFromQueue(peer);
        }
    }

    public static Peer findAuthority(Peer exclude1, Peer exclude2) {
        List<Peer> potential = peers.stream()
                .filter(p -> p != exclude1 && p != exclude2)
                .collect(Collectors.toList());

        if (potential.isEmpty()) return null;
        return potential.get(new Random().nextInt(potential.size()));
    }

    public synchronized static void broadcast(Message message) {
        for (Peer peer : peers) {
            peer.sendMessage(message);
        }
    }
    public static boolean addContacts(String ip, String username, String pubKey) {
        if (contactNames.containsKey(pubKey)) {
            return false;
        }
        contactIps.put(pubKey, ip);
        contactNames.put(ip, username);
        return true;
    }

    public static String getName(String pubKey) {
        return contactNames.get(pubKey);
    }
    public static String[] getPeerIps(int max) {
        int ipsToPull = Math.min(max, peers.size());
        Collections.shuffle(peers);
        String[] ips = new String[ipsToPull];
        for (int i = 0; i < ipsToPull; i++) {
            ips[i] = peers.get(i).getIp();
        }
        return ips;
    }

    public static void connectToRemote(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            Peer peer = new Peer(socket);
            new Thread(peer).start();

            if (peers.size() < 2) {
                peer.sendMessage(new Message(MessageType.PEER_DISCOVERY_REQUEST, "2"));
            }

            peer.sendMessage(new Message(MessageType.SHARE_INFO, Constants.MY_IP + ";" + Constants.USERNAME + ";" + CryptoUtil.getPubKey()));
        } catch (IOException e) {
            System.out.println("<!> Could not connect to remote: " + ip);
        }
    }


}
