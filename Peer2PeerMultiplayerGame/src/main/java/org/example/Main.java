package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        final boolean gameType = true;
        Game game = new Game(gameType);
        Thread g = new Thread(game);
        g.start();
    }
}
