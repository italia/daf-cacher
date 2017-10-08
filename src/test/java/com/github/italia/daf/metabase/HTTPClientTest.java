package com.github.italia.daf.metabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class HTTPClientTest {

    private HTTPClient.Credential getCredential() {
        return new HTTPClient.Credential(System.getenv("METABASE_USERNAME"), System.getenv("METABASE_PASSWORD"));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testLogin() throws Exception {
        HTTPClient client = new HTTPClient(new URL(System.getenv("METABASE_URL")), getCredential());
        client.authenticate();
        assertTrue(client.isAuthenticated());
    }

    @Test
    public void testGetCards() throws Exception {
        HTTPClient client = new HTTPClient(new URL(System.getenv("METABASE_URL")), getCredential());
        client.authenticate();
        final List<HTTPClient.Card> cards =  client.getPublicCards();
        assertTrue(!cards.isEmpty());

    }

}