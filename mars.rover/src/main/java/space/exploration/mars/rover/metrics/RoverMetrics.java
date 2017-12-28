package space.exploration.mars.rover.metrics;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import communications.protocol.ModuleDirectory;

import java.util.concurrent.TimeUnit;

public class RoverMetrics {
    private final MetricsRegistry metricsRegistry     = new MetricsRegistry();
    private       Meter[]         stateMachineMetrics = new Meter[ModuleDirectory.Module.values().length];

    public RoverMetrics() {

    }

    public void incrementMeter(ModuleDirectory.Module module) {
        stateMachineMetrics[module.getValue()].mark();
    }

}
