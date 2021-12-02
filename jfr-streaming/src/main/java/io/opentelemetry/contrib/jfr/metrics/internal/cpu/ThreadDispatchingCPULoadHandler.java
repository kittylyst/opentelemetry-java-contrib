/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.cpu;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.contrib.jfr.metrics.internal.AbstractThreadDispatchingHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.ThreadGrouper;

public final class ThreadDispatchingCPULoadHandler extends AbstractThreadDispatchingHandler {
  static final String EVENT_NAME = "jdk.ThreadCPULoad";
  private static final String METRIC_NAME = "runtime.jvm.cpu.longlock.time";
  private static final String DESCRIPTION = "Long lock times";

  public ThreadDispatchingCPULoadHandler(ThreadGrouper grouper) {
    super(grouper);
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public void initializeMeter(Meter meter) {}

  @Override
  public RecordedEventHandler createPerThreadSummarizer(String threadName) {
    return null;
  }
}
