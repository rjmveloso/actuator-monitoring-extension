package io.github.appdynamics.extensions.micrometer.utils;

import com.appdynamics.extensions.conf.processor.ConfigProcessor;
import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MockUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, ?> CONFIG;

    static {
        CONFIG = ConfigProcessor.process(YmlReader.readFromFileAsMap(new File("src/test/resources/conf/config.yml")));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ?> getServer() {
        return ((List<Map<String, ?>>) CONFIG.get(Constants.SERVERS)).get(0);
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, ?>> getMetricConfigs() {
        return (List<Map<String, ?>>) CONFIG.get(Constants.METRIC_CONFIG);
    }

    public static JsonNode getResourceAsJson(String path) throws IOException {
        InputStream is = MockUtils.class.getResourceAsStream(path);
        return MAPPER.readTree(is);
    }
}
