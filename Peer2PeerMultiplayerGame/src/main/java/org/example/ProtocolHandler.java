
package org.example;

import java.net.Socket;
import java.util.ArrayList;

public class ProtocolHandler extends Thread{
    private ArrayList<String> history = new ArrayList<>();

    @Override
    public void run() {
        while(true) {
            Task task = null;

            try {
                task = TaskQueue.queue.take();
            } catch (InterruptedException e) {
                System.out.println("<!> Could not take task from queue");
                continue;
            }

            if (!CryptoUtil.verifyMessage(task.message)) {
                System.out.println("<!> Invalid signature");
                continue;
            }

            if (history.contains(task.message.id)) {
                continue;
            }
            history.add(task.message.id);

            System.out.println(task.message.type);
//            System.out.println(task.message.body);

            try {
                switch (task.message.type) {
                    case CHAT -> handleChat(task);
                    case PEER_DISCOVERY_REQUEST -> handlePeerDiscoveryRequest(task);
                    case PEER_DISCOVERY_RESPONSE -> handlePeerDiscoveryResponse(task);
                    case SHARE_INFO -> handleShareInfo(task);
                    case PLAY_REQUEST -> handlePlayRequest(task);
                    case GAME_START -> handleGameStart(task);
                    case GAME_MOVE -> handleGameMove(task);
                    case GAME_STATE -> handleGameState(task);
                    case GAME_END -> handleGameEnd(task);
                }
            } catch (Exception e) {
                System.out.println("<!> Something went wrong digesting the message");
            }
        }
    }
    private void handleChat(Task task) {
        System.out.println(PeerList.getName(task.message.sender) + ": " + task.message.body);
        PeerList.broadcast(task.message);
    }

    private void handlePeerDiscoveryRequest(Task task) {
        int numOfIps = Integer.parseInt(task.message.body.trim());
        String[] ips = PeerList.getPeerIps(numOfIps);
        String body = String.join(";", ips);

        task.sender.sendMessage(new Message(MessageType.PEER_DISCOVERY_RESPONSE, body));
    }

    private void handlePeerDiscoveryResponse(Task task) {
        String[] ips = task.message.body.split(";");
        for (String ip : ips) {
            if (!ip.equals(Constants.MY_IP)) {
                PeerList.connectToRemote(ip, Constants.PORT);
            }
        }
    }

    private void handleShareInfo(Task task) {
        String[] tokens = task.message.body.split(";");
        if (tokens.length < 3) return;

        if (!PeerList.contactIps.containsKey(tokens[2])) {
            System.out.println("User " + tokens[1] + " entered the chat {" + tokens[0] + "}");
            PeerList.addContacts(tokens[0], tokens[1], tokens[2]);
            PeerList.broadcast(new Message(MessageType.SHARE_INFO, Constants.MY_IP + ";" + Constants.USERNAME + ";" + CryptoUtil.getPubKey()));
            task.sender.setRemoteUsername(tokens[1]);
        }
    }

    public void handlePlayRequest(Task task) {
        String senderUsername = task.message.body;
        ArrayList<Peer> peers = PeerList.peers;

        System.out.println(task.message.body);

        Peer sender = null;
        for (Peer peer : peers) {
            if (peer.getUsername().equals(senderUsername)) {
                sender = peer;
            }
        }

        if (!GameQueue.isInQueue(sender)) {
            GameQueue.addToWaitingQueue(sender);
            GameQueue.checkForMatches();
            System.out.println("Added " + sender.getUsername() + " to queue");
        }
    }
    private void handleGameStart(Task task) {
        System.out.println(task.message.body);
        String[] players = task.message.body.split(";");
        String localUsername = Constants.USERNAME;

        if (!players[0].equals(localUsername) && !players[1].equals(localUsername)) {
            return;
        }

        if (Peer.getSelf().getInGame()) {
            System.out.println("Already in a game, ignoring match");
            return;
        }

        Peer opponent = null;
        for (Peer peer : PeerList.getPeers()) {
            if (peer.getUsername().equals(players[0]) || peer.getUsername().equals(players[1]) && !peer.getUsername().equals(localUsername)) {
                opponent = peer;
                break;
            }
        }

        if (opponent == null) {
            System.out.println("<!> Waiting for connection to opponent...");
            GameQueue.joinLobby(Peer.getSelf());
            return;
        }

        new GameSession(Peer.getSelf(), opponent);
    }

    private void handleGameMove(Task task) {
        if (task.sender.getCurrentGame() == null) {
            System.out.println("<!> Move from player not in game: " + PeerList.getName(task.message.sender));
            return;
        }

        try {
            int column = Integer.parseInt(task.message.body);
            task.sender.getCurrentGame().handleMove(task.sender, column);
        } catch (NumberFormatException e) {
            System.out.println("Invalid move format from " + PeerList.getName(task.message.sender));
        }
    }
    private void handleGameState(Task task) {
        try {
            // Split board state and current player
            String[] parts = task.message.body.split("\\|", 2);
            if (parts.length < 2) {
                System.out.println("<!> Invalid game state format");
                return;
            }

            String boardState = parts[0];
            String currentPlayer = parts[1];

            // Split into rows
            String[] rows = boardState.split(";");
            if (rows.length != 6) {
                System.out.println("<!> Expected 6 rows, got " + rows.length);
                return;
            }

            char[][] board = new char[6][7];
            for (int i = 0; i < 6; i++) {
                if (rows[i].isEmpty()) continue;

                String[] cells = rows[i].split(",");
                if (cells.length != 7) {
                    System.out.println("<!> Row " + i + " has invalid cell count: " + cells.length);
                    return;
                }

                for (int j = 0; j < 7; j++) {
                    board[i][j] = cells[j].charAt(0) == '.' ? ' ' : cells[j].charAt(0);
                }
            }

            System.out.println("\nCurrent board:");
            System.out.println("1 2 3 4 5 6 7");
            System.out.println("-------------");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 7; j++) {
                    System.out.print(board[i][j]);
                    System.out.print(" ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("<!> Error processing game state: " + e.getMessage());
        }
    }

    private void handleGameEnd(Task task) {
        String result = task.message.body;
        switch (result) {
            case "PLAYER1":
            case "PLAYER2":
                System.out.println("\nGame over! " + result + " wins!");
                break;
            case "DRAW":
                System.out.println("\nGame ended in a draw!");
                break;
            case "OPPONENT_DISCONNECTED":
                System.out.println("\nOpponent disconnected! Game ended.");
                break;
            default:
                System.out.println("\nGame ended: " + result);
        }
        task.sender.setCurrentGame(null);
        task.sender.setInGame(false);
        System.out.println("Type /play to start a new game");
    }

}
