package com.lautaro.service;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class ImageScraper {

    public List<String> scrapeImages(WebDriver driver) {
        List<String> imageUrls = new ArrayList<>();

        // Wait for the page to load completely
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        // Wait for the product image container to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".vtex-store-components-3-x-productImagesContainer")));

        // Scroll to load lazy images
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

        // Find the main product image
        try {
            WebElement mainImage = driver.findElement(By.cssSelector(".vtex-store-components-3-x-productImageTag--main[data-vtex-preload='true']"));
            String mainSrc = mainImage.getAttribute("src");
            if (mainSrc != null && !mainSrc.isEmpty() && !mainSrc.contains("data:image")) {
                imageUrls.add(mainSrc);
                System.out.println("Found main image URL: " + mainSrc);
            }
        } catch (Exception e) {
            System.out.println("Main image not found: " + e.getMessage());
        }

        // Find all thumbnail images
        List<WebElement> thumbnailImages = driver.findElements(By.cssSelector(".vtex-store-components-3-x-thumbImg"));
        for (WebElement img : thumbnailImages) {
            String src = img.getAttribute("src");
            if (src != null && !src.isEmpty() && !src.contains("data:image")) {
                imageUrls.add(src);
                System.out.println("Found thumbnail image URL: " + src);
            }
        }

        // Remove duplicates
        imageUrls = new ArrayList<>(new LinkedHashSet<>(imageUrls));

        return imageUrls;
    }
}