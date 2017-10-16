package com.github.italia.daf.sniper;

import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;

public class PageSniper {
    private static final String ERROR_MSG = "Unable to save the viewport buffer";
    private WebDriver driver;
    private int timeout;
    private Page pageHandler;

    public PageSniper(WebDriver driver, int timeOutInSecond) {
        this.driver = driver;
        this.timeout = timeOutInSecond;
    }

    public void setPageHandler(Page handler) {
        this.pageHandler = handler;
    }


    public byte[] shoot(final String url) throws IOException, TimeoutException {

        final Duration duration = Duration.ofSeconds(this.timeout);
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<byte[]> future = executor.submit(() -> {
            pageHandler.visit(PageSniper.this.driver, url);
            return pageHandler.saveAsImage(PageSniper.this.driver);
        });

        try {
            final byte[] buffer = future.get(duration.toMillis(), TimeUnit.MILLISECONDS);
            if (buffer == null || buffer.length == 0)
                throw new IOException(ERROR_MSG);
            return buffer;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            future.cancel(true);
            throw new TimeoutException("Page load timeout");
        } finally {
            executor.shutdownNow();
        }

    }
}
