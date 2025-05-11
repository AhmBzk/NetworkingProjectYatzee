/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import GUI.GUILogin;
import java.io.*;
import java.net.Socket;
import GUI.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 *
 * @author Ahmet
 */
public class MessageClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GUILogin loginUI;
    private GUIGame gameUI;
    private boolean isMyTurn = false;
    private String playerName;
    private final Map<Integer, Object> lastScores = new HashMap<>();
    private WaitingRoom waitingRoom;

    public MessageClient(String serverAddress, GUILogin loginUI) throws IOException {
        this.loginUI = loginUI;
        socket = new Socket(serverAddress, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        listenToServer();
    }

    public void setLastScoreValue(int row, Object value) {
        lastScores.put(row, value);
    }

    public Object getLastScoreValue(int row) {
        return lastScores.get(row);
    }

    public void setGameUI(GUIGame gameUI) {
        this.gameUI = gameUI;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void sendName(String name) {
        this.playerName = name; 
        out.println(name);
    }

    public void send(String msg) {
        out.println(msg);
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        continue;
                    }

                    switch (line) {
                        case "REQUEST_NAME" ->
                            loginUI.requestName();
                        case "NAME_TAKEN" ->
                            loginUI.nameTaken();
                        case "NAME_ACCEPTED" ->
                            loginUI.nameAccepted();
                        case "WAIT" ->
                            isMyTurn = false;
                        default -> {
                            if (line.startsWith("START ")) {
                                String[] tokens = line.split(" ");
                                String opponent = tokens[1].equals(playerName) ? tokens[2] : tokens[1];
                                loginUI.startGame(opponent, playerName);  
                                if (waitingRoom != null) {
                                    waitingRoom.dispose();
                                    waitingRoom = null;
                                }
                            } else if (line.startsWith("TURN ")) {
                                isMyTurn = true;

                                String[] parts = line.split(" ");
                                if (parts.length == 4) {
                                    int row = Integer.parseInt(parts[2]);
                                    String value = parts[3];
                                    gameUI.lockScoreFromServer(row, value);
                                }
                            } else if (line.startsWith("GAME_RESULT")) {
                                String[] parts = line.split(" ");
                                String result = parts[1];
                                String score = parts[2];

                                if (gameUI != null) {
                                    gameUI.showResultPopup(result, score);
                                } else if (line.startsWith("LOCK_SCORE")) {

                                    int row = Integer.parseInt(parts[1]);
                                    String value = parts[2];

                                    // only on the player turn
                                    if (gameUI != null) {
                                        gameUI.lockScoreFromServer(row, value);
                                    }
                                } else if (line.startsWith("OPPONENT_LOCKED")) {

                                    int row = Integer.parseInt(parts[1]);
                                    String value = parts[2];

                                    if (gameUI != null) {
                                        gameUI.lockScoreFromServer(row, value);
                                    }
                                }
                            } else if (line.equals("RESTART")) {
                                SwingUtilities.invokeLater(() -> {
                                    if (waitingRoom == null) {
                                        waitingRoom = new WaitingRoom();
                                    }
                                });
                            } else if (line.startsWith("DICE ")) {
                                String diceStr = line.substring(5);
                                int[] opponentDice = new int[diceStr.length()];
                                for (int i = 0; i < diceStr.length(); i++) {
                                    opponentDice[i] = Character.getNumericValue(diceStr.charAt(i));
                                }
                                gameUI.updateOpponentDice(opponentDice);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
