package com.github.italia.daf.metabase;

import com.github.italia.daf.sniper.Page;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class MetabaseSniperPageImpl implements Page {
    @Override
    public void visit(WebDriver driver, String url) {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(url);
        (new WebDriverWait(driver, 20)).until((ExpectedCondition<Boolean>) d -> {
            final String status = (String) ((JavascriptExecutor) d).executeScript("return document.readyState");
            return status.equals("complete");
        });

        try {
            final WebElement spinner = driver.findElement(By.className("LoadingSpinner"));
            if (spinner.isDisplayed()) {

                (new WebDriverWait(driver, 20))
                        .until(
                                ExpectedConditions.invisibilityOf(spinner));

            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            // ignoring
        }

        (new WebDriverWait(driver, 20))
                .until(
                        ExpectedConditions.visibilityOf(
                                driver.findElement(By.name("downarrow"))));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public byte[] saveAsImage(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

}
