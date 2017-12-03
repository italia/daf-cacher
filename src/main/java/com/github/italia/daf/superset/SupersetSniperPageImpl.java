package com.github.italia.daf.superset;

import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.utils.Credential;
import org.openqa.selenium.*;

import java.net.URL;

public class SupersetSniperPageImpl implements Page {

    private boolean loggedIn = false;
    private String supersetLoginUrl;
    private Credential credential;
    private int implicitWait;

    private SupersetSniperPageImpl() {
        this.implicitWait = 10;
    }

    @Override
    public void visit(WebDriver webDriver, String url) {


        if (!loggedIn)
            login(webDriver);

        final Dimension dimension = webDriver.manage().window().getSize();

        final String patchedUrl = url.concat("&width=" + (dimension.getWidth() - 300) + "&height=" + (dimension.getHeight() - 300));
        webDriver.navigate().to(patchedUrl);

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

    private void login(final WebDriver webDriver) {

        webDriver.navigate().to(supersetLoginUrl);
        webDriver.findElement(By.id("username")).sendKeys(credential.getUsername());
        webDriver.findElement(By.id("password")).sendKeys(credential.getPassword());
        webDriver.findElement(By.id("password")).submit();

        webDriver.findElement(By.id("dash_table"));
        loggedIn = true;

    }

    public void reset() {
        this.loggedIn = false;
    }

    public static class Builder {

        private SupersetSniperPageImpl sniperPage;

        public Builder() {
            sniperPage = new SupersetSniperPageImpl();
        }

        public Builder setSupersetLoginUrl(URL url) {
            sniperPage.supersetLoginUrl = url.toString();
            return this;
        }

        public Builder setCredential(Credential credential) {
            sniperPage.credential = credential;
            return this;
        }

        public Builder implicitWait(int timeout) {
            sniperPage.implicitWait = timeout;
            return this;
        }

        public SupersetSniperPageImpl getSniperPage() {
            return sniperPage;
        }
    }

}
