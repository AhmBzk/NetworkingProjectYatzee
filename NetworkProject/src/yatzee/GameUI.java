package yatzee;

import Client.bridgeCS;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GameUI extends Application {

    private Label[] diceLabels = new Label[5];
    private CheckBox[] diceChecks = new CheckBox[5];
    private ComboBox<String> scoreCategories;
    private TextArea messageArea;

    private bridgeCS connection = new bridgeCS();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Zar görselleri ve seçim kutuları
        HBox diceBox = new HBox(10);
        for (int i = 0; i < 5; i++) {
            VBox dieContainer = new VBox(5);
            diceLabels[i] = new Label("🎲 ?");
            diceChecks[i] = new CheckBox("Seç");
            dieContainer.getChildren().addAll(diceLabels[i], diceChecks[i]);
            diceBox.getChildren().add(dieContainer);
        }

        // Butonlar
        Button throwButton = new Button("Zar At");
        Button rerollButton = new Button("Tekrar At");

        throwButton.setOnAction(e -> {
            connection.send("THROW");
            log("Zarlar atılıyor...");
        });

        rerollButton.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("REROLL");
            for (int i = 0; i < 5; i++) {
                if (diceChecks[i].isSelected()) {
                    sb.append(" ").append(i);
                }
            }
            connection.send(sb.toString());
            log("Seçili zarlar tekrar atılıyor...");
        });
        
        // Puan kategorisi seçimi
        scoreCategories = new ComboBox<>();
        scoreCategories.getItems().addAll("ONES", "TWOS", "THREES", "FOURS", "FIVES", "SIXES",
                "THREE_KIND", "FOUR_KIND", "FULL_HOUSE", "SMALL_STR", "LARGE_STR", "CHANCE", "YAHTZEE");

        Button scoreButton = new Button("Kategori Seç");
        scoreButton.setOnAction(e -> {
            String selected = scoreCategories.getValue();
            if (selected != null) {
                connection.send("SCORE_SELECT " + selected);
                log("Kategori seçildi: " + selected);
            } else {
                log("Lütfen bir kategori seçin!");
            }
        });

        // Mesaj alanı
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(10);

        // Layout
        HBox buttons = new HBox(10, throwButton, rerollButton, scoreCategories, scoreButton);
        root.getChildren().addAll(diceBox, buttons, new Label("Mesajlar:"), messageArea);

        stage.setTitle("Yathzee Oyunu");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            connection.connect("localhost", 12345);
            log("Sunucuya bağlanıldı.");
            new Thread(() -> {
                try {
                    String line;
                    while ((line = connection.receive()) != null) {
                        processMessage(line);
                    }
                } catch (Exception e) {
                    log("Bağlantı kesildi.");
                }
            }).start();
        } catch (Exception e) {
            log("Sunucuya bağlanılamadı: " + e.getMessage());
        }
    }

    private void processMessage(String msg) {
        if (msg.startsWith("DICE ")) {
            String[] values = msg.substring(5).split(",");
            Platform.runLater(() -> {
                for (int i = 0; i < 5; i++) {
                    diceLabels[i].setText("🎲 " + values[i]);
                    diceChecks[i].setSelected(false);
                }
                log("Yeni zarlar geldi.");
            });
        } else if (msg.startsWith("SCORE_CONFIRMED ")) {
            Platform.runLater(() -> {
            log("Puanlama kabul edildi: " + msg.substring(16));});
        } else if (msg.startsWith("SCORE_REJECTED ")) {
            Platform.runLater(() -> {
            log("Bu kategori daha önce seçildi: " + msg.substring(15));});
        }
    }

    private void log(String msg) {
        messageArea.appendText(msg + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
