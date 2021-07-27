package io.opentelemetry.instrumentation.api.tracer;

public class InstrumentationType {
  private static final int NONE_VALUE = 0;
  private static final int HTTP_VALUE = 1;
  private static final int RPC_VALUE = 2;
  private static final int DB_VALUE = 3;
  private static final int MESSAGING_VALUE = 4;

  public static final InstrumentationType NONE = new InstrumentationType(NONE_VALUE);
  public static final InstrumentationType HTTP = new InstrumentationType(HTTP_VALUE);
  public static final InstrumentationType RPC = new InstrumentationType(RPC_VALUE);
  public static final InstrumentationType DB = new InstrumentationType(DB_VALUE);
  public static final InstrumentationType MESSAGING = new InstrumentationType(MESSAGING_VALUE);

  private final int value;

  public InstrumentationType create(int value) {
    switch (value) {
      case NONE_VALUE: return NONE;
      case HTTP_VALUE: return HTTP;
      case RPC_VALUE: return RPC;
      case DB_VALUE: return DB;
      case MESSAGING_VALUE: return MESSAGING;
      default: return new InstrumentationType(value);
    }
  }

  private InstrumentationType(int value) {
    this.value = value;
  }

  public boolean isNone() { return value == NONE_VALUE; }

  public int getValue(){
    return value;
  }
}
