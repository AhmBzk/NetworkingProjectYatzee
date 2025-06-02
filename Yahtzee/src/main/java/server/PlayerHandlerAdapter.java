/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author Ahmet
 */
public class PlayerHandlerAdapter extends PlayerHandler {

    private final WebSocketPlayerHandler wsPlayer;
    private final Object[] lastScores = new Object[15];
    private int[] currentDice;
    private PlayerHandlerAdapter adapter;

    public PlayerHandlerAdapter(WebSocketPlayerHandler wsPlayer) {
        super(null); // Socket bazlı bağlantı kullanılmıyor
        this.wsPlayer = wsPlayer;
        super.setPlayerName(wsPlayer.getPlayerName());
        wsPlayer.setAdapter(this); // WebSocket handler içine adapter yerleştir
    }

    public boolean isConnected() {
        return adapter.isConnected();
    }

    @Override
    public void sendMessage(String message) {
        wsPlayer.sendMessage(message);
    }

    public WebSocketPlayerHandler getOriginalHandler() {
        return wsPlayer;
    }

    @Override
    public boolean isWebSocketPlayer() {
        return true;
    }

    @Override
    public String getPlayerName() {
        return wsPlayer.getPlayerName();
    }

    @Override
    public void setLastScoreValue(int row, Object value) {
        lastScores[row] = value;
    }

    @Override
    public Object getLastScoreValue(int row) {
        return lastScores[row];
    }

    @Override
    public void run() {
        // WebSocket tabanlı istemciler için thread çalışmaz
    }

    @Override
    public void setCurrentDice(int[] dice) {
        this.currentDice = dice;
    }

    @Override
    public int[] getCurrentDice() {
        return currentDice;
    }

    @Override
    public GameRoom getGameRoom() {
        return wsPlayer.getGameRoom();
    }

    public void setAdapter(PlayerHandlerAdapter adapter) {
        this.adapter = adapter;
    }

    public PlayerHandlerAdapter getAdapter() {
        return adapter;
    }

    WebSocketPlayerHandler getWebSocketHandler() {
        return wsPlayer;
    }

}
