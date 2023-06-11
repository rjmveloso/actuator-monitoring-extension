package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.metrics.Metric;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.file.FileReader;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.serialization.ExpectationSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 1080)
public class MicrometerMonitorIT {

    private final CountDownLatch latch = new CountDownLatch(1);

    private final MicrometerMonitor monitor = new MicrometerMonitor() {
        @Override
        protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
            tasksExecutionServiceProvider.getMetricWriteHelper().setCacheMetrics(true);
            super.doRun(tasksExecutionServiceProvider);
        }

        @Override
        protected void onComplete() {
            super.onComplete();
            latch.countDown();
        }
    };

    @BeforeEach
    public void beforeEachLifecyleMethod(MockServerClient client) {
        String json = FileReader.readFileFromClassPathOrPath("mock/expectations.json");
        Expectation[] expectations = new ExpectationSerializer(new MockServerLogger()).deserializeArray(json, false);
        client.upsert(expectations);
    }

    @Test
    public void test() throws TaskExecutionException, InterruptedException {
        Map<String, String> config = new HashMap<>();
        config.put("config-file", "src/test/resources/conf/config.yml");

        monitor.execute(config, null);

        latch.await(5, TimeUnit.SECONDS);

        // com.appdynamics.extensions.MetricWriteHelper also provides the number of metric uploaded
        Map<String, Metric> metrics = monitor.getContextConfiguration().getContext().getCachedMetrics();
        Assertions.assertEquals(5, metrics.size());
    }

}
