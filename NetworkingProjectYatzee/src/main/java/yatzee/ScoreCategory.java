/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yatzee;

/**
 *
 * @author Ahmet
 */
public enum ScoreCategory {
    ONES(0, "Ones"),
    TWOS(1, "Twos"),
    THREES(2, "Threes"),
    FOURS(3, "Fours"),
    FIVES(4, "Fives"),
    SIXES(5, "Sixes"),
    THREE_OF_A_KIND(6, "Three of a Kind"),
    FOUR_OF_A_KIND(7, "Four of a Kind"),
    FULL_HOUSE(8, "Full House"),
    SMALL_STRAIGHT(9, "Small Straight"),
    LARGE_STRAIGHT(10, "Large Straight"),
    CHANCE(11, "Chance"),
    YAHTZEE(12, "Yahtzee");

    private final int index;
    private final String displayName;

    ScoreCategory(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ScoreCategory fromIndex(int i) {
        for (ScoreCategory cat : values()) {
            if (cat.getIndex() == i) return cat;
        }
        throw new IllegalArgumentException("Invalid index: " + i);
    }
}
