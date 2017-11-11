package com.github.italia.daf.metabase;

import com.github.italia.daf.sniper.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MetabaseSniperPageImpl implements Page {
    @Override
    public void visit(WebDriver driver, String url) {
        driver.get(url);
        (new WebDriverWait(driver, 20))
                .until(
                        ExpectedConditions.invisibilityOf(
                                driver.findElement(By.className("LoadingSpinner"))));
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
