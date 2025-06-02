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

        sendTurn();  // start the game
        if (!player1.isWebSocketPlayer()) {
            listenForMoves(player1);
        }
        if (!player2.isWebSocketPlayer()) {
            listenForMoves(player2);
        }

    }

    private void sendTurn(PlayerHandler previousPlayer, int row, int value) {
        PlayerHandler nextPlayer = getOtherPlayer(previousPlayer);
        nextPlayer.sendMessage("TURN " + previousPlayer.getPlayerName() + " " + row + " " + value);
        previousPlayer.sendMessage("WAIT");
        currentPlayer = nextPlayer;  // whose turn

    }

    public void sendTurn(PlayerHandlerAdapter adapter, int row, int value) {
        System.out.println(adapter.getPlayerName() + " " + row + value);

        PlayerHandler nextPlayer = player1.getPlayerName().equals(adapter.getPlayerName()) ? player2 : player1;
        nextPlayer.sendMessage("TURN " + adapter.getPlayerName() + " " + row + " " + value);
        adapter.sendMessage("WAIT");
        currentPlayer = nextPlayer;
    }

    private PlayerHandler getPlayerByName(String name) {
        if (player1.getPlayerName().equals(name)) {
            return player1;
        }
        return player2;
    }

    public void sendTurn(String playerName, int row, int value) {
        System.out.println("[SEND TURN] " + playerName + " " + row + " " + value);

        PlayerHandler previousPlayer = getPlayerByName(playerName);
        PlayerHandler nextPlayer = previousPlayer == player1 ? player2 : player1;

        nextPlayer.sendMessage("TURN " + playerName + " " + row + " " + value);
        previousPlayer.sendMessage("WAIT");

        currentPlayer = nextPlayer;
    }

    private void sendTurn() {
        currentPlayer.sendMessage("TURN " + currentPlayer.getPlayerName());
        getOtherPlayer(currentPlayer).sendMessage("WAIT");
    }

    private void listenForMoves(PlayerHandler player) {
        System.out.println(player);
        if (!(player instanceof PlayerHandler)) {
            System.out.println("Skipping listenForMoves for non-TCP player.");
            return;
        }
        new Thread(() -> {
            try {
                BufferedReader in = player.getInput();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("SCORE_LOCKED")) {
                        String[] parts = line.split(" ");
                        int row = Integer.parseInt(parts[1]);
                        String value = parts.length > 3 ? parts[3] : "0";

                        player.sendMessage("LOCK_SCORE " + row + " " + value);
                        getOtherPlayer(player).sendMessage("OPPONENT_LOCKED " + row + " " + value);

                        sendTurn(player, row, Integer.parseInt(value));
                    } else if (line.startsWith("ROLL_DICE ")) {
                        String diceValues = line.substring("ROLL_DICE ".length());
                        getOtherPlayer(player).sendMessage("DICE " + diceValues);
                    } else if (line.startsWith("GAME_OVER")) {
                        int score = Integer.parseInt(line.split(" ")[1]);
                        if (player == player1) {
                            player1Score = score;
                        } else {
                            player2Score = score;
                        }

                        if (player1Score != -1 && player2Score != -1) {
                            sendGameResult();
                        }
                    } else if (line.equals("RESTART")) {
                        MessageServer.registerPlayer(player);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnected: " + player.getPlayerName());
                PlayerHandler opponent = getOtherPlayer(player);
                opponent.sendMessage("GAME_RESULT WIN 0");
                MessageServer.removePlayer(player.getPlayerName());
            }
        }).start();
    }

    public PlayerHandler getOtherPlayer(PlayerHandler player) {
        if (player.getPlayerName().equals(player1.getPlayerName())) {
            return player2;
        } else {
            return player1;
        }
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

    public PlayerHandler getOtherPlayer(String name) {
        return player1.getPlayerName().equals(name) ? player2 : player1;
    }

    public void handleMessage(String playerName, String message) {
        System.out.println("[HANDLE] " + playerName + ": " + message);

        if (message.startsWith("SCORE_LOCKED")) {
            String[] parts = message.split(" ");
            int row = Integer.parseInt(parts[1]);
            String value = parts[2];

            PlayerHandler player = getPlayerByName(playerName);
            PlayerHandler opponent = getOtherPlayer(player);

            player.sendMessage("LOCK_SCORE " + row + " " + value);
            opponent.sendMessage("OPPONENT_LOCKED " + row + " " + value);

            sendTurn(player, row, Integer.parseInt(value));
        } else if (message.startsWith("ROLL_DICE ")) {
            String diceValues = message.substring("ROLL_DICE ".length());
            getOtherPlayer(playerName).sendMessage("DICE " + diceValues);
        } else if (message.startsWith("GAME_OVER")) {
            int score = Integer.parseInt(message.split(" ")[1]);

            if (player1.getPlayerName().equals(playerName)) {
                player1Score = score;
            } else {
                player2Score = score;
            }

            if (player1Score != -1 && player2Score != -1) {
                sendGameResult();
            }
        } else if (message.equals("RESTART")) {
            MessageServer.registerPlayer(getPlayerByName(playerName));
        } else if (message.equals("EXIT")) {
            PlayerHandler player = getPlayerByName(playerName);
            MessageServer.removePlayer(playerName);

            if (!player.isWebSocketPlayer()) {
                try {
                    player.getSocket().close();  // sadece TCP oyuncular için
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // WebSocket oyuncuysa WebSocket bağlantısını kapat
                if (player instanceof PlayerHandlerAdapter adapter) {
                    WebSocketPlayerHandler wsHandler = adapter.getWebSocketHandler();
                    if (wsHandler != null) {
                        wsHandler.getConnection().close(); // WebSocket bağlantısını kapat
                    }
                }
            }

            System.out.println("Player exited: " + playerName);
        }
    }
}
