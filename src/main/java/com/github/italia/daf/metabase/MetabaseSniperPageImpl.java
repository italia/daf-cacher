package com.github.italia.daf.metabase;

import com.github.italia.daf.sniper.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MetabaseSniperPageImpl implements Page {
    @Override
    public void visit(WebDriver driver, String url) {
        driver.get(url);
        (new WebDriverWait(driver, 10)).until((ExpectedCondition<Boolean>) d -> {
            assert d != null;
            return d.findElement(By.name("downarrow")).isDisplayed();
        });
    }

    @Override
    public byte[] saveAsImage(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

}
