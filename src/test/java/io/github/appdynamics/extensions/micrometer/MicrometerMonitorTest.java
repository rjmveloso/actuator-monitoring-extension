package io.github.appdynamics.extensions.micrometer;

import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MicrometerMonitorTest {

    @Test
    public void micrometerMonitorTest() throws TaskExecutionException {
        MicrometerMonitor monitor = new MicrometerMonitor();
        final Map<String, String> taskArgs = new HashMap<>();
        taskArgs.put("config-file", "src/test/resources/conf/config.yml");
        monitor.execute(taskArgs,null);
    }

}
