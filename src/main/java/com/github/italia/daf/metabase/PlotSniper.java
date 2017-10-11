package com.github.italia.daf.metabase;


import net.coobird.thumbnailator.Thumbnails;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PlotSniper {
    private WebDriver driver;

    public PlotSniper(WebDriver driver) {
        this.driver = driver;
    }

    public File shoot(final String url, int timeOutInSecond) throws IOException {
        visit(url, timeOutInSecond);
        final File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        if (file == null)
            throw new IOException("Unable to save the viewport buffer");

        return file;
    }


    public byte[] shootAsByte(final String url, int timeOutInSecond) throws IOException {
        visit(url, timeOutInSecond);
        final byte[] buffer = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

        if (buffer == null || buffer.length == 0)
            throw new IOException("Unable to save the viewport buffer");

        return buffer;
    }

    public String shootAsBase64(final String url, int timeOutInSecond) throws IOException {
        visit(url, timeOutInSecond);
        final String buffer = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

        if (buffer == null || buffer.isEmpty())
            throw new IOException("Unable to save the viewport buffer");

        return buffer;
    }

    private void visit(final String url, int timeOutInSecond) {
        driver.get(url);
        (new WebDriverWait(driver, timeOutInSecond)).until((ExpectedCondition<Boolean>) d -> {
            assert d != null;
            return d.findElement(By.name("downarrow")).isDisplayed();
        });
    }

    public static class Resize {
        private byte[] buffer;

        public Resize(byte[] buffer) {
            this.buffer = buffer;
        }

        public byte[] to(Geometry g) throws IOException {
            return to(g.w, g.h);
        }

        public byte[] to(int w, int h) throws IOException {
            final ByteArrayOutputStream thumb = new ByteArrayOutputStream();
            Thumbnails.of(
                    new ByteArrayInputStream(this.buffer))
                    .size(w, h)
                    .keepAspectRatio(true)
                    .toOutputStream(thumb);
            return thumb.toByteArray();
        }
    }

    public static class Geometry {
        private int h;
        private int w;

        public Geometry(int w, int h) {
            this.w = w;
            this.h = h;
        }

        public Geometry() {
        }

        @Override
        public String toString() {
            return w + "x" + h;
        }

        public static Geometry fromString(String g) {
            final String[] gg = g.split("x");
            return new Geometry(Integer.parseInt(gg[0]), Integer.parseInt(gg[1]));
        }
    }
}
