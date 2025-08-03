package org.example;

import java.util.UUID;

public class GameSession {
    private Peer player1;
    private Peer player2;
    private Game game;
    private String sessionId;

    public GameSession(Peer player1, Peer player2) {
        if (player1 == null || player2 == null) {
            System.out.println("<!> One of the players disconnected before game start");
            return;
        }

        System.out.println("here");

        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game();
        this.sessionId = UUID.randomUUID().toString();

        player1.setCurrentGame(this);
        player2.setCurrentGame(this);
        player1.setInGame(true);
        player2.setInGame(true);

        player1.setInQueue(false);
        player2.setInQueue(false);
        GameQueue.removeFromQueue(player1);
        GameQueue.removeFromQueue(player2);

        player1.setPlayerRole("PLAYER1");
        player2.setPlayerRole("PLAYER2");

        player1.sendMessage(new Message(MessageType.GAME_START, player1.getPlayerRole()));
        player2.sendMessage(new Message(MessageType.GAME_START, player2.getPlayerRole()));

        sendGameState();
    }

    public Peer getPlayer1() {
        return this.player1;
    }

    public Peer getPlayer2() {
        return this.player2;
    }

    public String getSessionId() {
        return sessionId;
    }

    // In GameSession class
    public void handleMove(Peer sender, int column) {
        String player = sender.getPlayerRole();

        // Add validation for current player
        if (!player.equals(game.currentPlayer)) {
            System.out.println("<!> Not your turn");
            return;
        }

        // Add move validation
        if (column < 1 || column > 7) {
            System.out.println("<!> Invalid column");
            return;
        }

        if (game.makeMove(column, player)) {
            sendGameState();
            if (game.winner) {
                endGame(player);
            } else if (game.turn > 42) {
                endGame("DRAW");
            }
        }
    }

    private void sendGameState() {
        String state = game.printBoard() + "|" + game.currentPlayer;
        Message stateMessage = new Message(MessageType.GAME_STATE, state);

        player1.sendMessage(stateMessage);
        player2.sendMessage(stateMessage);
    }

    private void endGame(String result) {
        Message resultMessage = new Message(MessageType.GAME_END, result);

        player1.sendMessage(resultMessage);
        player2.sendMessage(resultMessage);

        player1.setCurrentGame(null);
        player2.setCurrentGame(null);
        player1.setInGame(false);
        player2.setInGame(false);

        GameQueue.removeSession(this);
    }

    public void playerDisconnected(Peer peer) {
        String result = "OPPONENT_DISCONNECTED";
        if (player1 != null) {
            player1.sendMessage(new Message(MessageType.GAME_END, result));
            player1.setCurrentGame(null);
            player1.setPlayerRole(null);
            player1.setInGame(false);
        }
        if (player2 != null) {
            player2.sendMessage(new Message(MessageType.GAME_END, result));
            player2.setCurrentGame(null);
            player2.setPlayerRole(null);
            player2.setInGame(false);
        }
        GameQueue.removeSession(this);
    }
}