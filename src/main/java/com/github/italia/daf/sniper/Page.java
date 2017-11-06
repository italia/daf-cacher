package com.github.italia.daf.sniper;

import org.openqa.selenium.WebDriver;

public interface Page {
    void visit(final WebDriver driver, final String url);

    byte[] saveAsImage(final WebDriver driver);
}
