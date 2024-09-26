package com.lautaro.service;

import java.util.Random;

//Funcion Implementar retrasos y aleatoriedad
public class ScrapingUtil {
    private static final Random random = new Random();

    public static void randomDelay() {
        try {
            // Retraso aleatorio entre 3 y 7 segundos
            Thread.sleep(3000 + random.nextInt(4000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}