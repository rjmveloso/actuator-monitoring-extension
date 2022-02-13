package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.appdynamics.extensions.micrometer.http.HttpClientHelper;
import io.github.appdynamics.extensions.micrometer.utils.Constants;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author ricardo.veloso
 */
public class MicrometerMonitorTask implements AMonitorTaskRunnable {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(MicrometerMonitorTask.class);

    private boolean heartbeat = true;

    private final String prefix;
    private final Map<String, ?> server;
    private final List<Map<String, ?>> configs;
    private final HttpClientHelper client;
    private final MetricWriteHelper writer;

    public MicrometerMonitorTask(String prefix, Map<String, ?> server, List<Map<String, ?>> configs, HttpClientHelper client, MetricWriteHelper writer) {
        this.prefix = prefix;
        this.server = server;
        this.client = client;
        this.writer = writer;
        this.configs = configs;
    }

    @Override
    public void run() {
        List<Metric> metrics = collect();
        metrics.add(createHeartbeatMetric());
        writer.transformAndPrintMetrics(metrics);
    }

    @Override
    public void onTaskComplete() {
        LOGGER.info("Finished collecting metrics for {}", getServerUri());
    }

    private String getServerUri() {
        return (String) server.get("uri");
    }

    @SuppressWarnings("unchecked")
    private List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>(configs.size());

        for (Map<String, ?> config : configs) {
            try {
                UrlBuilder builder = UrlBuilder.fromYmlServerConfig(server).path((String) config.get("endpoint"));
                List<Map<String, ?>> statistics = (List<Map<String, ?>>) config.get(Constants.STATISTICS);
                MetricRetriever retriever = new MetricRetriever(builder.build(), (String) config.get("name"), statistics);
                metrics.addAll(retriever.call());
            } catch (Exception e) {
                heartbeat = false;
            }
        }

        return metrics;
    }

    private Metric createHeartbeatMetric() {
        return new Metric("heartbeat", heartbeat ? "1" : "0", prefix, getServerUri());
    }

    private class MetricRetriever implements Callable<List<Metric>> {

        private final String url;
        private final String name;
        private final List<Map<String, ?>> stats;

        private MetricRetriever(String url, String name, List<Map<String, ?>> stats) {
            this.url = url;
            this.name = name;
            this.stats = stats;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Metric> call() throws Exception {
            JsonNode response = client.read(url);

            if (response == null || response.isEmpty()) {
                return Collections.emptyList();
            }

            List<Metric> metrics = new ArrayList<>(stats.size());

            Iterable<JsonNode> measurements = JsonUtils.getNestedObject(response, "measurements");

            for (Map<String, ?> stat : stats) {
                String statName = (String) stat.get(Constants.STATISTIC_NAME);
                List<String> type = (List<String>) stat.get(Constants.METRIC_TYPE);

                String value = retrieve(measurements, statName);
                metrics.add(new Metric(name, value, prefix + "|" + name + "|" + statName, type.get(0), type.get(1), type.get(2)));
            }

            return metrics;
        }

        private String retrieve(Iterable<JsonNode> measurements, String fetch) {
            for (JsonNode data : measurements) {
                if (fetch.equals(data.get("statistic").asText())) {
                    return data.get("value").asText("0");
                }
            }
            return "0";
        }
    }

}
