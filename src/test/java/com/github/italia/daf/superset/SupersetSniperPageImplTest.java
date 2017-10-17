package com.github.italia.daf.superset;

import com.github.italia.daf.dafapi.HTTPClient;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.service.ApiServiceTest;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.DafApiMock;
import com.github.italia.daf.utils.IntegrationTestDataProvider;
import com.github.italia.daf.utils.RandomDataProvider;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SupersetSniperPageImplTest {
    final static Properties properties = new Properties();
    static WebDriver webDriver;
    private SupersetSniperPageImpl supersetSniperPage;
    private IntegrationTestDataProvider provider;
    static {
        try (InputStream stream = ApiServiceTest
                .class
                .getClassLoader()
                .getResourceAsStream("config-test.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @BeforeClass
    public static void beforeClass() throws MalformedURLException {
        webDriver = new Browser
                .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                .chrome()
                .build()
                .webDriver();
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void afterClass() {
        webDriver.close();
        webDriver.quit();
    }

    @Before
    public void setUp() throws Exception {
        supersetSniperPage = new SupersetSniperPageImpl
                .Builder()
                .setSupersetLoginUrl(new URL(properties.getProperty("superset.login_url")))
                .setCredential(
                        new Credential(
                                properties.getProperty("superset.user"),
                                properties.getProperty("superset.password")
                        ))
                .getSniperPage();

        provider = new IntegrationTestDataProvider();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void visit() throws Exception {
        final HTTPClient.EmbeddableData d = provider.getList()
                .stream()
                .filter(x -> x.getOrigin().equals("superset"))
                .findFirst()
                .get();
        supersetSniperPage.visit(webDriver, d.getIframeUrl());

        assertTrue(webDriver.findElement(By.id("viewport")) != null);

        final  byte[] buff = supersetSniperPage.saveAsImage(webDriver);
        assertTrue(buff.length > 0);
    }


}