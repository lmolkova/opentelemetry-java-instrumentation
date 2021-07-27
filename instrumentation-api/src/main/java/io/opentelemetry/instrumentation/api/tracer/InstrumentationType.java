package io.opentelemetry.instrumentation.api.tracer;

public class InstrumentationType {

  public static final InstrumentationType NONE = new InstrumentationType("none");
  public static final InstrumentationType HTTP = new InstrumentationType("http");
  public static final InstrumentationType RPC = new InstrumentationType("rpc");
  public static final InstrumentationType DB = new InstrumentationType("db");
  public static final InstrumentationType MESSAGING = new InstrumentationType("messaging");

  private final String name;

  // each call to this method creates a unique instrumentation type
  // name is only used for debugging
  public static InstrumentationType create(String name) {
    return new InstrumentationType(name);
  }

  private InstrumentationType(String name) {
    this.name = name;
  }

  public boolean isNone() {
    return this == NONE;
  }

  @Override
  public String toString() {
    return name;
  }
}
