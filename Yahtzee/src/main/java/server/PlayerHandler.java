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

    protected final Socket socket;
    protected BufferedReader in;
    protected PrintWriter out;
    private String playerName;
    private final Object[] lastScores = new Object[15];
    private int[] currentDice;
    private GameRoom gameRoom;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isWebSocketPlayer() {
        return false;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void setLastScoreValue(int row, Object value) {
        lastScores[row] = value;
    }

    public Object getLastScoreValue(int row) {
        return lastScores[row];
    }

    public void setCurrentDice(int[] dice) {
        this.currentDice = dice;
    }

    public int[] getCurrentDice() {
        return currentDice;
    }
    public boolean isConnected() {
    return true; // TCP bağlantısı zaten dinleniyor; 'sendMessage' çağrısı çalışır
    }
    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom room) {
        this.gameRoom = room;
    }

    public BufferedReader getInput() {
        return in;
    }

    public Socket getSocket() {
        return socket;
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
                    setPlayerName(name);
                    MessageServer.registerPlayer(this);
                    out.println("NAME_ACCEPTED");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
