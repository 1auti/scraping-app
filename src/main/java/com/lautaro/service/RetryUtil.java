package com.lautaro.service;

import java.util.concurrent.Callable;

public class RetryUtil {
    public static <T> T retry(Callable<T> task, int maxAttempts, long delayMs) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                return task.call();
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("Max retry attempts reached", e);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        throw new RuntimeException("Unexpected error in retry logic");
    }
}
