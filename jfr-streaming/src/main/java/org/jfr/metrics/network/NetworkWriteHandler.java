package org.jfr.metrics.network;

// jdk.SocketWrite {
//        startTime = 20:22:57.161
//        duration = 87.4 ms
//        host = "mysql-staging-agentdb-2"
//        address = "10.31.1.15"
//        port = 3306
//        bytesWritten = 34 bytes
//        eventThread = "ActivityWriteDaemon" (javaThreadId = 252)
//        stackTrace = [
//        java.net.SocketOutputStream.socketWrite(byte[], int, int) line: 68
//        java.net.SocketOutputStream.write(byte[], int, int) line: 150
//        java.io.BufferedOutputStream.flushBuffer() line: 81
//        java.io.BufferedOutputStream.flush() line: 142
//        com.mysql.cj.protocol.a.SimplePacketSender.send(byte[], int, byte) line: 55
//        ...
//        ]
// }

import io.opentelemetry.api.metrics.Meter;
import org.jfr.metrics.AbstractThreadDispatchingHandler;
import org.jfr.metrics.RecordedEventHandler;
import org.jfr.metrics.ThreadGrouper;

public class NetworkWriteHandler extends AbstractThreadDispatchingHandler {
  public static final String EVENT_NAME = "jdk.SocketWrite";
  private final Meter otelMeter;

  public NetworkWriteHandler(Meter otelMeter, ThreadGrouper nameNormalizer) {
    super(nameNormalizer);
    this.otelMeter = otelMeter;
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }

  @Override
  public RecordedEventHandler createPerThreadSummarizer(String threadName) {
    var ret = new PerThreadNetworkWriteHandler(otelMeter, threadName);
    return ret.init();
  }
}