package org.example;

import java.io.IOException;

public class GameSession implements Runnable {

    private Peer player1;
    private Peer player2;
    private Game game;
    private boolean cleanedUp = false;

    public GameSession(Peer player1, Peer player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Game(false);
    }

    public Peer getPlayer1() {
        return this.player1;
    }
    public Peer getPlayer2() {
        return this.player2;
    }

    public void cleanup() {
        if (cleanedUp) {
            return;
        }

        cleanedUp = true;

        notifyPlayersOfCleanup();
        releaseReferences();
        closeGameResources();

        System.out.println("GameSession cleanup completed for players");
    }

    private void notifyPlayersOfCleanup(){
        try {
            if (player1 != null && !player1.socket.isClosed()) {
                player1.sendMessage("GAME_CLEANUP_COMPLETE");
            }
            if (player2 != null && !player2.socket.isClosed()) {
                player2.sendMessage("GAME_CLEANUP_COMPLETE");
            }
        } catch (IOException e) {
            System.out.println("Cleanup notification failed: " + e.getMessage());
        }
    }

    private void releaseReferences() {
        this.player1 = null;
        this.player2 = null;
        this.game = null;
    }

    private void closeGameResources() {
        try {
            if (game != null) {
                game.closeResources();
            }
        } catch (Exception e) {
            System.out.println("Error cleaning up game resources: " + e.getMessage());
        }
    }
    @Override
    public void run() {
        try {
            player1.sendMessage("GAME_START:PLAYER1");
            player2.sendMessage("GAME_START:PLAYER2");
            
            while (!game.winner && game.turn <= 42) {
                Peer currentPlayer = (game.player == Game.players.PLAYER1) ? player1 : player2;
                Peer otherPlayer = (game.player == Game.players.PLAYER2) ? player2 : player1;
                
                sendGameState();
                
                currentPlayer.sendMessage("YOUR_TURN");
                String moveStr = currentPlayer.reader.readLine();
                if (moveStr == null) {
                    handleDisconnect(currentPlayer, otherPlayer);
                    return;
                }

                try {
                    int column = Integer.parseInt(moveStr);
                    if (!makeMove(column)) {
                        currentPlayer.sendMessage("INVALID_MOVE");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    currentPlayer.sendMessage("INVALID_MOVE");
                    continue;
                }

                if(game.isWinner()) {
                    sendGameState();
                    currentPlayer.sendMessage("GAME_OVER:WIN");
                    otherPlayer.sendMessage("GAME_OVER:LOSE");
                    break;
                } else if (game.turn == 42) {
                    sendGameState();
                    player1.sendMessage("GAME_OVER:TIE");
                    player2.sendMessage("GAME_OVER:TIE");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Game session error: " + e.getMessage());
        } finally {
            try {
                cleanup();
            } finally {
                PeerList.endGameSession(this);
            }
        }
    }

    private boolean makeMove(int column) {
        column -= 1;
        if (column < 0 || column >= game.board[0].length || game.board[0][column] != ' ') {
            return false;
        }

        for (int i = game.board.length - 1; i >= 0; i--) {
            if (game.board[i][column] == ' ') {
                game.board[i][column] = (game.player == Game.players.PLAYER1) ? '1' : '2';
                break;
            }
        }
        game.winner = game.isWinner();
        game.player = (game.player == Game.players.PLAYER1) ? Game.players.PLAYER2 : Game.players.PLAYER1;
        game.turn++;
        return true;
    }

    private void handleDisconnect(Peer disconnected, Peer other) throws IOException {
        try {
            if (other != null && other.socket != null && !other.socket.isClosed()) {
                other.sendMessage("GAME_OVER:OPPONENT_DISCONNECTED");
            }
        } catch (IOException e) {
            System.out.println("Error notifying player of disconnect: " + e.getMessage());
        }

        //other.sendMessage("GAME_OVER:OPPONENT_DISCONNECTED");
        System.out.println("Player disconnected: " + (disconnected == player1 ? "Player1" : "Player2"));

        PeerList.endGameSession(this);
    }

    private void sendGameState() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BOARD:");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                sb.append(game.board[i][j]).append(",");
            }
        }
        sb.append("TURN:").append(game.player.toString());

        player1.sendMessage(sb.toString());
        player2.sendMessage(sb.toString());
    }
}
