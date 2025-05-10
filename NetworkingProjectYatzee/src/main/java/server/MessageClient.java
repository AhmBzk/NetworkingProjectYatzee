/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import GUI.GUILogin;
import java.io.*;
import java.net.Socket;
import GUI.*;

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

    public MessageClient(String serverAddress, GUILogin loginUI) throws IOException {
        this.loginUI = loginUI;
        socket = new Socket(serverAddress, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        listenToServer();
    }

    public void setGameUI(GUIGame gameUI) {
        this.gameUI = gameUI;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void sendName(String name) {
        this.playerName = name; // oyuncu adı burada kaydediliyor
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
                                String myName = tokens[1];
                                String opponent = tokens[2];
                                this.playerName = myName; 
                                loginUI.startGame(opponent, myName);
                            } else if (line.startsWith("TURN ")) {
                                String turnOwner = line.substring(5);
                                isMyTurn = turnOwner.equals(playerName);
                            } else if (line.startsWith("GAME_RESULT")) {
                                String[] parts = line.split(" ");
                                String result = parts[1];
                                String score = parts[2];
                                if (gameUI != null) {
                                    gameUI.showResultPopup(result, score);
                                }
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
