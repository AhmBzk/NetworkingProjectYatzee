/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Ahmet
 */
public class GameRoom implements Runnable {

    private final PlayerHandler player1;
    private final PlayerHandler player2;
    private PlayerHandler currentPlayer;
    private int player1Score = -1;
    private int player2Score = -1;

    public GameRoom(PlayerHandler p1, PlayerHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = player1;

    }

    @Override
    public void run() {
        player1.sendMessage("START " + player1.getPlayerName() + " " + player2.getPlayerName());
        player2.sendMessage("START " + player2.getPlayerName() + " " + player1.getPlayerName());

        sendTurn();  // 🟢 ilk oyunu başlat
        listenForMoves(player1);
        listenForMoves(player2);
    }

    private void sendTurn(PlayerHandler previousPlayer, int row, int value) {
        PlayerHandler nextPlayer = getOtherPlayer(previousPlayer);
        nextPlayer.sendMessage("TURN " + previousPlayer.getPlayerName() + " " + row + " " + value);
        previousPlayer.sendMessage("WAIT");
        currentPlayer = nextPlayer;  // 🔁 doğru sıradaki oyuncuyu güncelle
    }

    private void sendTurn() {
        currentPlayer.sendMessage("TURN " + currentPlayer.getPlayerName());
        getOtherPlayer(currentPlayer).sendMessage("WAIT");
    }

    private void listenForMoves(PlayerHandler player) {
        new Thread(() -> {
            try {
                BufferedReader in = player.getInput();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("SCORE_LOCKED")) {
                        String[] parts = line.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        String value = parts.length > 3 ? parts[3] : "0";

                        // Ekranları güncelle
                        player.sendMessage("LOCK_SCORE " + row + " " + value);
                        getOtherPlayer(player).sendMessage("OPPONENT_LOCKED " + row + " " + value);

                        //  Sıra değiştir
                        sendTurn(player, row, Integer.parseInt(value));
                    } else if (line.startsWith("GAME_OVER")) {
                        int score = Integer.parseInt(line.split(" ")[1]);
                        if (player == player1) {
                            player1Score = score;
                        } else {
                            player2Score = score;
                        }

                        if (player1Score != -1 && player2Score != -1) {
                            sendGameResult(); // iki oyuncudan da geldiyse karşılaştır
                        }
                    } else if (line.equals("RESTART")) {
                        MessageServer.registerPlayer(player); // tekrar sıraya al
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private PlayerHandler getOtherPlayer(PlayerHandler player) {
        return (player == player1) ? player2 : player1;
    }

    private void sendGameResult() {
        if (player1Score > player2Score) {
            player1.sendMessage("GAME_RESULT WIN " + player1Score);
            player2.sendMessage("GAME_RESULT LOSE " + player1Score);
        } else if (player1Score < player2Score) {
            player1.sendMessage("GAME_RESULT LOSE " + player2Score);
            player2.sendMessage("GAME_RESULT WIN " + player2Score);
        } else {
            player1.sendMessage("GAME_RESULT DRAW " + player1Score);
            player2.sendMessage("GAME_RESULT DRAW " + player2Score);
        }
    }
}
