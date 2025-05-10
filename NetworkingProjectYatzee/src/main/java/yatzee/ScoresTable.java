/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yatzee;

/**
 *
 * @author Ahmet
 */
public class ScoresTable {
    public int[] getAllPoints(int[] dices) {
        int[] points = new int[15];
        Game scores = new Game();
        for (int dice : dices) {
            if (dice >= 1 && dice <= 6) {
                points[dice - 1] += dice;
            }
        }
        points[6] = 0;
        points[7] = 0;
        points[8] = scores.threeOfaKind(dices) ? scores.sumDices(dices) : 0;
        points[9] = scores.fourOfaKind(dices) ? scores.sumDices(dices) : 0;
        points[10] = scores.checkFullHouse(dices) ? 25 : 0;
        points[11] = scores.smallStr(dices) ? 30 : 0;
        points[12] = scores.largeStr(dices) ? 40 : 0;
        points[13] = scores.sumDices(dices);
        points[14] = scores.yathzee(dices) ? 50 : 0;
        return points;
    }
}
