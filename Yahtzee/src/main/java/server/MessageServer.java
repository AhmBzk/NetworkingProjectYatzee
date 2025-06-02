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
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Ahmet
 */
public class MessageServer {

    private static final int PORT = 5000;
    private static final int WEBSOCKET_PORT = 8887;

    private static final Set<String> playerNames = new HashSet<>();
    private static final Queue<PlayerHandler> waitingPlayers = new LinkedList<>();
    private static final Map<String, GameRoom> activeGames = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // TCP server (Swing GUI için)
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("TCP Server started on port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    PlayerHandler playerHandler = new PlayerHandler(clientSocket);
                    new Thread(playerHandler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // WebSocket server (Tarayıcı için)
        new Thread(() -> {
            WebSocketServerImpl wsServer = new WebSocketServerImpl(WEBSOCKET_PORT);
            wsServer.start();
            System.out.println("WebSocket Server started on port " + WEBSOCKET_PORT);
        }).start();
    }

    public static synchronized boolean isNameTaken(String name) {
        return playerNames.contains(name);
    }

    public static synchronized void registerPlayer(PlayerHandler player) {
        if (!playerNames.contains(player.getPlayerName())) {
            playerNames.add(player.getPlayerName());
        }

        waitingPlayers.add(player);

        if (waitingPlayers.size() >= 2) {
            PlayerHandler p1 = waitingPlayers.poll();
            PlayerHandler p2 = waitingPlayers.poll();
            GameRoom room = new GameRoom(p1, p2);
            registerGameRoom(p1, p2, room);
            new Thread(room).start();
        }
    }

    public static synchronized void removePlayer(String name) {
        playerNames.remove(name);
        activeGames.remove(name);
        System.out.println("Removed player: " + name);
    }

    // WebSocket tarafı için overload edilmiş metot
    public static synchronized void registerWebSocketPlayer(WebSocketPlayerHandler wsPlayer) {
        PlayerHandlerAdapter adapter = new PlayerHandlerAdapter(wsPlayer);
        registerPlayer(adapter);
    }

    public static void registerGameRoom(PlayerHandler p1, PlayerHandler p2, GameRoom room) {
        activeGames.put(p1.getPlayerName(), room);
        activeGames.put(p2.getPlayerName(), room);

        // WebSocket bağlantısı olanlar için room set et
        if (p1 instanceof PlayerHandlerAdapter) {
            PlayerHandlerAdapter adapter1 = (PlayerHandlerAdapter) p1;
            if (adapter1.getOriginalHandler() instanceof WebSocketPlayerHandler) {
                WebSocketPlayerHandler ws1 = (WebSocketPlayerHandler) adapter1.getOriginalHandler();
                ws1.setGameRoom(room);
            }
        }

        if (p2 instanceof PlayerHandlerAdapter) {
            PlayerHandlerAdapter adapter2 = (PlayerHandlerAdapter) p2;
            if (adapter2.getOriginalHandler() instanceof WebSocketPlayerHandler) {
                WebSocketPlayerHandler ws2 = (WebSocketPlayerHandler) adapter2.getOriginalHandler();
                ws2.setGameRoom(room);
            }
        }
    }

}
