package org.example;

import java.util.Scanner;

public class Game implements Runnable{
    Scanner in = new Scanner(System.in);
    public char[][] board = new char[6][7];
    public int turn = 1;
    public boolean winner = false;
    public GUI gui;
    public final boolean gameType;
    public enum players {
        PLAYER1,
        PLAYER2,
    }
    public players player = players.PLAYER1;

    public Game(boolean gameType) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                board[i][j] = ' ';
            }
        }
        this.gameType = gameType;
        if (gameType) {
            gui = new GUI(this);
        }
    }

    @Override
    public void run() {
        while(winner != true && turn <= 42) {
            if (gameType) {
                gui.updateBoard();
            } else {
                printBoard();
                play();
            }
            isWinner();
        }

        if (player == players.PLAYER1) {
            player = players.PLAYER2;
        } else if (player == players.PLAYER2) {
            player = players.PLAYER1;
        }

        if(winner == true) {
            printBoard();
            System.out.println(player + " is the WINNER!");
        } else if (turn == 43) {
            System.out.println("Out of moves! TIE!");
        }
    }

    private void play() {
        System.out.println(player + " pick a column!");

        int play = in.nextInt() - 1;

        if (play < 0 || play >= board[0].length) {
            System.out.println("Invalid move!");
            play();
            return;
        }

        if (board[0][play] != ' ') {
            System.out.println("Invalid move!");
            play();
            return;
        }

        for (int i = board.length - 1; i >= 0; i--) {
            if (board[i][play] == ' ') {
                if (player == players.PLAYER1) {
                    board[i][play] = '1';
                } else if (player == players.PLAYER2) {
                    board[i][play] = '2';
                }
                break;
            }
        }

        winner = isWinner();

        if (player == players.PLAYER1) {
            player = players.PLAYER2;
        } else if (player == players.PLAYER2) {
            player = players.PLAYER1;
        }

        turn = turn++;
    }

    boolean isWinner() {
        char pman = '0';

        if (player == players.PLAYER1) {
            pman = '1';
        } else if (player == players.PLAYER2) {
            pman = '2';
        }
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

    public void printBoard() {
        System.out.println("1 | 2 | 3 | 4 | 5 | 6 | 7 |");
        System.out.println("===========================");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
        }
    }
}