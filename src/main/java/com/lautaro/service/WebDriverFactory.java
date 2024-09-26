package com.lautaro.service;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
public class WebDriverFactory {

    @Value("${webdriver.timeout:30}")  // 30 es el valor por defecto si no se encuentra la propiedad
    private int timeout;

    public WebDriver createWebDriver() {
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        return driver;
    }
}
