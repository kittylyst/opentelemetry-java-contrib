/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.memory;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.internal.Constants;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import java.util.HashMap;
import java.util.Map;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;

/** This class handles GCHeapSummary JFR events. For GC purposes they come in pairs. */
public final class GCHeapSummaryHandler implements RecordedEventHandler {
  private static final String SIMPLE_CLASS_NAME = GCHeapSummaryHandler.class.getSimpleName();
  private static final String JFR_GC_HEAP_SUMMARY_DURATION = "jfr.GCHeapSummary.duration";
  private static final String JFR_GC_HEAP_SUMMARY_HEAP_USED = "jfr.GCHeapSummary.heapUsed";
  private static final String JFR_GC_HEAP_SUMMARY_COMMITTED = "jfr.GCHeapSummary.heapCommitted";
  private static final String EVENT_NAME = "jdk.GCHeapSummary";
  private static final String BEFORE = "Before GC";
  private static final String AFTER = "After GC";
  private static final String GC_ID = "gcId";
  private static final String WHEN = "when";
  private static final String HEAP_USED = "heapUsed";
  private static final String HEAP_SPACE = "heapSpace";
  private static final String DESCRIPTION = "GC Duration";
  private static final String COMMITTED_SIZE = "committedSize";

  private final Map<Long, RecordedEvent> awaitingPairs = new HashMap<>();

  private final Meter otelMeter;
  private DoubleHistogram gcHistogram;
  private volatile long heapUsed = 0;
  private volatile long heapCommitted = 0;

  public GCHeapSummaryHandler(Meter otelMeter) {
    this.otelMeter = otelMeter;
  }

  public GCHeapSummaryHandler init() {
    var builder = otelMeter.histogramBuilder(JFR_GC_HEAP_SUMMARY_DURATION);
    builder.setDescription(DESCRIPTION);
    builder.setUnit(Constants.MILLISECONDS);
    gcHistogram = builder.build();

    otelMeter
        .upDownCounterBuilder(JFR_GC_HEAP_SUMMARY_HEAP_USED)
        .ofDoubles()
        .setUnit(Constants.KILOBYTES)
        .buildWithCallback(codm -> codm.observe(heapUsed));

    otelMeter
        .upDownCounterBuilder(JFR_GC_HEAP_SUMMARY_COMMITTED)
        .ofDoubles()
        .setUnit(Constants.KILOBYTES)
        .buildWithCallback(codm -> codm.observe(heapCommitted));

    return this;
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public void accept(RecordedEvent ev) {
    String when = null;
    if (ev.hasField(WHEN)) {
      when = ev.getString(WHEN);
    }
    if (when != null) {
      if (!(when.equals(BEFORE) || when.equals(AFTER))) {
        return;
      }
    }

    long gcId = 0;
    if (ev.hasField(GC_ID)) {
      gcId = ev.getLong(GC_ID);
    } else {
      return;
    }

    var pair = awaitingPairs.get(gcId);
    if (pair == null) {
      awaitingPairs.put(gcId, ev);
    } else {
      awaitingPairs.remove(gcId);
      if (when != null && when.equals(BEFORE)) {
        recordValues(ev, pair);
      } else { //  i.e. when.equals(AFTER)
        recordValues(pair, ev);
      }
    }
  }

  private void recordValues(RecordedEvent before, RecordedEvent after) {
    gcHistogram.record(after.getStartTime().toEpochMilli() - before.getStartTime().toEpochMilli());
    if (after.hasField(HEAP_USED)) {
      heapUsed = after.getLong(HEAP_USED);
    }
    if (after.hasField(HEAP_SPACE)) {
      after.getValue(HEAP_SPACE);
      if (after.getValue(HEAP_SPACE) instanceof RecordedObject) {
        RecordedObject ro = after.getValue(HEAP_SPACE);
        heapCommitted = ro.getLong(COMMITTED_SIZE);
      }
    }
  }
}
