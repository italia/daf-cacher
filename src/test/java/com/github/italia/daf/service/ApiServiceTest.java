package com.github.italia.daf.service;

import com.github.italia.daf.metabase.HTTPClient;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class ApiServiceTest {
    final static Properties properties = new Properties();
    static ApiService service;

    static {
        try (InputStream stream = ApiServiceTest
                .class
                .getClassLoader()
                .getResourceAsStream("config-test.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            service = new ApiService(properties);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @BeforeClass
    public static void startServer() {
        service.start();
    }

    @AfterClass
    public static void stopServer() {
        service.stop();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPlotList() throws IOException {
        final HttpResponse response = Request.Get("http://localhost:4567/plot/")
                .execute()
                .returnResponse();
        assertTrue(response.getStatusLine().getStatusCode() == 200);
        final String body = EntityUtils.toString(response.getEntity());


        final Gson gson = new GsonBuilder().create();
        List<HTTPClient.Card> cards = gson.fromJson(body, new TypeToken<List<HTTPClient.Card>>() {
        }.getType());
        assertTrue(!cards.isEmpty());
    }

    @Test
    public void testPlot() throws IOException {
        final String plotId = "d68c6cae-0494-452a-871d-822e345c3981";
        final HttpResponse response = Request.Get("http://localhost:4567/plot/" + plotId + "/original")
                .execute()
                .returnResponse();
        assertTrue(response.getStatusLine().getStatusCode() == 200);
        assertTrue("image/png".equals(response.getEntity().getContentType().getValue()));
    }

    @Test
    public void testPlotResize()throws IOException {
        final String plotId = "d68c6cae-0494-452a-871d-822e345c3981";
        final HttpResponse response = Request.Get("http://localhost:4567/plot/" + plotId + "/200x200")
                .execute()
                .returnResponse();
        assertTrue(response.getStatusLine().getStatusCode() == 200);
        assertTrue("image/png".equals(response.getEntity().getContentType().getValue()));

    }

    @Test
    public void testPlotWrongParameter()throws IOException {
        final String plotId = "d68c6cae-0494-452a-871d-822e345c3981";
        final HttpResponse response = Request.Get("http://localhost:4567/plot/" + plotId + "/aaax200")
                .execute()
                .returnResponse();
        assertTrue(response.getStatusLine().getStatusCode() == 404);

    }


}