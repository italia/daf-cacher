package com.github.italia.daf;

import com.github.italia.daf.dafapi.HTTPClient;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.sniper.PageSniper;
import com.github.italia.daf.superset.SupersetSniperPageImpl;
import com.github.italia.daf.utils.Configuration;
import com.github.italia.daf.utils.Credential;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Superset {

    public static void main(String[] args) throws IOException {
        final Properties properties = new Configuration(args[0]).load();
        final WebDriver webDriver = new Browser
                .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                .chrome()
                .build()
                .webDriver();
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        try {


            Credential credential = new Credential(
                    properties.getProperty("daf_api.user"),
                    properties.getProperty("daf_api.password")
            );
            HTTPClient client = new HTTPClient(new URL(properties.getProperty("daf_api.host")), credential);
            client.authenticate();

            final PageSniper pageSniper = new PageSniper(webDriver, 30);

            Page handler = new SupersetSniperPageImpl
                    .Builder()
                    .setSupersetLoginUrl(new URL(properties.getProperty("superset.login_url")))
                    .setCredential(new Credential(
                            properties.getProperty("superset.user"),
                            properties.getProperty("superset.password"))
                    )
                    .getSniperPage();

            pageSniper.setPageHandler(handler);


            for (final HTTPClient.EmbeddableData data : client.getEmbeddableDataList()) {
                if (!data.getOrigin().equalsIgnoreCase("superset"))
                    continue;

                try {
                    System.out.println(data.getIframeUrl());
                    final byte[] payload = pageSniper.shoot(data.getIframeUrl());
                    IOUtils.write(payload, new FileOutputStream(new File("/tmp/superset/" + data.getIdentifier() + ".png")));
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }


            }
        } finally {
            webDriver.close();
            webDriver.quit();
        }


    }
}
