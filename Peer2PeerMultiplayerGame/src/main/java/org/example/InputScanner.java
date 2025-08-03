package org.example;

import java.util.Scanner;

public class InputScanner extends Thread {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String body = scanner.nextLine();
            Peer self = Peer.getSelf();

            if (self.getInGame()) {
                if (Integer.parseInt(body) < 1 || Integer.parseInt(body) > 7) {
                    System.out.println("<!> Wrong input");
                    continue;
                }

                Peer.getSelf().sendMessage(new Message(MessageType.GAME_MOVE, body));
            }
            if (body.contains("/play")) {
                Peer localPeer = Peer.getSelf();
                if (localPeer != null && !localPeer.getInQueue()) {
                    if (Constants.MY_IP.equals(Constants.BOOTSTRAP_IP)) {
                        GameQueue.joinLobby(localPeer);
                    } else {
                        Peer bootstrapPeer = null;
                        for (Peer peer : PeerList.getPeers()) {
                            if (peer.getIp().equals(Constants.BOOTSTRAP_IP)) {
                                bootstrapPeer = peer;
                                break;
                            }
                        }
                        if (bootstrapPeer != null) {
                            bootstrapPeer.sendMessage(new Message(MessageType.PLAY_REQUEST, Constants.USERNAME));
                            localPeer.setInQueue(true);
                            System.out.println("Added " + Constants.USERNAME + " to queue");
                        }
                    }
                }
            }
        }
    }
}
