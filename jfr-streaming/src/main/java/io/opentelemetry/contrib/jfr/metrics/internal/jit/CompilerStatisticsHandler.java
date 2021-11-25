/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.jit;

import static io.opentelemetry.contrib.jfr.metrics.internal.Constants.MILLISECONDS;
import static io.opentelemetry.contrib.jfr.metrics.internal.Constants.ONE;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import jdk.jfr.consumer.RecordedEvent;

public final class CompilerStatisticsHandler implements RecordedEventHandler {
  private static final String EVENT_NAME = "jdk.CompilerStatistics";
  private static final String METRIC_NAME_COUNT = "runtime.jvm.compiler.count";
  private static final String METRIC_NAME_SIZE = "runtime.jvm.compiler.size";
  private static final String METRIC_NAME_TIME = "runtime.jvm.compiler.time";

  private static final String COMPILE_COUNT = "compileCount";
  private static final String COMPILE_SIZE =
      "nmetodsSize"; // This is correct, there is a typo in the JFR field name
  private static final String COMPILE_TIME = "totalTimeSpent";

  private final Meter otelMeter;
  private volatile long countValue = 0L;
  private volatile double sizeValue = 0.0;
  private volatile double timeValue = 0.0;

  public CompilerStatisticsHandler(Meter otelMeter) {
    this.otelMeter = otelMeter;
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  public CompilerStatisticsHandler init() {
    otelMeter
        .upDownCounterBuilder(METRIC_NAME_COUNT)
        .ofDoubles()
        .setUnit(ONE)
        .buildWithCallback(codm -> codm.observe(countValue));
    otelMeter
        .upDownCounterBuilder(METRIC_NAME_SIZE)
        .ofDoubles()
        .setUnit(ONE) // Really MB
        .buildWithCallback(codm -> codm.observe(sizeValue));
    otelMeter
        .upDownCounterBuilder(METRIC_NAME_TIME)
        .ofDoubles()
        .setUnit(MILLISECONDS) // Really s
        .buildWithCallback(codm -> codm.observe(timeValue));

    return this;
  }

  @Override
  public void accept(RecordedEvent ev) {
    if (ev.hasField(COMPILE_COUNT)) {
      countValue = ev.getLong(COMPILE_COUNT);
    }
    if (ev.hasField(COMPILE_SIZE)) {
      sizeValue = ev.getLong(COMPILE_SIZE);
    }
    if (ev.hasField(COMPILE_TIME)) {
      timeValue = ev.getLong(COMPILE_TIME);
    }
  }
}
