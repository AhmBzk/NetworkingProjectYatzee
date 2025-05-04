/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/**
 *
 * @author Ahmet
 */

public class Server {

    private static final int PORT = 12345;
    private static final List<Socket> waitingPlayers = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Server started... Waiting for players.");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("Player connected from IP: " + ip);

                synchronized (waitingPlayers) {
                    waitingPlayers.add(socket);
                    if (waitingPlayers.size() >= 2) {
                        Socket player1 = waitingPlayers.remove(0);
                        Socket player2 = waitingPlayers.remove(0);
                        System.out.println("Starting a new game session between two players...");
                        new Thread(new GameSession(player1, player2)).start();
                    } else {
                        System.out.println("Waiting for an opponent to pair...");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
