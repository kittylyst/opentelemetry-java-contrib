package org.jfr.metrics.network;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import jdk.jfr.consumer.RecordedEvent;
import org.jfr.metrics.RecordedEventHandler;

public class PerThreadNetworkWriteHandler implements RecordedEventHandler {
  public static final String SIMPLE_CLASS_NAME =
      PerThreadNetworkWriteHandler.class.getSimpleName();
  public static final String BYTES_WRITTEN = "bytesWritten";
  public static final String THREAD_NAME = "thread.name";
  public static final String JFR_SOCKET_WRITE_BYTES_WRITTEN = "jfr.SocketWrite.bytesWritten";
  public static final String JFR_SOCKET_WRITE_DURATION = "jfr.SocketWrite.duration";

  private final String threadName;
  private final Meter otelMeter;

  private BoundDoubleHistogram bytesHistogram;
  private BoundDoubleHistogram durationHistogram;

  public PerThreadNetworkWriteHandler(Meter otelMeter, String threadName) {
    this.threadName = threadName;
    this.otelMeter = otelMeter;
  }

  public PerThreadNetworkWriteHandler init() {
    var attr = Attributes.of(AttributeKey.stringKey(THREAD_NAME), threadName);

    var builder = otelMeter.histogramBuilder(JFR_SOCKET_WRITE_BYTES_WRITTEN);
    builder.setDescription("Bytes Written");
    builder.setUnit("KB");
    bytesHistogram = builder.build().bind(attr);

    builder = otelMeter.histogramBuilder(JFR_SOCKET_WRITE_DURATION);
    builder.setDescription("Write Duration");
    builder.setUnit("ms");
    durationHistogram = builder.build().bind(attr);

    return this;
  }

  @Override
  public String getEventName() {
    return NetworkWriteHandler.EVENT_NAME;
  }

  @Override
  public void accept(RecordedEvent ev) {
    bytesHistogram.record(ev.getLong(BYTES_WRITTEN));
    durationHistogram.record(ev.getDuration().toMillis());
  }

}