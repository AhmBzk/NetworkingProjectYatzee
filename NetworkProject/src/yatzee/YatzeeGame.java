/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package yatzee;

/**
 *
 * @author Ahmet
 */
public class YatzeeGame {

    private static final int dice_count = 5;
    private static Dice[] dices;

    public YatzeeGame() {
        dices = new Dice[dice_count];
        for (int i = 0; i < dice_count; i++) {
            dices[i] = new Dice();
        }
    }

    public static Dice getDice(int index) {
        return dices[index];
    }

    public static void throwAll() {
        for (Dice dice : dices) {
            dice.dThrow();
        }
    }

    public static void throwChoosen(int[] secilenIndexler) {
        for (int index : secilenIndexler) {
            if (index >= 0 && index < dice_count) {
                dices[index].dThrow();
            }
        }
    }

    public static void print() {
        for (int i = 0; i < dice_count; i++) {
            System.out.println((i + 1) + ". dice: " + dices[i].getValue());
        }
    }

    public static int[] countValues() {
        int[] value_count = new int[6];
        for (Dice dice : dices) {
            int value = dice.getValue();
            value_count[value - 1]++;
        }
//        System.out.println("Repeats : ");
//        for (int i = 0; i < value_count.length; i++) {
//            System.out.println((i + 1) + ": " + value_count[i] + " times");
//        }
        return value_count;
    }
}
