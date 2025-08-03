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

    public Game getGame() {
        return this.game;
    }

    // Fixed move handling
    public void handleMove(String role, int column) {
        if (!role.equals(game.currentPlayer)) {
            System.out.println("<!> Not your turn");
            return;
        }

        if (game.makeMove(column, role)) {
            sendGameState();

            if (game.winner) {
                endGame(role);
            } else if (game.turn > 42) {
                endGame("DRAW");
            }
        }
    }

    private void sendGameState() {
        String state = game.printBoard() + "|" + game.currentPlayer;
        Message stateMessage = new Message(MessageType.GAME_STATE, state);

        // Send to both players (removed sender check)
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
        player1.setPlayerRole(null);
        player2.setPlayerRole(null);

        GameQueue.removeSession(this);
    }

    public void playerDisconnected(Peer peer) {
        Peer remainingPlayer = (peer == player1) ? player2 : player1;
        if (remainingPlayer != null) {
            remainingPlayer.sendMessage(new Message(MessageType.GAME_END, "OPPONENT_DISCONNECTED"));

            remainingPlayer.setCurrentGame(null);
            remainingPlayer.setInGame(false);
            remainingPlayer.setPlayerRole(null);
        }

        peer.setCurrentGame(null);
        peer.setInGame(false);
        peer.setPlayerRole(null);

        GameQueue.removeSession(this);
    }
}