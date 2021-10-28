/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics.internal.profiler;

import static io.opentelemetry.contrib.jfr.metrics.internal.Constants.ATTR_THREAD_NAME;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.contrib.jfr.metrics.internal.AbstractThreadDispatchingHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.RecordedEventHandler;
import io.opentelemetry.contrib.jfr.metrics.internal.ThreadGrouper;

public class MethodSampleHandler extends AbstractThreadDispatchingHandler {

  public MethodSampleHandler(ThreadGrouper grouper, String eventName) {
    super(grouper);
    this.eventName = eventName;
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

  private final String eventName;

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
    // Instrument build goes here...

    // var eventName = "jdk.ExecutionSample";
    var ret = new PerThreadMethodSampleHandler(threadName, eventName);
    return ret.init();
  }
}
