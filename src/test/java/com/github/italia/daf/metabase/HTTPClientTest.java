package com.github.italia.daf.metabase;

import com.github.italia.daf.service.ApiServiceTest;
import com.github.italia.daf.util.Credential;
import com.github.italia.daf.util.Token;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class HTTPClientTest {
    final static Properties properties = new Properties();

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

    private Credential getCredential() {
        return new Credential(properties.getProperty("metabase.username"), properties.getProperty("metabase.password"));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * /api/session has some non-configurable throttler attached
     * It makes te test to fail once in a while
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testLogin() throws Exception {
        HTTPClient client = new HTTPClient(new URL("http://localhost:3000/api"), getCredential());
        Token token = client.authenticate();
        assertTrue(!token.getId().isEmpty());
        assertTrue(client.isAuthenticated());
    }

    @Test
    public void testGetCards() throws Exception {
        HTTPClient client = new HTTPClient(new URL("http://localhost:3000/api"), getCredential());
        client.authenticate();
        final List<HTTPClient.Card> cards = client.getPublicCards();
        assertTrue(!cards.isEmpty());
    }

}