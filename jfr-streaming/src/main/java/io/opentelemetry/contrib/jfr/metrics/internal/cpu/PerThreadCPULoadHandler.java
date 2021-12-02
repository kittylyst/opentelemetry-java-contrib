/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.cpu;

import static io.opentelemetry.contrib.jfr.metrics.internal.Constants.*;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import java.time.Duration;
import java.util.Optional;
import jdk.jfr.consumer.RecordedEvent;

public final class PerThreadCPULoadHandler implements RecordedEventHandler {
  private static final String SIMPLE_CLASS_NAME = PerThreadCPULoadHandler.class.getSimpleName();
  private static final String EVENT_NAME = "jdk.CPULoad";
  private static final String JVM_USER = "jvmUser";
  private static final String JVM_SYSTEM = "jvmSystem";
  private static final String MACHINE_TOTAL = "machineTotal";

  private static final String METRIC_NAME = "runtime.jvm.cpu.utilization";
  private static final String DESCRIPTION = "CPU Utilization";

  private BoundDoubleHistogram userHistogram;
  private BoundDoubleHistogram systemHistogram;
  private BoundDoubleHistogram machineHistogram;

  @Override
  public void initializeMeter(Meter meter) {
    var attr = Attributes.of(ATTR_CPU_USAGE, USER);
    var builder = meter.histogramBuilder(METRIC_NAME);
    builder.setDescription(DESCRIPTION);
    builder.setUnit(ONE);
    userHistogram = builder.build().bind(attr);

    attr = Attributes.of(ATTR_CPU_USAGE, SYSTEM);
    builder = meter.histogramBuilder(METRIC_NAME);
    builder.setDescription(DESCRIPTION);
    builder.setUnit(ONE);
    systemHistogram = builder.build().bind(attr);

    attr = Attributes.of(ATTR_CPU_USAGE, MACHINE);
    builder = meter.histogramBuilder(METRIC_NAME);
    builder.setDescription(DESCRIPTION);
    builder.setUnit(ONE);
    machineHistogram = builder.build().bind(attr);
  }

  @Override
  public void accept(RecordedEvent ev) {
    if (ev.hasField(JVM_USER)) {
      userHistogram.record(ev.getDouble(JVM_USER));
    }
    if (ev.hasField(JVM_SYSTEM)) {
      systemHistogram.record(ev.getDouble(JVM_SYSTEM));
    }
    if (ev.hasField(MACHINE_TOTAL)) {
      machineHistogram.record(ev.getDouble(MACHINE_TOTAL));
    }
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public Optional<Duration> getPollingDuration() {
    return Optional.of(Duration.ofSeconds(1));
  }
}
