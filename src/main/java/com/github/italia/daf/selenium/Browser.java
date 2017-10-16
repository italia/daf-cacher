package com.github.italia.daf.selenium;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class Browser {

    private final URL seleniumServerURL;
    private final Capabilities caps;
    private WebDriver webDriver;

    private Browser(URL seleniumServerURL, Capabilities caps) {
        this.seleniumServerURL = seleniumServerURL;
        this.caps = caps;
    }


    public WebDriver webDriver() {
        if (this.webDriver == null)
            webDriver = new RemoteWebDriver(this.seleniumServerURL, this.caps);

        return webDriver;
    }


    public static class Builder {
        private final URL seleniumServerURL;
        private DesiredCapabilities browserCaps;

        public Builder(URL seleniumServerURL) {
            this.seleniumServerURL = seleniumServerURL;
        }

        public Builder capabilities(DesiredCapabilities caps) {
            this.browserCaps = caps;
            return this;
        }

        public Builder chrome() {
            this.browserCaps = DesiredCapabilities.chrome();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("'--start-maximized", "--window-size=1360,1020", "--headless");
            this.browserCaps.setCapability(ChromeOptions.CAPABILITY, options);
            return this;
        }

        public Builder firefox() {
            this.browserCaps = DesiredCapabilities.firefox();
            return this;
        }

        public Browser build() {
            return new Browser(this.seleniumServerURL, this.browserCaps);
        }

    }
}
