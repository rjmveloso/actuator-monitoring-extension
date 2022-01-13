package io.github.appdynamics.extensions.micrometer.http;

import com.appdynamics.extensions.conf.MonitorContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class HttpClientHelperTest {

    private MonitorContext context = mock(MonitorContext.class);
    private CloseableHttpClient client = mock(CloseableHttpClient.class);

    @BeforeEach
    void init() {
        when(context.getHttpClient()).thenReturn(client);
    }

    @Test
    void whenResponseIsNullExpectNull() throws IOException {
        when(client.execute(any(HttpGet.class))).thenReturn(null);

        HttpClientHelper victim = new HttpClientHelper(context);

        Assertions.assertNull(victim.read("http://localhost:8080"));
    }

    @Test
    void whenInvalidResponseStatusExpectNull() throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, "bad request"));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        HttpClientHelper victim = new HttpClientHelper(context);

        Assertions.assertNull(victim.read("http://localhost:8080"));
    }

    @Test
    void whenValidResponseStatusExpectJsonNode() throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "ok"));
        when(response.getEntity()).thenReturn(new StringEntity("{\"name\": \"metric\", \"description\": \"metric\"}", ContentType.APPLICATION_JSON));

        when(client.execute(any(HttpGet.class))).thenReturn(response);

        HttpClientHelper victim = new HttpClientHelper(context);
        JsonNode data = victim.read("http://localhost:8080");

        Assertions.assertNotNull(data);
        Assertions.assertEquals("metric", data.findValue("name").asText());
        Assertions.assertEquals("metric", data.findValue("description").asText());
    }


}
