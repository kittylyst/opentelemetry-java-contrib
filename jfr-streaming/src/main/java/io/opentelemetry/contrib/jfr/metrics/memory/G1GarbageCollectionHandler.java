/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.memory;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.Constants;
import io.opentelemetry.contrib.jfr.metrics.RecordedEventHandler;
import jdk.jfr.consumer.RecordedEvent;

/** This class aggregates the duration of G1 Garbage Collection JFR events */
public final class G1GarbageCollectionHandler implements RecordedEventHandler {
  public static final String EVENT_NAME = "jdk.G1GarbageCollection";
  public static final String JFR_G1_GARBAGE_COLLECTION_DURATION =
      "jfr.G1GarbageCollection.duration";
  private static final String DESCRIPTION = "G1 GC Duration";

  private final Meter otelMeter;
  private DoubleHistogram gcHistogram;

  public G1GarbageCollectionHandler(Meter otelMeter) {
    this.otelMeter = otelMeter;
  }

  public G1GarbageCollectionHandler init() {
    var builder = otelMeter.histogramBuilder(JFR_G1_GARBAGE_COLLECTION_DURATION);
    builder.setDescription(DESCRIPTION);
    builder.setUnit(Constants.MILLISECONDS);
    gcHistogram = builder.build();
    return this;
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public void accept(RecordedEvent ev) {
    // FIXME Is this a getDuration, or is it named?
    gcHistogram.record(ev.getDuration().toMillis());
  }
}
