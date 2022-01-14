package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.metrics.MetricCharSequenceReplacer;
import com.appdynamics.extensions.util.MetricPathUtils;
import io.github.appdynamics.extensions.micrometer.http.HttpClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.Suite;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static io.github.appdynamics.extensions.micrometer.utils.Constants.*;
import static io.github.appdynamics.extensions.micrometer.utils.MockUtils.*;
import static org.mockito.Mockito.*;

/**
 * @author ricardo.veloso
 */
@Suite
@ExtendWith(MockitoExtension.class)
public class MicrometerMonitorTaskTest {

    @Captor
    private ArgumentCaptor<List<Metric>> captor;

    @BeforeAll
    static void init() {
        ABaseMonitor monitor = mockBaseMonitor();
        MetricPathUtils.registerMetricCharSequenceReplacer(monitor);
    }

    @Test
    void micrometerMonitorTaskTest() throws IOException {
        String url = (String) getServer().get("uri");

        HttpClientHelper client = mock(HttpClientHelper.class);
        when(client.read(url + "/jvm.memory.used")).thenReturn(getResourceAsJson("/data/jvm-memory-used.json"));
        when(client.read(url + "/http.server.requests")).thenReturn(getResourceAsJson("/data/http-server-requests.json"));

        MetricWriteHelper writer = mock(MetricWriteHelper.class);

        MicrometerMonitorTask victim = new MicrometerMonitorTask(DEFAULT_METRIC_PREFIX, getServer(), getMetricConfigs(), client, writer);
        victim.run();

        verify(writer).transformAndPrintMetrics(captor.capture());

        assertExpectedMetrics(captor.getValue(),
                "[AVERAGE/AVERAGE/INDIVIDUAL] [" + DEFAULT_METRIC_PREFIX + "|JVM Memory Used|VALUE]=[1.91]]",
                "[AVERAGE/AVERAGE/INDIVIDUAL] [" + DEFAULT_METRIC_PREFIX + "|HTTP Server Requests|COUNT]=[40.0]]",
                "[AVERAGE/AVERAGE/INDIVIDUAL] [" + DEFAULT_METRIC_PREFIX + "|HTTP Server Requests|TOTAL_TIME]=[5292.4442]]",
                "[AVERAGE/AVERAGE/INDIVIDUAL] [" + DEFAULT_METRIC_PREFIX + "|HTTP Server Requests|MAX]=[0.0]]",
                "[AVERAGE/AVERAGE/INDIVIDUAL] [" + DEFAULT_METRIC_PREFIX + "|" + url + "]=[1]]"
        );
    }

    private static ABaseMonitor mockBaseMonitor() {
        MetricCharSequenceReplacer replacer = new MetricCharSequenceReplacer(Collections.emptyMap());

        MonitorContext context = mock(MonitorContext.class);
        when(context.getMetricCharSequenceReplacer()).thenReturn(replacer);

        MonitorContextConfiguration configuration = mock(MonitorContextConfiguration.class);
        when(configuration.getContext()).thenReturn(context);

        ABaseMonitor monitor = mock(ABaseMonitor.class);
        when(monitor.getContextConfiguration()).thenReturn(configuration);

        return monitor;
    }

    private void assertExpectedMetrics(List<Metric> actual, String... expected) {
        for (int i = 0; i < actual.size(); i++) {
            Assertions.assertEquals(expected[i], actual.get(i).toString());
        }
    }

}
