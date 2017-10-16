package com.github.italia.daf.superset;

import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.util.Credential;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.AShot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class SupersetSniperPageImpl implements Page {

    private boolean loggedIn = false;
    private String supersetLoginUrl;
    private Credential credential;

    private SupersetSniperPageImpl() {
    }

    @Override
    public void visit(WebDriver webDriver, String url) {

        if (!loggedIn)
            login(webDriver);

        webDriver.navigate().to("about:blank");
        JavascriptExecutor js = (JavascriptExecutor) webDriver;

        js.executeScript(
                "var j = document.createElement('script');" +
                        "j.src = 'https://code.jquery.com/jquery-latest.min.js';" +
                        "j.onload = function() {  };" +
                        "document.body.appendChild(j);" +
                        "var iframe = document.createElement('iframe');" +
                        "iframe.src = '" + url + "';" +
                        "iframe.width = '1000px';" +
                        "iframe.height = '300px';" +
                        "iframe.id = 'viewport';" +
                        "iframe.frameBorder = '0';" +
                        "iframe.scrolling = 'no';" +
                        "document.body.appendChild(iframe);"
        );

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public byte[] saveAsImage(WebDriver webDriver) {
        final WebElement viewport = webDriver.findElement(By.id("viewport"));
        final BufferedImage image = new AShot()
                .takeScreenshot(webDriver, viewport).getImage();

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private void login(final WebDriver webDriver) {

        webDriver.navigate().to(supersetLoginUrl);
        webDriver.findElement(By.id("username")).sendKeys(credential.getUsername());
        webDriver.findElement(By.id("password")).sendKeys(credential.getPassword());
        webDriver.findElement(By.id("password")).submit();
        loggedIn = true;

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

        public SupersetSniperPageImpl getSniperPage() {
            return sniperPage;
        }
    }

}
