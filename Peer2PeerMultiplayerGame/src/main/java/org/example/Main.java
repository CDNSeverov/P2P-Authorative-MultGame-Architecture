package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        CryptoUtil.generatePubPvtKey();
        new Server().start();
        new InputScanner().start();
        new ProtocolHandler().start();
    }
}
