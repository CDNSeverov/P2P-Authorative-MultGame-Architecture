package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.System.exit;

// Test comment
public class GUI {
    private JFrame frame;
    private JPanel boardPanel;
    private JButton[] columnButtons;
    private JLabel[][] boardCells;
    private Game game;

    public GUI(Game game) {
        this.game = game;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Connect 4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 7));
        columnButtons = new JButton[7];

        for (int i = 0; i < 7; i++) {
            int column = i;
            columnButtons[i] = new JButton(String.valueOf(i + 1));
            columnButtons[i].addActionListener(e -> handleColumnClick(column));
            buttonPanel.add(columnButtons[i]);
        }

        frame.add(buttonPanel, BorderLayout.NORTH);

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(6, 7));
        boardCells = new JLabel[6][7];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                boardCells[i][j] = new JLabel();
                boardCells[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                boardCells[i][j].setVerticalAlignment(SwingConstants.CENTER);
                boardCells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                boardCells[i][j].setOpaque(true);
                boardCells[i][j].setBackground(Color.WHITE);
                boardPanel.add(boardCells[i][j]);
            }
        }

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void handleColumnClick(int column) {
        if (game.board[0][column] != ' ') {
            JOptionPane.showMessageDialog(frame, "Invalid move! Column is full.");
            return;
        }

        for (int i = game.board.length - 1; i >= 0; i--) {
            if (game.board[i][column] == ' ') {
                game.board[i][column] = (game.player == Game.players.PLAYER1) ? '1' : '2';
                break;
            }
        }

        if (game.isWinner()) {
            updateBoard();
            JOptionPane.showMessageDialog(frame, game.player + " is the WINNER!");
            frame.dispose();
            exit(0);
            return;
        }

        game.turn++;
        game.player = (game.player == Game.players.PLAYER1) ? Game.players.PLAYER2 : Game.players.PLAYER1;

        updateBoard();

        if (game.turn > 42) {
            JOptionPane.showMessageDialog(frame, "Out of moves! It's a TIE!");
            frame.dispose();
            exit(0);
        }
    }

    void updateBoard() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                char cell = game.board[i][j];
                if (cell == '1') {
                    boardCells[i][j].setBackground(Color.RED);
                } else if (cell == '2') {
                    boardCells[i][j].setBackground(Color.YELLOW);
                } else {
                    boardCells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }
}
