/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package yatzee;

/**
 *
 * @author Ahmet
 */
public class Test {

    public static void main(String[] args) {
        int[] valuesCount;
        ScoreTable sTable = new ScoreTable();
        YatzeeGame game = new YatzeeGame();
        YatzeeGame.throwAll();
        YatzeeGame.print();
        valuesCount = sTable.calculatePoints(YatzeeGame.countValues());

        for (int i = 0; i < 6; i++) {
            System.out.println(valuesCount[i]);
        }
        System.out.println("Fullhouse : " + sTable.checkFullHouse(valuesCount));
        System.out.println("Four of a Kind : " + sTable.fourOfaKind(valuesCount));
        System.out.println("Large Straight : " + sTable.largeStr(valuesCount));
        System.out.println("Small Straight : " + sTable.smallStr(valuesCount));
        System.out.println("Three of a Kind : " + sTable.threeOfaKind(valuesCount));
        System.out.println("Yathzee : " + sTable.yathzee(valuesCount));
        int counter = 0;
//        for (int j=0; j<20;j++) {
        while(!sTable.yathzee(valuesCount)){
            int[] choosen = {0, 1, 2, 3, 4};
            System.out.println("ReRolling ");
            YatzeeGame.throwChoosen(choosen);
            YatzeeGame.print();
            valuesCount = sTable.calculatePoints(YatzeeGame.countValues());
            System.out.println("Fullhouse       : " + sTable.checkFullHouse(valuesCount));
            System.out.println("Four of a Kind  : " + sTable.fourOfaKind(valuesCount));
            System.out.println("Large Straight  : " + sTable.largeStr(valuesCount));
            System.out.println("Small Straight  : " + sTable.smallStr(valuesCount));
            System.out.println("Three of a Kind : " + sTable.threeOfaKind(valuesCount));
            System.out.println("Yathzee         : " + sTable.yathzee(valuesCount));
            sTable.getAllPoints(valuesCount);
            counter++;

        }
        System.out.println("The dice thrown total of " +counter+ " times ");

        System.out.println("Total Points : " + sTable.sumDices(valuesCount));

    }

}
