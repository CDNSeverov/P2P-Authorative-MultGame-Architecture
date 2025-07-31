package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PeerList {
    private static ArrayList<Peer> peers = new ArrayList<>();
    private static HashMap<String, String> contactIps = new HashMap<>();
    private static HashMap<String, String> contactNames = new HashMap<>();
    public synchronized static void addPeer(Peer peer) {
        peers.add(peer);
    }

    public static boolean addContacts(String ip, String username, String pubkey) {
        if (contactIps.containsKey(pubkey)) {
            return false;
        }
        contactIps.put(pubkey, ip);
        contactNames.put(ip, username);
        return true;
    }
    public static String getName(String pubKey) {
        return contactNames.get(pubKey);
    }

    public synchronized static void removePeer(Peer peer) {
        peers.remove(peer);
        if (peer.getInQueue()) {
            GameQueue.removeFromQueue(peer);
        }
    }
    public synchronized static void broadcast(Message message) {
        for (Peer peer : peers) {
            peer.sendMessage(message);
        }
    }
    public synchronized static void connectToRemote(String ip, int port) {
        System.out.println("connectToRemote called");
        for (Peer p : peers) {
            if (p.getIp().equals(ip)) {
                return;
            }
        }

        Socket socket = null;
        Peer peer = null;

        try {
            socket = new Socket(ip, port);
            peer = new Peer(socket);
            new Thread(peer).start();
        } catch (IOException e) {
            System.out.println("Could not connect to peer at " + ip);
        }

        if (peers.size() < 2) {
            peer.sendMessage(new Message(MessageType.PEER_DISCOVERY_REQUEST, "2"));
        }

        peer.sendMessage(new Message(MessageType.SHARE_INFO, Constants.MY_IP + ";" + Constants.USERNAME + ";" + CryptoUtil.getPubKey()));
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

    public static ArrayList<Peer> getPeers() {
        return new ArrayList<>(peers);
    }
}
