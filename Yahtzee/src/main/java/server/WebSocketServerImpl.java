/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Ahmet
 */
public class WebSocketServerImpl extends WebSocketServer {

    private final Map<WebSocket, WebSocketPlayerHandler> connectedPlayers = new ConcurrentHashMap<>();

    public WebSocketServerImpl(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
        conn.send("REQUEST_NAME");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        WebSocketPlayerHandler player = connectedPlayers.get(conn);

        // 1. Yeni gelen isim kayıtları
        if (player == null) {
            if (message.startsWith("NAME ")) {
                String playerName = message.substring(5).trim();
                if (MessageServer.isNameTaken(playerName)) {
                    conn.send("NAME_TAKEN");
                } else {
                    WebSocketPlayerHandler newPlayer = new WebSocketPlayerHandler(conn, playerName);
                    PlayerHandlerAdapter adapter = new PlayerHandlerAdapter(newPlayer);
                    newPlayer.setAdapter(adapter);

                    connectedPlayers.put(conn, newPlayer);
                    MessageServer.registerPlayer(adapter);
                    conn.send("NAME_ACCEPTED");

                }
            }
            return;
        }

        // 2. TURN mesajları dahil tüm mesajlar doğrudan handleMessage(playerName, message)
        if (player.getGameRoom() != null) {
            System.out.println("player.getGameRoom != null");
            System.out.println("Received from : " + player.getPlayerName() + " Message: " + message);
            player.getGameRoom().handleMessage(player.getPlayerName(), message);
        }
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started.");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + conn.getRemoteSocketAddress());

        WebSocketPlayerHandler player = connectedPlayers.remove(conn);
        if (player != null) {
            String playerName = player.getPlayerName();
            System.out.println("[DISCONNECT] Removing player: " + playerName);

            // 🔐 Adapter null olabilir. Null kontrolü şart.
            PlayerHandlerAdapter adapter = player.getAdapter();
            if (adapter != null) {
                GameRoom room = adapter.getGameRoom();
                if (room != null) {
                    PlayerHandler opponent = room.getOtherPlayer(playerName);
                    if (opponent != null) {
                        try {
                            opponent.sendMessage("GAME_RESULT WIN 0");
                        } catch (Exception e) {
                            System.err.println("[ERROR] Failed to send WIN to opponent: " + e.getMessage());
                        }
                    }
                }
            }

            MessageServer.removePlayer(playerName);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            connectedPlayers.remove(conn); // 🔧 bağlantı hatası olduysa temizle
        }
    }
}
