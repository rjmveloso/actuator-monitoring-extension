# AppDynamics Spring Boot Actuator Monitoring Extension

## Use Case
This extensions collects metrics from the Spring Boot Actuator endpoint and publish them to the AppDynamics Metric Browser

## Prerequisites
Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

Spring Boot Actuator metrics must be enabled and exposed via HTTP
```
management.endpoints.web.exposure.include=metrics
```

## Installation
1. Clone the "actuator-monitoring-extension" repo using `git clone <repoUrl>` command.
2. Run 'mvn clean package' from "actuator-monitoring-extension" directory
3. Unzip the file "ActuatorMonitor-[version].zip" into `<MACHINE_AGENT_HOME>/monitors/`.
4. Configure the extension by referring to the below section.
5Restart the machine agent.


**NOTE:** Please place the extension in the "monitors" directory of your Machine Agent installation directory. Do not place the extension in the "extensions" directory of your Machine Agent installation directory.

## Configuration
Note : Please make sure not to use tab (\t) while editing yaml files. You can validate the yaml file using a [yaml validator](https://jsonformatter.org/yaml-validator)

Configure the extension by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/ActuatorMonitor/`.  
The metricPrefix of the extension has to be configured as specified [here](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695#Configuring%20an%20Extension).

```
# Use this only if SIM is enabled
#metricPrefix: "Custom Metrics|Spring Boot Actuator|"

# If SIM is not enabled, then use this
metricPrefix:  "Server|Component:<TIER_ID>|Custom Metrics|Spring Boot Actuator|"
```

### config.yml
1. Configure the server including the metric path `http://localhost:8080/app/actuator/metrics`
2. Configure the metric statistics settings to collect data
   ```yaml
   metric_config:
     - name: "HTTP Server Requests"
       endpoint: "/http.server.requests"
       statistics:
         - name: "COUNT"
           metric_type: ["AVERAGE", "AVERAGE", "INDIVIDUAL"]
         - name: "TOTAL_TIME"
           metric_type: ["AVERAGE", "AVERAGE", "INDIVIDUAL"]
   ```
   * **name**: metric name that will be displayed in the metric browser
   * **endpoint**: path to the metric (to be appended to the server uri)
   * **stat name**: the statistic name to collect  
   * **stat metric_type**: array to identify [aggregationType, timeRollUpType, clusterRollUpType]  
Please refer to Metric Processing Qualifiers [here](https://docs.appdynamics.com/22.1/en/infrastructure-visibility/machine-agent/extensions-and-custom-metrics/build-a-monitoring-extension-using-scripts#BuildaMonitoringExtensionUsingScripts-DefineYourMetrics).

### metricPathReplacements
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/Metric-Path-CharSequence-Replacements-in-Extensions/ta-p/35412) page to get detailed instructions on configuring Metric Path Character sequence replacements in Extensions.

## Credentials Encryption
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) page to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following [document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130) for how to use the Extensions WorkBench

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/rjmveloso/actuator-monitoring-extension).

**Note**: While extensions are maintained and supported by customers under the open-source licensing model, they interact with agents and Controllers that are subject to [AppDynamics’ maintenance and support policy](https://docs.appdynamics.com/latest/en/product-and-release-announcements/maintenance-support-for-software-versions). Some extensions have been tested with AppDynamics 4.5.13+ artifacts, but you are strongly recommended against using versions that are no longer supported.
