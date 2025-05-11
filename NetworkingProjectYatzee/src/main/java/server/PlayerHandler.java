/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Ahmet
 */
public class PlayerHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private final Object[] lastScores = new Object[15];

    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void setLastScoreValue(int row, Object value) {
        lastScores[row] = value;
    }

    public Object getLastScoreValue(int row) {
        return lastScores[row];
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                out.println("REQUEST_NAME");
                String name = in.readLine();

                if (name == null || name.isBlank()) {
                    continue;
                }

                if (MessageServer.isNameTaken(name)) {
                    out.println("NAME_TAKEN");
                } else {
                    playerName = name;
                    MessageServer.registerPlayer(this);
                    out.println("NAME_ACCEPTED");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getInput() {
        return in;
    }
}
