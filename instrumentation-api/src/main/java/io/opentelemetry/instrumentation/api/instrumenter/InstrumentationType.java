/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.ContextKey;

/**
 * An instrumentation type that distinguishes span within kind: as HTTP, DB, MESSAGING, RPC,
 * GENERIC, or any custom type. It is used to suppress multiple instrumentation layers of the same type and
 * to find and enrich spans of certain type in the current stack;
 */
public class InstrumentationType {
  private static final SuppressingSpanWrapper INTERNAL_SPAN = SuppressingSpanWrapper.neverSuppress();
  private static final SuppressingSpanWrapper SERVER_SPAN = SuppressingSpanWrapper.suppressNestedIfSameType("server");
  private static final SuppressingSpanWrapper CONSUMER_SPAN = SuppressingSpanWrapper.suppressNestedIfSameType("consumer");

  public static final InstrumentationType HTTP = new InstrumentationType("http");
  public static final InstrumentationType DB = new InstrumentationType("db");
  public static final InstrumentationType MESSAGING = new InstrumentationType("messaging");
  public static final InstrumentationType RPC = new InstrumentationType("rpc");
  public static final InstrumentationType GENERIC = new InstrumentationType(SuppressingSpanWrapper.neverSuppress());
  public static final InstrumentationType NONE = new InstrumentationType(SuppressingSpanWrapper.suppressNestedIfSameType("none"));

  public static final String ENABLE_INSTRUMENTATION_TYPE_SUPPRESSION_KEY =
      "otel.instrumentation.experimental.span-suppression-by-type";

  private final SuppressingSpanWrapper clientSpan;

  private InstrumentationType(String instrumentationType) {
    this(SuppressingSpanWrapper.suppressNestedIfSameType("client-" + instrumentationType));
  }

  private InstrumentationType(SuppressingSpanWrapper clientSpanWrapper) {
    this.clientSpan = clientSpanWrapper;
  }

  public ContextKey<Span> clientContextKey() {
    return clientSpan.getContextKey();
  }

  public ContextKey<Span> serverContextKey() {
    return SERVER_SPAN.getContextKey();
  }

  public ContextKey<Span> consumerContextKey() {
    return CONSUMER_SPAN.getContextKey();
  }

  SuppressingSpanWrapper getSpanWrapper(SpanKind spanKind) {
    switch (spanKind) {
      case CLIENT:
      case PRODUCER:
        return clientSpan;
      case SERVER:
        return SERVER_SPAN;
      case CONSUMER:
        return CONSUMER_SPAN;
      default:
        return INTERNAL_SPAN;
    }
  }
}