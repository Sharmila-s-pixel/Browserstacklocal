package org.example.browserstack;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BSAssignment extends SeleniumTest {

    @Test
    public void testElPaisOpinionSection() throws Exception {
        driver.get("https://elpais.com/");
        driver.manage().window().maximize();

        try {
            WebElement acceptCookies = driver.findElement(By.id("didomi-notice-agree-button"));
            if (acceptCookies.isDisplayed()) {
                acceptCookies.click();
            }
        } catch (Exception e) {
            System.out.println("No cookies popup found or already handled.");
        }

        String title = driver.getTitle();
        System.out.println("Page Title: " + title);
        Assert.assertTrue(title.matches(".*[áéíóúñ].*"), "Title is not in Spanish: " + title);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));


        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[@cmp-ltrk='portada_menu' and text()='Opinión']")));
        WebElement opinion = driver.findElement(By.xpath("//a[@cmp-ltrk='portada_menu' and text()='Opinión']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", opinion);
        wait.until(ExpectedConditions.elementToBeClickable(opinion));
        Thread.sleep(1000);
        try {
            opinion.click();
        } catch (Exception e) {
            System.out.println("Regular click failed, trying JavaScript click...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opinion);
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> articles = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@class, 'b-d_d')]//article"))
        );

        int limit = Math.min(articles.size(), 5);
        List<String> translatedHeaders = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            try {
                String titleText = articles.get(i).findElement(By.xpath(".//header[contains(@class,'c_h')]//h2")).getText();
                System.out.println("Original Title (Article " + (i + 1) + "): " + titleText);

                String translatedTitle = TranslationService.translateToEnglish(titleText);

//                String translatedTitle = translateToEnglish(titleText);
                translatedHeaders.add(translatedTitle);
                System.out.println("Translated Title (Article " + (i + 1) + "): " + translatedTitle);

                List<WebElement> imgElements = articles.get(i).findElements(By.xpath(".//figure/a/img"));
                if (!imgElements.isEmpty()) {
                    String imgUrl = imgElements.get(0).getAttribute("src");
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        downloadImage(imgUrl, "Article_" + (i + 1) + ".jpg");
                        System.out.println("Downloaded: Article_" + (i + 1) + ".jpg");
                    } else {
                        System.out.println("Image URL is empty for Article " + (i + 1));
                    }
                } else {
                    System.out.println("No image found for Article " + (i + 1));
                }
            } catch (Exception e) {
                System.out.println("Error processing Article " + (i + 1) + ": " + e.getMessage());
            }
        }

        analyzeRepeatedWords(translatedHeaders);
    }



    // Helper method to download image
    public static void downloadImage(String imageUrl, String fileName) {
        String directoryPath = "/Users/sharmilas/Downloads/testng-browserstack-master/src/test/resources/Article_images/";
        Path filePath = Paths.get(directoryPath + fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, filePath);
            }
        } catch (Exception e) {
            System.out.println("Failed to download " + fileName + ": " + e.getMessage());
        }
    }

     //Helper method to analyze repeated words
    public static void analyzeRepeatedWords(List<String> translatedHeaders) {
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String header : translatedHeaders) {
            String[] words = header.split("\\s+");
            for (String word : words) {
                word = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
                if (!word.isEmpty()) {
                    wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                }
            }
        }
        System.out.println("Repeated Words (Appeared more than twice):");
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() > 2) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " occurrences");
            }
        }
    }
} 