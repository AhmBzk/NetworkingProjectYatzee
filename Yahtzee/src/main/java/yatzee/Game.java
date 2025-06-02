/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yatzee;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Ahmet
 */
public class Game {

    public int[] calculatePoints(int[] dices) {
        int[] points = new int[6];
        for (int dice : dices) {
            if (dice >= 1 && dice <= 6) {
                points[dice - 1] += dice;
            }
        }
        return points;
    }

    public int sumDices(int[] dices) {
        int sum = 0;
        for (int dice : dices) {
            sum += dice;
        }
        return sum;
    }

    public boolean threeOfaKind(int[] dices) {
        int[] freq = new int[7];
        for (int d : dices) freq[d]++;
        for (int count : freq) {
            if (count >= 3) return true;
        }
        return false;
    }

    public boolean fourOfaKind(int[] dices) {
        int[] freq = new int[7];
        for (int d : dices) freq[d]++;
        for (int count : freq) {
            if (count >= 4) return true;
        }
        return false;
    }

    public boolean yathzee(int[] dices) {
        int[] freq = new int[7];
        for (int d : dices) freq[d]++;
        for (int count : freq) {
            if (count == 5) return true;
        }
        return false;
    }

    public boolean checkFullHouse(int[] dices) {
        int[] freq = new int[7];
        for (int d : dices) freq[d]++;
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int count : freq) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }
        return hasThree && hasTwo;
    }

    public boolean smallStr(int[] dices) {
        Set<Integer> set = new TreeSet<>();
        for (int d : dices) set.add(d);

        int[][] validStraights = {
            {1, 2, 3, 4},
            {2, 3, 4, 5},
            {3, 4, 5, 6}
        };

        for (int[] pattern : validStraights) {
            boolean found = true;
            for (int num : pattern) {
                if (!set.contains(num)) {
                    found = false;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }

    public boolean largeStr(int[] dices) {
        Set<Integer> set = new TreeSet<>();
        for (int d : dices) set.add(d);
        return set.equals(Set.of(1, 2, 3, 4, 5)) || set.equals(Set.of(2, 3, 4, 5, 6));
    }
}
