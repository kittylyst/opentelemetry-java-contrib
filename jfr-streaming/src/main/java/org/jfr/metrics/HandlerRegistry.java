package org.jfr.metrics;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import org.jfr.metrics.container.ContainerConfigurationHandler;
import org.jfr.metrics.cpu.ContextSwitchRateHandler;
import org.jfr.metrics.cpu.OverallCPULoadHandler;
import org.jfr.metrics.memory.G1GarbageCollectionHandler;
import org.jfr.metrics.memory.GCHeapSummaryHandler;
import org.jfr.metrics.memory.ObjectAllocationInNewTLABHandler;
import org.jfr.metrics.memory.ObjectAllocationOutsideTLABHandler;
import org.jfr.metrics.network.NetworkReadHandler;
import org.jfr.metrics.network.NetworkWriteHandler;

import java.util.*;
import java.util.stream.Stream;

public class HandlerRegistry {

  private final List<RecordedEventHandler> mappers;

  private HandlerRegistry(List<? extends RecordedEventHandler> mappers) {
    this.mappers = new ArrayList<>(mappers);
  }

  public static HandlerRegistry createDefault(SdkMeterProvider meterProvider) {
    var schemaUrl = "https://localhost/"; // FIXME
    var otelMeter = meterProvider.get("org.jfr", "0.0.1-SNAPSHOT", schemaUrl);

    var grouper = new ThreadGrouper();
    var filtered = List.of(
              new ObjectAllocationInNewTLABHandler(otelMeter, grouper),
              new ObjectAllocationOutsideTLABHandler(otelMeter, grouper),
              new NetworkReadHandler(otelMeter, grouper),
              new NetworkWriteHandler(otelMeter, grouper),
              new G1GarbageCollectionHandler(otelMeter),
              new GCHeapSummaryHandler(otelMeter),
              new ContextSwitchRateHandler(otelMeter),
              new OverallCPULoadHandler(otelMeter),
              new ContainerConfigurationHandler(otelMeter)
            );
    filtered.forEach(RecordedEventHandler::init);

    return new HandlerRegistry(filtered);
  }

  /** @return a stream of all entries in this registry. */
  public Stream<RecordedEventHandler> all() {
    return mappers.stream();
  }

}