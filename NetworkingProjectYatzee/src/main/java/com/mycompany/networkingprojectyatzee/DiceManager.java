/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.networkingprojectyatzee;

import java.util.Random;

/**
 *
 * @author Ahmet
 */
public class DiceManager {

    private int value;
    private static final Random rand = new Random();

    public void dThrow() {
        value = rand.nextInt(6) + 1;
    }

    public int getValue() {
        return value;
    }
}
