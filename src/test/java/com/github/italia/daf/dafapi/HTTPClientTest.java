package com.github.italia.daf.dafapi;

import com.github.italia.daf.service.ApiServiceTest;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.DafApiMock;
import com.github.italia.daf.utils.RandomDataProvider;
import com.github.italia.daf.utils.Token;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class HTTPClientTest {
    private HTTPClient client;
    static DafApiMock dafApiMock;
    final static Properties properties = new Properties();

    static {
        try (InputStream stream = ApiServiceTest
                .class
                .getClassLoader()
                .getResourceAsStream("config-test.properties")) {
            properties.load(stream);
            dafApiMock = new DafApiMock(properties, new RandomDataProvider());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @BeforeClass
    public static void beforeClass() {
        dafApiMock.start();
    }

    @AfterClass
    public static void afterClass() {
        dafApiMock.stop();
    }

    @Before
    public void setUp() throws Exception {
        Credential credential = new Credential(
                properties.getProperty("daf_api.user"),
                properties.getProperty("daf_api.password")
        );
        client = new HTTPClient(new URL(properties.getProperty("daf_api.host")), credential);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDataList() throws Exception {
        client.authenticate();
        assertTrue(client.getEmbeddableDataList().size() > 0);

    }

    @Test
    public void testAuthenticate() throws Exception {
        final Token token = client.authenticate();
        assertTrue(!(token == null));
    }


}