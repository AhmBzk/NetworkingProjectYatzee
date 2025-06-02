/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import org.java_websocket.WebSocket;

/**
 *
 * @author Ahmet
 */
public class WebSocketPlayerHandler {

    private final WebSocket connection;
    private final String playerName;
    private GameRoom gameRoom;  // 🔁 yeni eklendi
    private PlayerHandlerAdapter adapter;

    public WebSocketPlayerHandler(WebSocket connection, String playerName) {
        this.connection = connection;
        this.playerName = playerName;
    }

    public void setAdapter(PlayerHandlerAdapter adapter) {
        this.adapter = adapter;
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    public PlayerHandlerAdapter getAdapter() {
        return adapter;
    }

    public void sendMessage(String message) {
        connection.send(message);
    }

    public String getPlayerName() {
        return playerName;
    }

    public WebSocket getConnection() {
        return connection;
    }

    public void setGameRoom(GameRoom room) {
        this.gameRoom = room;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

}
