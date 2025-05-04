/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import yatzee.YatzeeGame;
import yatzee.ScoreTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Ahmet
 */
public class Client implements Runnable {

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private String id = "Player";
    private final YatzeeGame game;
    private final ScoreTable scoreTable;
    private final Set<String> usedScores = new HashSet<>();

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.game = new YatzeeGame(); // zarlar burada tutuluyor
        this.scoreTable = new ScoreTable(); // puan hesapları burada yapılır
    }

    public void send(String msj) {
        out.println(msj);
    }

    private String getDiceValues() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(YatzeeGame.getDice(i).getValue());
            if (i < 4) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            String input;
            while ((input = in.readLine()) != null) {
                if (input.equals("THROW")) {
                    YatzeeGame.throwAll();
                    out.println("DICE " + getDiceValues());
                } else if (input.startsWith("REROLL")) {
                    int[] indexes = Arrays.stream(input.split(" "))
                            .skip(1)
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    YatzeeGame.throwChoosen(indexes);
                    out.println("DICE " + getDiceValues());
                } else if (input.equals("SCORE_REQUEST")) {
                    int[] valueCounts = scoreTable.getAllPoints(YatzeeGame.countValues());
                } else if (input.startsWith("SCORE_SELECT")) {
                    String category = input.split(" ")[1];
                    if (!usedScores.contains(category)) {
                        usedScores.add(category);
                        out.println("SCORE_CONFIRMED " + category);
                    } else {
                        out.println("SCORE_REJECTED " + category);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }

}
