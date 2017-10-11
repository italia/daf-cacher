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
    private boolean authenticated;
    private Token token;
    private final static String HEADER_X_KEY = "X-Metabase-Session";


    public HTTPClient(URL metabaseHost, final Credential credential) {
        this.metabaseHost = metabaseHost;
        this.credential = credential;
        this.authenticated = false;
    }

    public HTTPClient(URL metabaseHost, final Token token) {
        this.metabaseHost = metabaseHost;
        this.token = token;
        this.authenticated = true;
    }


    public Token authenticate() throws IOException {
        final Gson gson = new GsonBuilder().create();
        final Content response = Request.Post(this.metabaseHost.toString() + "/session")
                .setHeader("Content-Type", "application/json")
                .body(new StringEntity(gson.toJson(this.credential)))
                .execute().returnContent();

        this.token = gson.fromJson(response.asString(), Token.class);
        this.authenticated = true;
        return this.token;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public List<Card> getPublicCards() throws IOException {
        final Content content = executeGetAuthenticatedMethod("/card/public");
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(content.asString(), new TypeToken<List<Card>>() {
        }.getType());
    }

    private Content executeGetAuthenticatedMethod(final String apiEndpoint) throws IOException {
        return Request.Get(this.metabaseHost.toString() + apiEndpoint)
                .setHeader("Content-Type", "application/json")
                .setHeader(HEADER_X_KEY, this.token.id)
                .execute().returnContent();
    }


    public static class Credential {
        private String username;
        private String password;

        public Credential(final String username, final String password) {
            this.setUsername(username);
            this.setPassword(password);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Token {
        private String id;

        public Token() {
        }

        public Token(String token) {
            this.id = token;
        }

        public String getId() {
            return this.id;
        }
    }

    public static class Card {
        public int id;
        public String public_uuid;

        public String toString() {
            return "id:" + id + " public_uuid:" + public_uuid;
        }
    }

}
