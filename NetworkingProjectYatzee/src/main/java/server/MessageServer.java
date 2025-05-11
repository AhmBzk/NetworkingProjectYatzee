/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Ahmet
 */
public class MessageServer {

    private static final int PORT = 5000;
    private static final Set<String> playerNames = new HashSet<>();
    private static final Queue<PlayerHandler> waitingPlayers = new LinkedList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PlayerHandler playerHandler = new PlayerHandler(clientSocket);
                new Thread(playerHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean isNameTaken(String name) {
        return playerNames.contains(name);
    }

    public static synchronized void registerPlayer(PlayerHandler player) {
        // only add the new comer
        if (!playerNames.contains(player.getPlayerName())) {
            playerNames.add(player.getPlayerName());
        }

        waitingPlayers.add(player);

        if (waitingPlayers.size() >= 2) {
            PlayerHandler p1 = waitingPlayers.poll();
            PlayerHandler p2 = waitingPlayers.poll();
            GameRoom room = new GameRoom(p1, p2);
            new Thread(room).start();
        }
    }

    public static synchronized void removePlayer(String name) {
        playerNames.remove(name);
    }

}
