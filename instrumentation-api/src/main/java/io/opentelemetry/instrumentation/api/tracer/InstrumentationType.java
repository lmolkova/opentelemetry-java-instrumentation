package io.opentelemetry.instrumentation.api.tracer;

public class InstrumentationType {
  static final int NONE = 0;
  static final int HTTP = 1;
  static final int RPC = 2;
  static final int DB = 3;
  static final int MESSAGING = 4;

  public static final InstrumentationType NONE_TYPE = new InstrumentationType(NONE);
  public static final InstrumentationType HTTP_TYPE = new InstrumentationType(HTTP);
  public static final InstrumentationType RPC_TYPE = new InstrumentationType(RPC);
  public static final InstrumentationType DB_TYPE = new InstrumentationType(DB);
  public static final InstrumentationType MESSAGING_TYPE = new InstrumentationType(MESSAGING);
  private final int value;

  public InstrumentationType create(int value) {
    switch (value) {
      case NONE: return NONE_TYPE;
      case HTTP: return HTTP_TYPE;
      case RPC: return RPC_TYPE;
      case DB: return DB_TYPE;
      case MESSAGING: return MESSAGING_TYPE;
      default: return new InstrumentationType(value);
    }
  }

  private InstrumentationType(int value) {
    this.value = value;
  }

  public int getValue(){
    return value;
  }
}
