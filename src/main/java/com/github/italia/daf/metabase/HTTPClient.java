package com.github.italia.daf.metabase;

import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.Token;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
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
    private static final String HEADER_X_KEY = "X-Metabase-Session";


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
                .setHeader(HEADER_X_KEY, this.token.getId())
                .execute().returnContent();
    }


    public static class Card {
        private int id;
        @SerializedName("public_uuid")
        private String publicUuid;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setPublicUuid(String public_uuid) {
            this.publicUuid = public_uuid;
        }

        public String getPublicUuid() {
            return publicUuid;
        }

        public String toString() {
            return "id:" + id + " public_uuid:" + publicUuid;
        }
    }

}
