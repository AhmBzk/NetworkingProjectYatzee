/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.networkingprojectyatzee;

/**
 *
 * @author Ahmet
 */
public class Game {
    
    public int[] calculatePoints(int[] dices) {
        int[] points = new int[6];
        for (int i = 0; i < dices.length; i++) {
            points[i] = dices[i] * (i + 1);
        }
        return points;
    }
    
    public int sumDices(int[] dices){
        int sum = 0;
        for(int dice : dices){
            sum=sum+dice;
        }
        return sum;
    }

    public boolean checkFullHouse(int[] dices) {
        boolean fullHouse = false;
        if (threeOfaKind(dices)) {
            for (int l = 0; l < dices.length; l++) {
                if (dices[l]/(l+1) == 2) {
                    fullHouse = true;
                }
            }
        }

        return fullHouse;
    }

    public boolean threeOfaKind(int[] dices) {
        boolean threeKind = false;
        for (int i = 0; i < dices.length; i++) {
            if (dices[i]/(i+1) == 3) {
                threeKind = true;
            }
        }
        return threeKind;
    }

    public boolean fourOfaKind(int[] dices) {
        boolean fourKind = false;
        for (int i = 0; i < dices.length; i++) {
            if (dices[i]/(i+1) == 4) {
                fourKind = true;
            }
        }
        return fourKind;
    }

    public boolean yathzee(int[] dices) {
        boolean yathzee = false;
        for (int i = 0; i < dices.length; i++) {
            if (dices[i]/(i+1) == 5) {
                yathzee = true;
            }
        }
        return yathzee;
    }

    public boolean smallStr(int[] dices) {
        int count = 0;
        int largeStr=0;
        for (int i = 0; i < dices.length; i++) {
            if (dices[i] > 0) {
                count++;
                largeStr=count;
            }else{
                if (count>=4) { return count>=4;}
                else {count = 0;}
            }
        }
        return largeStr >= 4;
    }

    public boolean largeStr(int[] dices) {
        int count = 0;
        int largeStr=0;
        for (int i = 0; i < dices.length; i++) {
            if (dices[i] > 0) {
                count++;
                largeStr=count;
            }else{
                count=0;
            }
        }
        return largeStr >= 5;
    }

}
