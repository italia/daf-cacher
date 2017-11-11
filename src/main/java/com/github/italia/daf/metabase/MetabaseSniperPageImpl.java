package com.github.italia.daf.metabase;

import com.github.italia.daf.sniper.Page;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MetabaseSniperPageImpl implements Page {
    @Override
    public void visit(WebDriver driver, String url) {
        driver.get(url);
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
    }

    @Override
    public byte[] saveAsImage(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

}
