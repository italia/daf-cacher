package com.github.italia.daf.metabase;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class HTTPClient {
    private URL metabaseHost;
    private Credential credential;
    boolean authenticated;
    private Token token;
    final static String HEADER_X_KEY = "X-Metabase-Session";

    public HTTPClient(URL metabaseHost, final Credential credential) {
        this.metabaseHost = metabaseHost;
        this.credential = credential;
        this.authenticated = false;

    }


    public void authenticate() throws IOException {
        final Gson gson = new GsonBuilder().create();
        final Content response = Request.Post(this.metabaseHost.toString() + "session")
                .setHeader("Content-Type", "application/json")
                .body(new StringEntity(gson.toJson(this.credential)))
                .execute().returnContent();

        this.token = gson.fromJson(response.asString(), Token.class);
        this.authenticated = true;
    }

    public boolean isAuthenticated(){
        return this.authenticated;
    }

    public List<Card> getPublicCards() throws IOException {
        final Content content = executeGetAuthenticatedMethod("card/public");
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(content.asString(), new TypeToken<List<Card>>(){}.getType());

    }

    private Content executeGetAuthenticatedMethod(final String apiEndpoint) throws IOException {
        final Gson gson = new GsonBuilder().create();
        return Request.Get(this.metabaseHost.toString() + apiEndpoint)
                .setHeader("Content-Type", "application/json")
                .setHeader(HEADER_X_KEY, this.token.id)
                .execute().returnContent();
    }


    public static class Credential {
        private String username;
        private String password;

        public Credential(final String username, final String password){
            this.username = username;
            this.password = password;
        }


    }

    private class Token {
        protected String id;
    }

    public static class Card {
        public int id;
        public String public_uuid;

        public String toString(){
            return "id:" + id + " public_uuid:" + public_uuid;
        }
    }

}
