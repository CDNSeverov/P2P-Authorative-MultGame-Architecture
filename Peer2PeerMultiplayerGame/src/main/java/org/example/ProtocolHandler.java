package org.example;

import java.util.ArrayList;

public class ProtocolHandler extends Thread {
    private ArrayList<String> history = new ArrayList<>();

    @Override
    public void run() {
        while(true) {
            Task task = null;

            try {
                task = TaskQueue.queue.take();
            } catch (InterruptedException e) {
                System.out.println("<!>Could not take task from queue");
                continue;
            }

            if(!CryptoUtil.verifyMessage(task.message)) {
                System.out.println("<!>Invalid signature");
                continue;
            }


            if (history.contains(task.message.id)) {
                continue;
            }
            history.add(task.message.id);

            try {
                switch (task.message.type) {
                    case CHAT -> handleChat(task);
                    case PEER_DISCOVERY_REQUEST -> handlePeerDiscoveryRequest(task);
                    case PEER_DISCOVERY_RESPONSE -> handlePeerDiscoveryResponse(task);
                    case SHARE_INFO -> handleShareInfo(task);
                    case GAME_START -> handleGameStart(task);
                    case GAME_MOVE -> handleGameMove(task);
                    case GAME_END -> handleGameEnd(task);
                    case GAME_STATE -> handleGameState(task);
                    case PLAY_REQUEST -> handlePlayRequest(task);
                    case PLAY_RESPONSE -> handlePlayResponse(task);
                    case VERIFICATION_REQUEST -> handleVerificationRequest(task);
                    case VERIFICATION_RESPONSE -> handleVerificationResponse(task);
                }
            } catch (Exception e) {
                System.out.println("<!>Something went wrong digesting the message");
            }
        }
    }
    private void handleChat(Task task) {
        System.out.println(task.sender.getUsername() + ": " + task.message.body);
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
        System.out.println("User " + tokens[1] + " entered the chat {" + tokens[0] + "}");
        boolean newContact = PeerList.addContacts(tokens[0], tokens[1], tokens[2]);
        if (newContact) {
            PeerList.broadcast(new Message(MessageType.SHARE_INFO,
                    Constants.MY_IP + ";" + Constants.USERNAME + ";" + CryptoUtil.getPubKey()));
        }
    }

    private void handleGameStart(Task task) {
        String playerRole = task.message.body; // Will be "PLAYER1" or "PLAYER2"
        System.out.println("\nGame started! You are " + playerRole);
        System.out.println("Enter column numbers (1-7) to make your moves");

        if (playerRole.equals("PLAYER1")) {
            System.out.println("It's your turn first!");
        } else {
            System.out.println("Waiting for opponent's move...");
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
        System.out.println("Type /play to start a new game");
    }

    private void handleGameState(Task task){
        String[] parts = task.message.body.split("\\|");
        String boardState = parts[0];
        String currentPlayer = parts.length > 1 ? parts[1] : "PLAYER1";

        String[] rows = boardState.split(",");
        char[][] board = new char[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                board[i][j] = rows[i*7 + j].charAt(0);
            }
        }

        System.out.println("\nCurrent board:");
        System.out.println("1 2 3 4 5 6 7");
        System.out.println("-------------");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                System.out.print(board[i][j] == ' ' ? '.' : board[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }

        Peer self = Peer.getSelfPeer();
        if (self.getCurrentGame() != null) {
            String myRole = self == self.getCurrentGame().getPlayer1() ? "PLAYER1" : "PLAYER2";
            if (currentPlayer.equals(myRole)) {
                System.out.println("It's your turn! Enter column (1-7):");
            } else {
                System.out.println("Waiting for opponent's move...");
            }
        }
    }
    private void handleGameMove(Task task) {
        if (task.sender.getCurrentGame() != null) {
            try {
                int column = Integer.parseInt(task.message.body);
                task.sender.getCurrentGame().handleMove(task.sender, column);
            } catch (NumberFormatException e) {
                task.sender.sendMessage(new Message(MessageType.GAME_STATE, "INVALID_MOVE"));
            }
        }
    }
    private void handlePlayRequest(Task task) {
        GameLobby.joinLobby(task.sender);
    }

    private void handlePlayResponse(Task task) {
        String status = task.message.body;
        if (status.equals("Waiting")) {
            System.out.println("Waiting for another player to join...");
        } else {
            System.out.println("Play response: " + status);
        }
    }

    private void handleVerificationRequest(Task task) {
        // Verify the game state
        String[] parts = task.message.body.split("\\|");
        String sessionId = parts[0];
        String boardState = parts[1];

        // In a real implementation, we would:
        // 1. Check if the board state is valid
        // 2. Verify move history if available
        // 3. Check for any inconsistencies

        // For this basic implementation, we'll just verify the board looks reasonable
        boolean isValid = true;
        String[] cells = boardState.split(",");
        if (cells.length != 42) { // 6x7 board
            isValid = false;
        } else {
            int xCount = 0, oCount = 0;
            for (String cell : cells) {
                if (cell.equals("X")) xCount++;
                else if (cell.equals("O")) oCount++;
                else if (!cell.equals(" ")) isValid = false;
            }

            // Check if counts are reasonable (difference should be 0 or 1)
            if (Math.abs(xCount - oCount) > 1) {
                isValid = false;
            }
        }

        String response = isValid ? "VALID" : "INVALID";
        task.sender.sendMessage(new Message(MessageType.VERIFICATION_RESPONSE,
                sessionId + "|" + response));
    }

    private void handleVerificationResponse(Task task) {
        // Handle verification responses from other peers
        String[] parts = task.message.body.split("\\|");
        String sessionId = parts[0];
        String result = parts.length > 1 ? parts[1] : "UNKNOWN";

        // In a real implementation, we would:
        // 1. Track verification responses
        // 2. Take action if multiple peers report invalid state
        // 3. Handle potential cheating scenarios

        System.out.println("Verification result for game " + sessionId + ": " + result);

        if (result.equals("INVALID")) {
            Peer self = Peer.getSelfPeer();
            if (self.getCurrentGame() != null &&
                    self.getCurrentGame().getSessionId().equals(sessionId)) {
                System.out.println("Warning: Game state verification failed!");
            }
        }
    }
}
