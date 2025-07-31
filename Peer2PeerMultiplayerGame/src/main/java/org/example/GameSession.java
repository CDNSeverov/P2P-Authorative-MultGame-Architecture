package org.example;

import java.util.UUID;

public class GameSession {
    private Peer player1;
    private Peer player2;
    private Game game;
    private String sessionId;

    public GameSession(Peer player1, Peer player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game();
        this.sessionId = UUID.randomUUID().toString();

        player1.setCurrentGame(this);
        player2.setCurrentGame(this);

        player1.sendMessage(new Message(MessageType.GAME_START, "PLAYER1"));
        player2.sendMessage(new Message(MessageType.GAME_START, "PLAYER2"));

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

    public void handleMove(Peer sender, int column) {
        String player = sender == player1 ? "PLAYER1" : "PLAYER2";

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

        GameQueue.removeSession(this);
    }

    public void playerDisconnected(Peer peer) {
        Peer remaining = peer == player1 ? player2 : player1;
        if (remaining != null) {
            remaining.sendMessage(new Message(MessageType.GAME_END, "<!> Opponent disconnected"));
            remaining.setCurrentGame(null);
        }
        GameQueue.removeSession(this);
    }
}
