/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
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

    public GUIGame(MessageClient client, String playerName, String opponentName) {
        this.client = client;
        this.playerName = playerName;
        this.opponentName = opponentName;

        initComponents();

        diceButtons = new JToggleButton[]{Dice1, Dice2, Dice3, Dice4, Dice5};

        setTitle("Yahtzee vs " + opponentName);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        setupRollButton();
        setupScoreTableClick();
        client.setGameUI(this);
    }

    private int getPlayerColumn() {
        return playerName.compareTo(opponentName) < 0 ? 1 : 2;
    }

    private void setupRollButton() {
        jButton1.addActionListener(e -> {
            if (!client.isMyTurn()) {
                JOptionPane.showMessageDialog(this, "It's not your turn!");
                return;
            }

            if (rerollCount >= 2) {
                JOptionPane.showMessageDialog(this, "You have used all re-roll chances.");
                return;
            }

            for (int i = 0; i < diceButtons.length; i++) {
                if (!diceButtons[i].isSelected()) {
                    diceValues[i] = (int) (Math.random() * 6) + 1;
                    diceButtons[i].setText(String.valueOf(diceValues[i]));
                }
            }

            rerollCount++;
            jButton1.setText("RE-ROLL (" + (3 - rerollCount) + ")");
            highlightPossibleScores();
        });
    }

    private void highlightPossibleScores() {
        int[] points = new ScoresTable().getAllPoints(diceValues);
        int playerColumn = playerName.compareTo(opponentName) < 0 ? 1 : 2;

        for (int row = 0; row < 13; row++) {
            Object value = jTable1.getValueAt(row, playerColumn);
            if (value == null && points[row] > 0) {
                jTable1.setValueAt(points[row], row, playerColumn);
            }
        }

        TableCellRenderer redRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object cellVal = jTable1.getValueAt(row, column);
                boolean isPermanent = cellVal != null && cellVal.toString().startsWith("✓");

                if (column == playerColumn && row < 13 && value != null && !isPermanent) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        jTable1.getColumnModel().getColumn(playerColumn).setCellRenderer(redRenderer);
    }

    private void setupScoreTableClick() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.rowAtPoint(evt.getPoint());
                int col = jTable1.columnAtPoint(evt.getPoint());
                int myCol = playerName.equals("Player 1") ? 1 : 2;

                if (col == myCol) {
                    lockScore(row);
                }
            }
        });
    }

    private void lockScore(int row) {
        int playerColumn = playerName.equals("Player 1") ? 1 : 2;
        Object val = jTable1.getValueAt(row, playerColumn);
        if (val == null) {
            return;
        }

        jTable1.setValueAt("✓ " + val, row, playerColumn);

        for (int r = 0; r < 13; r++) {
            if (r != row) {
                Object otherVal = jTable1.getValueAt(r, playerColumn);
                if (otherVal != null && !otherVal.toString().startsWith("✓")) {
                    jTable1.setValueAt(null, r, playerColumn);
                }
            }
        }

        rerollCount = 0;
        jButton1.setText("ROLL");
        for (JToggleButton btn : diceButtons) {
            btn.setSelected(false);
            btn.setText("Dice");
        }

        if (isGameOver()) {
            int total = calculateTotalScore();
            jTable1.setValueAt("✓ " + total, 14, playerColumn);
            client.send("GAME_OVER " + total);
            jButton1.setEnabled(false);
            jTable1.setEnabled(false);
        } else {
            client.send("SCORE_LOCKED " + row);
        }
    }

    private boolean isGameOver() {
        int playerColumn = playerName.equals("Player 1") ? 1 : 2;
        int filled = 0;
        for (int row = 0; row < 13; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null && val.toString().startsWith("✓")) {
                filled++;
            }
        }
        return filled == 13;
    }

    private int calculateTotalScore() {
        int playerColumn = playerName.equals("Player 1") ? 1 : 2;
        int total = 0;
        for (int row = 0; row < 13; row++) {
            Object val = jTable1.getValueAt(row, playerColumn);
            if (val != null && val.toString().startsWith("✓")) {
                try {
                    total += Integer.parseInt(val.toString().replace("✓", "").trim());
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
                "Title 1", "Player 1", "Player 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 0, 250, 620));

        Dice1.setText("Dice 1");
        getContentPane().add(Dice1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 440, 70, -1));

        Dice2.setText("Dice 2");
        getContentPane().add(Dice2, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 440, 70, -1));

        Dice3.setText("Dice 3");
        getContentPane().add(Dice3, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 440, 70, -1));

        Dice4.setText("Dice 4");
        getContentPane().add(Dice4, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 440, 70, -1));

        Dice5.setText("Dice 5");
        getContentPane().add(Dice5, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 440, 70, -1));

        jButton1.setText("ROLL");
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 500, -1, -1));
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 620));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
