package org.example;

import java.util.UUID;

// Class is used to make the communication to the game possible for the 2 players

public class GameSession {

    private Peer player1;
    private Peer player2;
    private Peer authority;
    private Game game;
    private String sessionId;

    public GameSession(Peer player1, Peer player2, Peer authority) {
        this.player1 = player1;
        this.player2 = player2;
        this.authority = authority;
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

    public void handleMove(Peer sender, int column) {
        String player = sender == player1 ? "PLAYER1" : "PLAYER2";

        if (game.makeMove(column, player)) {
            sendGameState();

            if (game.winner) {
                endGame(player);
            } else if (game.turn > 42) {
                endGame("DRAW");
            }

            if (authority != null) {
                Message verifyRequest = new Message(MessageType.VERIFICATION_REQUEST, sessionId + "|" + game.printBoard());
                authority.sendMessage(verifyRequest);
            } else {
                sender.sendMessage(new Message(MessageType.GAME_STATE, "<!>Invalid Move"));
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
        Message endMessage = new Message(MessageType.GAME_END, result);

        player1.sendMessage(endMessage);
        player2.sendMessage(endMessage);

        player1.setCurrentGame(null);
        player2.setCurrentGame(null);

        GameLobby.removeSession(this);
    }

    public void playerDisconnected(Peer peer) {
        Peer remaining = peer == player1 ? player2 : player1;
        if (remaining != null) {
            remaining.sendMessage(new Message(MessageType.GAME_END, "<!> Opponent Disconnected"));
            remaining.setCurrentGame(null);
        }
        GameLobby.removeSession(this);
    }

    public String getSessionId() {
        return sessionId;
    }
}
