/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import server.MessageClient;
import yatzee.ScoresTable;

/**
 *
 * @author Ahmet
 */
public class GUIGame extends javax.swing.JFrame {

    /**
     * Creates new form GUIGame
     */
    private final MessageClient client;
    private final String playerName;
    private final String opponentName;
    private int[] diceValues = new int[5];
    private int rerollCount = 0;
    private final JToggleButton[] diceButtons;
    Set<Integer> tempRows = new HashSet<>();
    private final Set<Integer> lockedRows = new HashSet<>();
    ImageIcon icon = new ImageIcon(getClass().getResource("/dice1.png"));
    Image scaledImage = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);

    public GUIGame(MessageClient client, String playerName, String opponentName) {
        this.client = client;
        this.playerName = playerName;
        this.opponentName = opponentName;

        initComponents();
        setTableColumnHeaders();
        diceButtons = new JToggleButton[]{Dice1, Dice2, Dice3, Dice4, Dice5};
        setTitle("Yahtzee vs " + opponentName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        setupScoreTableClick();
        client.setGameUI(this);
        setupDiceToggleMovement();
        repositionDiceButtons();
        setTurnText();
        setImage();
    }

    public void setImage() {
        URL imageUrl = getClass().getResource("/table.jpg");
        if (imageUrl == null) {
            System.err.println("Cannot find /table.jpg");
            return;
        }

        ImageIcon background = new ImageIcon(imageUrl);
        jLabel1.setIcon(background);
    }

    private void setTurnText() {
        javax.swing.Timer timer = new javax.swing.Timer(100, event -> {
            if (client.isMyTurn()) {
                jLabel2.setText("YOUR TURN");
            } else {
                jLabel2.setText("WAIT FOR YOUR TURN");
            }
        });
        timer.setRepeats(false);
        timer.start();

    }

    private void repositionDiceButtons() {
        for (JToggleButton btn : diceButtons) {

            btn.setLocation(btn.getX(), 350);
        }
    }

    private int getPlayerColumn() {
        return 1;
    }

    private void setupRollButton() {

        if (!client.isMyTurn()) {
            JOptionPane.showMessageDialog(this, "It's not your turn!");
            return;
        }

        if (rerollCount >= 3) {
            JOptionPane.showMessageDialog(this, "You have used all re-roll chances.");
            //repositionDiceButtons();
            return;
        }

        for (int i = 0; i < diceButtons.length; i++) {
            if (!diceButtons[i].isSelected() || diceValues[i] == 0) {
                diceValues[i] = (int) (Math.random() * 6) + 1;
            }
            //diceButtons[i].setText(String.valueOf(diceValues[i])); 
        }
        animateDice(diceValues);
        rerollCount++;
        jButton1.setText("RE-ROLL (" + (3 - rerollCount) + ")");
        highlightPossibleScores();

    }

    private void setupDiceToggleMovement() {
        for (JToggleButton btn : diceButtons) {
            if (btn.isSelected()) {
                btn.setLocation(btn.getX(), 440); // if toggled then move down the dice
            } else {
                int randomOffset = (int) (Math.random() * 100); // between 0–100
                btn.setLocation(btn.getX(), 200 + randomOffset); // move up the buttons
            }
        }
    }

    private void moveDice(JToggleButton btn) {
        if (btn.isSelected()) {
            btn.setLocation(btn.getX(), 440); // if toggled then move down the dice
        } else {
            // between 0–100
            btn.setLocation(btn.getX(), 350); // move up the buttons
        }
    }

    public ImageIcon getScaledIcon(String path, int width, int height) {
        javax.swing.Timer timer = new javax.swing.Timer(10, event -> {
        });
        timer.setRepeats(false);
        timer.start();
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("Image not found! " + path);
            return new ImageIcon();
        }

        ImageIcon icon = new ImageIcon(url);

        Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return new ImageIcon(scaledImage);

    }

    public void animateDice(int[] finalValues) {
        for (int i = 0; i < diceButtons.length; i++) {
            final int diceIndex = i;

            if (!diceButtons[diceIndex].isSelected()) {
                new Thread(() -> {
                    try {
                        for (int frame = 1; frame <= 6; frame++) {
                            int frameFinal = frame;
                            SwingUtilities.invokeLater(() -> {
                                diceButtons[diceIndex].setIcon(getScaledIcon("/dice" + frameFinal + ".png", 32, 32));
                            });
                            Thread.sleep(100);
                        }

                        int finalValue = finalValues[diceIndex];
                        SwingUtilities.invokeLater(() -> {
                            diceButtons[diceIndex].setIcon(getScaledIcon("/dice" + finalValue + ".png", 32, 32));
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    public void sendPlayerScoresToServer() {
        int playerColumn = getPlayerColumn();
        for (int row = 0; row < 16; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null) {
                String valueStr = val.toString().trim();
                if (!valueStr.equals("0")) { // "0" varsa bile istersen gönderebilirsin, bu satır opsiyonel
                    client.send("SCORE_LOCKED " + row + " " + playerColumn + " " + valueStr);
                    
                }
            }
        }
    }

    public void lockScoreFromServer(int row, String value) {
        jTable1.setValueAt(Integer.parseInt(value), row, 2); // since the opponent column always the 2nd one
        jTable1.repaint();

        javax.swing.Timer timer = new javax.swing.Timer(50, event -> {
            repositionDiceButtons();
        });
        timer.setRepeats(false);
        timer.start();
        jLabel2.setText("YOUR TURN!");
        for (JToggleButton btn : diceButtons) {
            btn.setSelected(false);
            //btn.setText("Dice");
        }
    }

    private void highlightPossibleScores() {
        int playerColumn = getPlayerColumn();

        for (int r : tempRows) {
            jTable1.setValueAt(null, r, playerColumn);
        }
        tempRows.clear();

        int[] points = new ScoresTable().getAllPoints(diceValues);
        boolean atLeastOneSelectable = false;

        for (int row = 0; row <= 14; row++) {
            Object value = jTable1.getValueAt(row, playerColumn);
            if (value == null && points[row] > 0) {
                jTable1.setValueAt(points[row], row, playerColumn);
                tempRows.add(row);
                atLeastOneSelectable = true;
            }
        }

        if (!atLeastOneSelectable) {
            for (int row = 0; row <= 14; row++) {
                if (jTable1.getValueAt(row, playerColumn) == null) {
                    jTable1.setValueAt(0, row, playerColumn);
                    tempRows.add(row);
                }
            }
        }

        TableCellRenderer redRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == playerColumn && tempRows.contains(row)) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        jTable1.getColumnModel().getColumn(playerColumn).setCellRenderer(redRenderer);
        jTable1.repaint();
    }

    private void setupScoreTableClick() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.rowAtPoint(evt.getPoint());
                int col = jTable1.columnAtPoint(evt.getPoint());
                int myCol = getPlayerColumn();

                if (col == myCol && row != 6 && row != 7) { // 6: Bonus, 7: Sum
                    lockScore(row);
                }
            }
        });
    }

    private boolean isYahtzee(int[] dice) {
        int first = dice[0];
        for (int i = 1; i < dice.length; i++) {
            if (dice[i] != first) {
                return false;
            }
        }
        return true;
    }

    private void lockScore(int row) {
        int playerColumn = getPlayerColumn();

        if (!tempRows.contains(row)) {
            return;
        }

        Object val = jTable1.getValueAt(row, playerColumn);
        if (val == null) {
            return;
        }

        if (isYahtzee(diceValues)) {
            Object yahtzeeCell = jTable1.getValueAt(14, playerColumn); // 14: Yahtzee satırı
            if (yahtzeeCell != null && row != 14) {
                try {
                    int yahtzeeScore = Integer.parseInt(yahtzeeCell.toString().trim());
                    if (yahtzeeScore >= 50) {
                        int newYahtzeeScore = yahtzeeScore + 100;
                        jTable1.setValueAt(newYahtzeeScore, 14, playerColumn);
                        client.send("SCORE_LOCKED 14 " + playerColumn + " " + newYahtzeeScore);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        jTable1.setValueAt(val, row, playerColumn);

        for (int r : tempRows) {
            if (r != row) {
                jTable1.setValueAt(null, r, playerColumn);
            }
        }

        client.setLastScoreValue(row, val);
        rerollCount = 0;
        jButton1.setText("ROLL");

        for (JToggleButton btn : diceButtons) {
            btn.setSelected(false);
            //btn.setText("Dice");
        }

        sendPlayerScoresToServer();

        if (isGameOver()) {
            int total = calculateTotalScore();
            jTable1.setValueAt(total, 15, playerColumn);
            client.send("GAME_OVER " + total);
            jButton1.setEnabled(false);
            jTable1.setEnabled(false);
        }
        jTable1.clearSelection();
        tempRows.clear();
        jTable1.repaint();
        updateSumAndBonus();
        setTurnText();
    }

    private void updateSumAndBonus() {
        int playerColumn = getPlayerColumn();

        int upperTotal = 0;
        for (int row = 0; row <= 5; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null) {
                try {
                    upperTotal += Integer.parseInt(val.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int bonus = upperTotal >= 63 ? 35 : 0;
        int total = upperTotal + bonus;

        for (int row = 8; row <= 14; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null) {
                try {
                    total += Integer.parseInt(val.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Refresh table
        jTable1.setValueAt(bonus, 6, playerColumn);
        jTable1.setValueAt(upperTotal, 7, playerColumn);
        jTable1.setValueAt(total, 15, playerColumn);

        // remind the opponent
        client.send("SCORE_LOCKED 6 " + 2 + " " + bonus);
        client.send("SCORE_LOCKED 7 " + 2 + " " + upperTotal);
        client.send("SCORE_LOCKED 15 " + 2 + " " + total);
    }

    private boolean isGameOver() {
        int playerColumn = getPlayerColumn();
        for (int row = 0; row <= 14; row++) {
            if (jTable1.getValueAt(row, playerColumn) == null) {
                return false;
            }
        }
        return true;
    }

    private int calculateTotalScore() {
        int playerColumn = getPlayerColumn();
        int total = 0;
        for (int row = 0; row < 15; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null) {  // only locked points
                try {
                    total += Integer.parseInt(val.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return total;
    }

    public void showResultPopup(String result, String score) {
        SwingUtilities.invokeLater(() -> {
            String message = switch (result) {
                case "WIN" ->
                    "You won! Your score: " + score;
                case "LOSE" ->
                    "You lost! Opponent's score: " + score;
                default ->
                    "It's a draw! Score: " + score;
            };

            int option = JOptionPane.showConfirmDialog(
                    this,
                    message + "\nDo you want to play again?",
                    "Game Over",
                    JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                client.send("RESTART");
                dispose();
                new WaitingRoom();
            } else {
                System.exit(0);
            }
        });
    }

    public void updateOpponentDice(int[] opponentDice) {
        if (!client.isMyTurn()) {
            for (int i = 0; i < 5; i++) {
                //diceButtons[i].setText(String.valueOf(opponentDice[i]));
                diceButtons[i].setSelected(false); // 
            }
            javax.swing.Timer timer = new javax.swing.Timer(50, event -> {
                setupDiceToggleMovement();
            });
            timer.setRepeats(false);
            timer.start();
            animateDice(opponentDice);
        }
    }

    public void highlightSuggestedScore(int row, String value) {
        int col = 1; // kendi kolonu
        if (jTable1.getValueAt(row, col) == null) {
            jTable1.setValueAt(Integer.parseInt(value), row, col);
            tempRows.add(row);
            TableCellRenderer redRenderer = new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (column == col && tempRows.contains(row)) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                    return c;
                }
            };
            jTable1.getColumnModel().getColumn(col).setCellRenderer(redRenderer);
            jTable1.repaint();
        }
    }

    private void setTableColumnHeaders() {
        jTable1.getColumnModel().getColumn(1).setHeaderValue(playerName);
        jTable1.getColumnModel().getColumn(2).setHeaderValue(opponentName);
        jTable1.getTableHeader().repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        Dice1 = new javax.swing.JToggleButton();
        Dice2 = new javax.swing.JToggleButton();
        Dice3 = new javax.swing.JToggleButton();
        Dice4 = new javax.swing.JToggleButton();
        Dice5 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Ones", null, null},
                {"Twos", null, null},
                {"Threes", null, null},
                {"Fours", null, null},
                {"Fives", null, null},
                {"Sixes", null, null},
                {"Bonus", null, null},
                {"Sum", null, null},
                {"Three of a kind", null, null},
                {"Four of a kind", null, null},
                {"Full house", null, null},
                {"Small straight", null, null},
                {"Large straight", null, null},
                {"Chance", null, null},
                {"Yathzee", null, null},
                {"Total", null, null}
            },
            new String [] {
                "Yahtzee", "Player 1", "Player 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 50, 250, 300));

        Dice1.setIcon(new ImageIcon(scaledImage));
        Dice1.setContentAreaFilled(false);
        Dice1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice1ActionPerformed(evt);
            }
        });
        getContentPane().add(Dice1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 350, 32, 32));

        Dice2.setIcon(new ImageIcon(scaledImage));
        Dice2.setContentAreaFilled(false);
        Dice2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice2ActionPerformed(evt);
            }
        });
        getContentPane().add(Dice2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 350, 32, 32));

        Dice3.setIcon(new ImageIcon(scaledImage));
        Dice3.setContentAreaFilled(false);
        Dice3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice3ActionPerformed(evt);
            }
        });
        getContentPane().add(Dice3, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 350, 32, 32));

        Dice4.setIcon(new ImageIcon(scaledImage));
        Dice4.setContentAreaFilled(false);
        Dice4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice4ActionPerformed(evt);
            }
        });
        getContentPane().add(Dice4, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 350, 32, 32));

        Dice5.setIcon(new ImageIcon(scaledImage));
        Dice5.setBorderPainted(false);
        Dice5.setContentAreaFilled(false);
        Dice5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Dice5ActionPerformed(evt);
            }
        });
        getContentPane().add(Dice5, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 350, 32, 32));

        jButton1.setText("ROLL");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 410, -1, -1));

        jLabel2.setText("YOUR TURN!");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 10, 250, 40));
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 740, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked

    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setupRollButton();
        if (client.isMyTurn() && rerollCount <= 3) {

            javax.swing.Timer timer = new javax.swing.Timer(50, event -> {
                setupDiceToggleMovement();
            });
            timer.setRepeats(false);
            timer.start();

            StringBuilder sb = new StringBuilder();
            for (int value : diceValues) {
                sb.append(value);
            }
            client.send("ROLL_DICE " + sb.toString());

        }
        jTable1.clearSelection();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void Dice1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice1ActionPerformed
        moveDice(Dice1);
    }//GEN-LAST:event_Dice1ActionPerformed

    private void Dice2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice2ActionPerformed
        moveDice(Dice2);
    }//GEN-LAST:event_Dice2ActionPerformed

    private void Dice3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice3ActionPerformed
        moveDice(Dice3);
    }//GEN-LAST:event_Dice3ActionPerformed

    private void Dice4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice4ActionPerformed
        moveDice(Dice4);
    }//GEN-LAST:event_Dice4ActionPerformed

    private void Dice5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Dice5ActionPerformed
        moveDice(Dice5);
    }//GEN-LAST:event_Dice5ActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton Dice1;
    private javax.swing.JToggleButton Dice2;
    private javax.swing.JToggleButton Dice3;
    private javax.swing.JToggleButton Dice4;
    private javax.swing.JToggleButton Dice5;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
