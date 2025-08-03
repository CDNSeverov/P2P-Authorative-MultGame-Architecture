package org.example;

import java.util.Scanner;

public class Game {
    public char[][] board = new char[6][7];
    public int turn = 1;
    public boolean winner = false;
    public String currentPlayer;

    public Game() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                board[i][j] = ' ';
            }
        }
        currentPlayer = "PLAYER1";
    }

    public boolean makeMove(int column, String player) {
        if (!player.equals(currentPlayer)) return false;

        column -= 1;
        if (column < 0 || column >= 7 || board[0][column] != ' ') {
            return false;
        }

        for (int i = 5; i >= 0; i--) {
            if (board[i][column] == ' ') {
                board[i][column] = currentPlayer.equals("PLAYER1") ? 'X' : 'O';
                break;
            }
        }

        winner = isWinner();
        currentPlayer = currentPlayer.equals("PLAYER1") ? "PLAYER2" : "PLAYER1";
        turn++;
        return true;
    }

    boolean isWinner() {
        char pman = currentPlayer.equals("PLAYER1") ? 'X' : 'O';

        //check for across
        for(int i = 0; i<board.length; i++){
            for (int j = 0; j < board[0].length - 3; j++){
                if (board[i][j] == pman && board[i][j+1] == pman && board[i][j+2] == pman && board[i][j+3] == pman) {
                    return true;
                }
            }
        }
        //check for up and down
        for(int i = 0; i < board.length - 3; i++){
            for(int j = 0; j < board[0].length; j++){
                if (board[i][j] == pman && board[i+1][j] == pman && board[i+2][j] == pman && board[i+3][j] == pman) {
                    return true;
                }
            }
        }
        //check upward diagonal
        for(int i = 3; i < board.length; i++){
            for(int j = 0; j < board[0].length - 3; j++){
                if (board[i][j] == pman && board[i-1][j+1] == pman && board[i-2][j+2] == pman && board[i-3][j+3] == pman) {
                    return true;
                }
            }
        }
        //check downward diagonal
        for(int i = 0; i < board.length - 3; i++){
            for(int j = 0; j < board[0].length - 3; j++){
                if (board[i][j] == pman && board[i+1][j+1] == pman && board[i+2][j+2] == pman && board[i+3][j+3] == pman){
                    return true;
                }
            }
        }

        return false;
    }

    public String printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                sb.append(board[i][j] == ' ' ? '.' : board[i][j]);
                if (j < 6) sb.append(",");
            }
            if (i < 5) sb.append(";");
        }
        return sb.toString();
    }
}
