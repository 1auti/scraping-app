package com.lautaro.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;

public class WebDriverConfig {
    private static final List<String> USER_AGENTS = new ArrayList<>(List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15"
            // Añade más user agents según sea necesario
    ));

    private static final List<String> PROXIES = new ArrayList<>(List.of(
            "ip1:port1",
            "ip2:port2",
            "ip3:port3"
            // Añade tus proxies aquí
    ));

    private static final Random RANDOM = new Random();

    public static WebDriver configurarWebDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Lautaro\\IdeaProjects\\scraping-app\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\Lautaro\\IdeaProjects\\scraping-app\\GoogleChromePortable64\\App\\Chrome-bin\\chrome.exe");

        // Configuración para evitar detección
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-gpu");

        // Rotación de User Agent
        String randomUserAgent = USER_AGENTS.get(RANDOM.nextInt(USER_AGENTS.size()));
        options.addArguments("user-agent=" + randomUserAgent);

        // Configuración de proxy (si se usa)
        if (!PROXIES.isEmpty()) {
            String randomProxy = PROXIES.get(RANDOM.nextInt(PROXIES.size()));
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(randomProxy);
            proxy.setSslProxy(randomProxy);
            options.setProxy(proxy);
        }

        // Ocultar la automatización
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        // Configurar tiempo de espera implícito
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Ocultar el webdriver
        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        return driver;
    }

    public static void esperaAleatoria(int minSegundos, int maxSegundos) {
        try {
            Thread.sleep(RANDOM.nextInt(maxSegundos - minSegundos + 1) * 1000L + minSegundos * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}