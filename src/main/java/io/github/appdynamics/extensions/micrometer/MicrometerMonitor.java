package io.github.appdynamics.extensions.micrometer;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import io.github.appdynamics.extensions.micrometer.http.HttpClientHelper;

import java.util.List;
import java.util.Map;

import static io.github.appdynamics.extensions.micrometer.utils.Constants.*;

/**
 * @author ricardo.veloso
 */
public class MicrometerMonitor extends ABaseMonitor {

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        String prefix = getContextConfiguration().getMetricPrefix();
        List<Map<String, ?>> configs = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(METRIC_CONFIG);

        HttpClientHelper client = new HttpClientHelper(getContextConfiguration().getContext());
        MetricWriteHelper writer = tasksExecutionServiceProvider.getMetricWriteHelper();
        getServers().forEach(server -> {
            AMonitorTaskRunnable task = new MicrometerMonitorTask(prefix, server, configs, client, writer);
            tasksExecutionServiceProvider.submit((String) server.get(DISPLAY_NAME), task);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get(SERVERS);
    }
}
