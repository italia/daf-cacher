package com.github.italia.daf.metabase;

import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.utils.LoggerFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MetabaseSniperPageImpl implements Page {

    private Properties properties;
    private static final Logger LOGGER = LoggerFactory.getLogger(MetabaseSniperPageImpl.class.getName());

    public MetabaseSniperPageImpl(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public void visit(WebDriver driver, String url) {

        final int implicitWait = Integer.parseInt(properties.getProperty("caching.page_load_wait"));
        driver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
        driver.get(url);
        LOGGER.fine("Wait for JS complete");
        (new WebDriverWait(driver, implicitWait)).until((ExpectedCondition<Boolean>) d -> {
            final String status = (String) ((JavascriptExecutor) d).executeScript("return document.readyState");
            return status.equals("complete");
        });
        LOGGER.fine("JS complete");


        LOGGER.fine("Wait for footer");
        (new WebDriverWait(driver, implicitWait))
                .until(
                        ExpectedConditions.visibilityOf(
                                driver.findElement(By.name("downarrow"))));
        LOGGER.info("Footer is there");
        try {
            Thread.sleep(implicitWait * 1000l);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public byte[] saveAsImage(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

}
