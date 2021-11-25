/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.jit;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import jdk.jfr.consumer.RecordedEvent;

public final class CompilerStatisticsHandler implements RecordedEventHandler {
  private static final String EVENT_NAME = "jdk.CompilerStatistics";
  private static final String METRIC_NAME = "runtime.jvm.compiler.";

  private final Meter otelMeter;

  public CompilerStatisticsHandler(Meter otelMeter) {
    this.otelMeter = otelMeter;
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public void accept(RecordedEvent recordedEvent) {}
}
