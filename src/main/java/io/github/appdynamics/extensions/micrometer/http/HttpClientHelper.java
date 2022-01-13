package io.github.appdynamics.extensions.micrometer.http;

import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

import static java.util.Collections.singletonMap;

/**
 * Helper class to abstract the usage of Apache Http Client
 */
public class HttpClientHelper {

    private final MonitorContext context;

    public HttpClientHelper(MonitorContext context) {
        this.context = context;
    }

    public JsonNode read(String url) {
        CloseableHttpClient client = context.getHttpClient();
        return HttpClientUtils.getResponseAsJson(client, url, JsonNode.class, singletonMap("Accept", "application/json"));
    }

}
