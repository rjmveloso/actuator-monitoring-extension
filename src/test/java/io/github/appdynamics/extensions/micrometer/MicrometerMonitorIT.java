package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.metrics.Metric;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MicrometerMonitorIT {

    private final MicrometerMonitor monitor = new MicrometerMonitor() {
        @Override
        protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
            tasksExecutionServiceProvider.getMetricWriteHelper().setCacheMetrics(true);
            super.doRun(tasksExecutionServiceProvider);
        }
    };

    @Test
    public void test() throws TaskExecutionException, InterruptedException {
        Map<String, String> config = new HashMap<>();
        config.put("config-file", "src/test/resources/conf/config.yml");

        monitor.execute(config, null);

        // TODO wait for the executor ??
        Thread.sleep(5000);

        Map<String, Metric> metrics = monitor.getContextConfiguration().getContext().getCachedMetrics();
        Assertions.assertEquals(5, metrics.size());
    }

}
