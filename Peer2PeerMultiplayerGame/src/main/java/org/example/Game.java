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

    public boolean isWinner(char piece) {  // Now takes a char parameter
        // Check horizontal
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == piece &&
                        board[row][col+1] == piece &&
                        board[row][col+2] == piece &&
                        board[row][col+3] == piece) {
                    return true;
                }
            }
        }

        // Check vertical
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 7; col++) {
                if (board[row][col] == piece &&
                        board[row+1][col] == piece &&
                        board[row+2][col] == piece &&
                        board[row+3][col] == piece) {
                    return true;
                }
            }
        }

        // Check diagonal (up-right)
        for (int row = 3; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == piece &&
                        board[row-1][col+1] == piece &&
                        board[row-2][col+2] == piece &&
                        board[row-3][col+3] == piece) {
                    return true;
                }
            }
        }

        // Check diagonal (down-right)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == piece &&
                        board[row+1][col+1] == piece &&
                        board[row+2][col+2] == piece &&
                        board[row+3][col+3] == piece) {
                    return true;
                }
            }
        }

        return false;
    }

    // Update makeMove() to use the new isWinner()
    public boolean makeMove(int column, String player) {
        if (!player.equals(currentPlayer)) return false;

        column -= 1;
        if (column < 0 || column >= 7 || board[0][column] != ' ') {
            return false;
        }

        for (int i = 5; i >= 0; i--) {
            if (board[i][column] == ' ') {
                board[i][column] = currentPlayer.equals("PLAYER1") ? 'X' : 'O';
                // Check win condition with the piece that was just placed
                winner = isWinner(board[i][column]);
                break;
            }
        }

        currentPlayer = currentPlayer.equals("PLAYER1") ? "PLAYER2" : "PLAYER1";
        turn++;
        return true;
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
    public void deserializeBoard(String state) {
        String[] rows = state.split(";");
        for (int i = 0; i < rows.length; i++) {
            String[] cells = rows[i].split(",");
            for (int j = 0; j < cells.length; j++) {
                board[i][j] = cells[j].charAt(0) == '.' ? ' ' : cells[j].charAt(0);
            }
        }
    }
}