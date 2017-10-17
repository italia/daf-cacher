package com.github.italia.daf.metabase;

import com.github.italia.daf.dafapi.HTTPClient;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.service.ApiServiceTest;
import com.github.italia.daf.superset.SupersetSniperPageImpl;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.IntegrationTestDataProvider;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MetabaseSniperPageImplTest {
    final static Properties properties = new Properties();
    static WebDriver webDriver;

    private MetabaseSniperPageImpl metabaseSniperPage;
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
        metabaseSniperPage = new MetabaseSniperPageImpl();
        provider = new IntegrationTestDataProvider();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void visit() throws Exception {
        final HTTPClient.EmbeddableData d = provider.getList()
                .stream()
                .filter(x -> x.getOrigin().equals("metabase"))
                .findFirst()
                .get();
        metabaseSniperPage.visit(webDriver, d.getIframeUrl());

        assertTrue(webDriver.findElement(By.name("downarrow")).isDisplayed());

        final  byte[] buff = metabaseSniperPage.saveAsImage(webDriver);
        assertTrue(buff.length > 0);
    }

}