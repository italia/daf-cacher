package com.github.italia.daf.dafapi;

import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.Token;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class HTTPClient {
    private URL apiHost;
    private Token token;
    private String authHeader;

    public HTTPClient(final URL apiHost, final Credential credential) {
        this.apiHost = apiHost;

        final String auth = credential.getUsername() + ":" + credential.getPassword();
        final byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("UTF-8")));
        authHeader = "Basic " + new String(encodedAuth);
    }

    public Token authenticate() throws IOException {
        final String tokenId = Request.Get(this.apiHost.toString() + "/security-manager/v1/token")
                .setHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .setHeader(HttpHeaders.ACCEPT, "application/json")
                .execute().returnContent().asString().replaceAll("^.|.$", "");
        this.token = new Token(tokenId);
        return token;
    }

    public List<EmbeddableData> getEmbeddableDataList() throws IOException {

        if (this.token == null)
            throw new IllegalArgumentException("Client not authenticated");

        final Gson gson = new GsonBuilder().create();
        final String response = Request.Get(this.apiHost.toString() + "/dati-gov/v1/dashboard/iframes")
                .setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.getId())
                .setHeader(HttpHeaders.ACCEPT, "application/json")
                .execute().returnContent().asString();

        return gson.fromJson(
                response,
                new TypeToken<List<EmbeddableData>>() {
                }.getType()
        );


    }

    public static class EmbeddableData {
        @SerializedName("iframe_url")
        private String iframeUrl;
        private String origin;
        private String title;
        private String identifier;

        public String getIframeUrl() {
            return iframeUrl;
        }

        public void setIframeUrl(String iframeUrl) {
            this.iframeUrl = iframeUrl;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }

}
