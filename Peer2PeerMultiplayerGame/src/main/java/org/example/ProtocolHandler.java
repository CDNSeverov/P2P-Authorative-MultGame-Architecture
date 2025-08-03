
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

//            System.out.println(task.message.type);
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
                    case AUTHORITY_VERDICT -> handleAuthorityVerdict(task);
                    case AUTHORITY_ASSIGN -> handleAuthorityAssign(task);
                    case AUTHORITY_VERIFY -> handleAuthorityVerify(task);
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

    // In ProtocolHandler.java
    public void handlePlayRequest(Task task) {
        if (!Constants.MY_IP.equals(Constants.BOOTSTRAP_IP)) {
            return;
        }

        String senderUsername = task.message.body;
        Peer sender = task.sender;

        if (senderUsername.equals(Constants.USERNAME) && sender == null) {
            GameQueue.joinLobby(Peer.getSelf());
            return;
        }

        if (!GameQueue.isInQueue(sender)) {
            GameQueue.addToWaitingQueue(sender);
            System.out.println("Bootstrap added " + senderUsername + " to queue");
            GameQueue.checkForMatches();
        }
    }
    private void handleGameStart(Task task) {
        String[] parts = task.message.body.split(":", 2);
        if (parts.length != 2) {
            System.out.println("Invalid GAME_START message: " + task.message.body);
            return;
        }

        String assignedRole = parts[0];
        String opponentUsername = parts[1];

        if (Peer.getSelf().getInGame()) {
            System.out.println("Already in a game, ignoring match");
            return;
        }

        Peer opponent = PeerList.getPeer(opponentUsername);
        if (opponent == null) {
            System.out.println("Could not find opponent: " + opponentUsername);
            Peer.getSelf().setInQueue(false);
            GameQueue.joinLobby(Peer.getSelf());
            return;
        }

        Peer.getSelf().setPlayerRole(assignedRole);

        GameSession session = new GameSession(Peer.getSelf(), opponent);

        System.out.println("\nGame started! Your opponent is " + opponentUsername);
        System.out.println("You are: " + assignedRole);
        session.getGame().printBoard();

        if (assignedRole.equals("PLAYER1")) {
            System.out.println("You go first! Enter column number (1-7):");
        } else {
            System.out.println("Opponent goes first. Waiting for their move...");
        }
    }

    private void handleGameMove(Task task) {
        if (task.sender.getCurrentGame() == null) {
            System.out.println("<!> Move from player not in game: " + PeerList.getName(task.message.sender));
            return;
        }

        try {
            String[] tokens = task.message.body.split(";");
            int column = Integer.parseInt(tokens[0]);
            task.sender.getCurrentGame().handleMove(tokens[1], column);
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

            // Update local game state
            if (task.sender.getCurrentGame() != null) {
                Game game = task.sender.getCurrentGame().getGame();
                game.deserializeBoard(boardState);
                game.currentPlayer = currentPlayer;

                // Print the board
                System.out.println("\nCurrent board:");
                System.out.println("1 2 3 4 5 6 7");
                System.out.println("-------------");
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 7; j++) {
                        System.out.print(game.board[i][j] == ' ' ? '.' : game.board[i][j]);
                        System.out.print(" ");
                    }
                    System.out.println();
                }

                // Prompt for next move if it's our turn
                if (Peer.getSelf().getPlayerRole().equals(currentPlayer)) {
                    System.out.println("Your turn! Enter column number (1-7):");
                }
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

        Peer self = Peer.getSelf();
        self.setCurrentGame(null);
        self.setInGame(false);
        self.setPlayerRole(null);

        System.out.println("Type /play to start a new game");
    }

    private void handleAuthorityAssign(Task task) {
        String[] tokens = task.message.body.split(";");
        if (tokens.length < 4) return;

        String sessionId = tokens[0];
        String player1Key = tokens[1];
        String player2Key = tokens[2];
        String initialState = tokens[3];

        // Initialize game state
        Game game = new Game();
        game.deserializeBoard(initialState);
        Authority.storeGameState(sessionId, game);

        // For testing
        System.out.println("[AUTHORITY] Assigned to session: " + sessionId);
    }

    private void handleAuthorityVerify(Task task) {
        String[] tokens = task.message.body.split(";");
        if (tokens.length < 2) return;

        String sessionId = tokens[0];
        String moveData = tokens[1];
        Authority.verifyMove(sessionId, moveData);
    }

    private void handleAuthorityVerdict(Task task) {
        String[] tokens = task.message.body.split(";");
        if (tokens.length < 2) return;

        String sessionId = tokens[0];
        String verdict = tokens[1];

        if ("REJECT".equals(verdict)) {
            GameSession session = GameQueue.getSession(sessionId);
            if (session != null) {
                session.endGame("CHEATING");
            }
        }
    }
}