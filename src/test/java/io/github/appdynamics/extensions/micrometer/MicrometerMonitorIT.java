package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.metrics.Metric;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MicrometerMonitorIT {

    private CountDownLatch latch = new CountDownLatch(1);

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
