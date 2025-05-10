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
    Game scores = new Game();    
    public int[] getAllPoints(int[] dices){
        int[] points = new int[13];
        points[0] = dices[0];
        points[1] = dices[1];
        points[2] = dices[2];
        points[3] = dices[3];
        points[4] = dices[4];
        points[5] = dices[5];
        int threeKind=0 ; if(scores.threeOfaKind(dices)){threeKind = scores.sumDices(dices);}
        points[6] = threeKind;
        int fourKind = 0; if(scores.fourOfaKind(dices)){fourKind=scores.sumDices(dices);}
        points[7] = fourKind;
        int fullHouse = 0; if(scores.checkFullHouse(dices)){fullHouse=  25;}
        points[8] =  fullHouse;
        int smallStr = 0; if(scores.smallStr(dices)){smallStr = 30;}
        points[9] = smallStr;
        int largeStr = 0; if(scores.largeStr(dices)){largeStr = 40;}
        points[10] = largeStr;
        int chance = scores.sumDices(dices);
        points[11] = chance;
        int yathzee = 0; if(scores.yathzee(dices)){yathzee = 50;}
        points[12] = yathzee; 
        
        for(int point:points){
            System.out.println(point+" points");
        }
        
        return points;
    }
}
