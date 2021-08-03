/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.api.config.Config;

/**
 * An instrumentation type that distinguishes span within kind: as HTTP, DB, MESSAGING, RPC,
 * GENERIC, or any custom type. It is used to suppress multiple instrumentation layers of the same type and
 * to find ancestor spans of certain type to enrich (e.g. SERVER).
 */
public class InstrumentationType {
  public static final InstrumentationType HTTP = InstrumentationType.create("http");
  public static final InstrumentationType DB = InstrumentationType.create("db");
  public static final InstrumentationType MESSAGING = InstrumentationType.create("messaging");
  public static final InstrumentationType RPC = InstrumentationType.create("rpc");
  public static final InstrumentationType GENERIC = new InstrumentationType(
      SuppressableSpan.neverSuppress(),
      SuppressableSpan.suppressNestedIfSameType("server-or-consumer"));

  private static final boolean IS_ENABLED = Config.get()
      .getBooleanProperty(
          "otel.instrumentation.experimental.span-suppression-by-type", false);

  public static boolean isEnabled() {
    return IS_ENABLED;
  }

  private static final SuppressableSpan suppressableSpanInternal = SuppressableSpan.neverSuppress();
  private final SuppressableSpan suppressableSpanClient;
  private final SuppressableSpan suppressableSpanServer;

  static InstrumentationType create(String instrumentationType) {
    return new InstrumentationType(
        SuppressableSpan.suppressNestedIfSameType(instrumentationType),
        SuppressableSpan.suppressNestedIfSameType("server-or-consumer"));
  }

  private InstrumentationType(SuppressableSpan clientSpan, SuppressableSpan serverSpan) {
    this.suppressableSpanClient = clientSpan;
    this.suppressableSpanServer = serverSpan;
  }

  SuppressableSpan getSpan(SpanKind spanKind) {

    switch (spanKind) {
      case SERVER:
      case CONSUMER:
        return suppressableSpanServer;
      case CLIENT:
      case PRODUCER:
        return suppressableSpanClient;
      default:
        return suppressableSpanInternal;
    }
  }
}