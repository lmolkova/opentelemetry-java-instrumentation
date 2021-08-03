/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.api.config.Config;
import java.util.HashMap;
import java.util.Map;

/**
 * An instrumentation type that distinguishes span within kind: as HTTP, DB, MESSAGING, RPC,
 * GENERIC, or any custom type. It is used to suppress multiple instrumentation layers of the same type and
 * to find and enrich spans of certain type in the current stack;
 */
public class InstrumentationType {
  private static final boolean IS_ENABLED;
  private static final SuppressingSpanWrapper internalSpan;
  private static final SuppressingSpanWrapper serverSpan;
  // we want singleton for each instrumentation type so multiple instrumenters
  // that are not aware of each other would share the same instance (i.e. same context keys for suppression)
  private static final Map<String, SuppressingSpanWrapper> clientSpanWrappers;

  static {
    IS_ENABLED = Config.get()
        .getBooleanProperty(
            "otel.instrumentation.experimental.span-suppression-by-type", false);

    clientSpanWrappers = new HashMap<>();
    internalSpan = SuppressingSpanWrapper.neverSuppress();
    serverSpan = SuppressingSpanWrapper.suppressNestedIfSameType("server-");
  }

  public static final InstrumentationType HTTP = InstrumentationType.getOrCreate("http");
  public static final InstrumentationType DB = InstrumentationType.getOrCreate("db");
  public static final InstrumentationType MESSAGING = InstrumentationType.getOrCreate("messaging");
  public static final InstrumentationType RPC = InstrumentationType.getOrCreate("rpc");
  public static final InstrumentationType GENERIC = new InstrumentationType(SuppressingSpanWrapper.neverSuppress());

  public static InstrumentationType getOrCreate(String instrumentationType) {
    SuppressingSpanWrapper clientSpansWrapper = clientSpanWrappers.get(instrumentationType);
    if (clientSpansWrapper == null) {
      clientSpansWrapper = SuppressingSpanWrapper.suppressNestedIfSameType("client-" + instrumentationType);
      clientSpanWrappers.put(instrumentationType, clientSpansWrapper);
    }

    return new InstrumentationType(clientSpansWrapper);
  }

  public static boolean isEnabled() {
    return IS_ENABLED;
  }

  private final SuppressingSpanWrapper clientSpan;

  private InstrumentationType(SuppressingSpanWrapper clientSpan) {
    this.clientSpan = clientSpan;
  }

  SuppressingSpanWrapper getSpanWrapper(SpanKind spanKind) {
    switch (spanKind) {
      case SERVER:
      case CONSUMER:
        return serverSpan;
      case CLIENT:
      case PRODUCER:
        return clientSpan;
      default:
        return internalSpan;
    }
  }
}