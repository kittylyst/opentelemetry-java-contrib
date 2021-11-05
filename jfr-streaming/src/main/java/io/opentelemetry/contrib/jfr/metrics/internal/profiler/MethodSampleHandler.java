/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.profiler;

import static io.opentelemetry.contrib.jfr.metrics.internal.Constants.ATTR_THREAD_NAME;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.profiler.ExecutionProfile;
import io.opentelemetry.contrib.jfr.metrics.internal.AbstractThreadDispatchingHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.ThreadGrouper;

public class MethodSampleHandler extends AbstractThreadDispatchingHandler {
  private static final String METRIC_NAME = "runtime.jvm.cpu.longlock.time";
  private static final String DESCRIPTION = "";

  private final String eventName;
  private final ExecutionProfile otelProfile;

  public MethodSampleHandler(
      ExecutionProfile otelProfile, ThreadGrouper grouper, String eventName) {
    super(grouper);
    this.eventName = eventName;
    this.otelProfile = otelProfile;
  }

  public enum Event {
    JAVA("jdk.ExecutionSample"),
    NATIVE("jdk.NativeMethodSample");

    private String name;

    Event(String s) {
      name = s;
    }

    public String getName() {
      return name;
    }
  }

  public MethodSampleHandler init() {
    return this;
  }

  @Override
  public String getEventName() {
    return eventName;
  }

  @Override
  public RecordedEventHandler createPerThreadSummarizer(String threadName) {
    var attr = Attributes.of(ATTR_THREAD_NAME, threadName);
    var builder = otelProfile.flamegraphBuilder(METRIC_NAME);
    builder.setDescription(DESCRIPTION);
    var flamegraph = builder.build().bind(attr);

    var ret = new PerThreadMethodSampleHandler(flamegraph, threadName, eventName);
    return ret.init();
  }
}
