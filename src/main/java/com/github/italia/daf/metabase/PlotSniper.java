package com.github.italia.daf.metabase;


import com.github.italia.daf.util.LoggerFactory;
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
import java.util.logging.Logger;

public class PlotSniper {
    private static final Logger LOGGER = LoggerFactory.getLogger( PlotSniper.class.getName() );
    private WebDriver driver;
    public PlotSniper(WebDriver driver){
        this.driver = driver;
    }

    public File shoot(final String url) throws IOException {
        visit(url);
        final File file = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);

        if (file == null)
            throw new IOException("Unable to save the viewport buffer");

        return file;
    }


    public byte[] shootAsByte(final String url) throws IOException {
        visit(url);
        final byte[] buffer = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);

        if (buffer == null || buffer.length == 0)
            throw new IOException("Unable to save the viewport buffer");

        return buffer;
    }

    public String shootAsBase64(final String url) throws IOException {
        visit(url);
        final String buffer = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);

        if (buffer == null || buffer.isEmpty())
            throw new IOException("Unable to save the viewport buffer");

        return buffer;
    }

    private void visit(final String url){
        driver.get(url);
        (new WebDriverWait(driver, 5)).until((ExpectedCondition<Boolean>) driver ->
                driver.findElement(By.name("downarrow")).isDisplayed());

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
                    .size(w,h)
                    .keepAspectRatio(true)
                    .toOutputStream(thumb);
            return thumb.toByteArray();
        }
    }

    public static class Geometry {
        public int h;
        public int w;

        public Geometry(int w, int h){
            this.w = w;
            this.h = h;
        }

        public Geometry(){
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
