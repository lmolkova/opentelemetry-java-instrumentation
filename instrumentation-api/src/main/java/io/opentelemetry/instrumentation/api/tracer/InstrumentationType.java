/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.instrumentation.api.config.Config;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An instrumentation type that distinguishes span within kind: as HTTP, DB, MESSAGING, RPC,
 * GENERIC, or any custom type. It is used to suppress multiple instrumentation layers of the same type and
 * to find ancestor spans of certain type to enrich (e.g. HTTP SERVER).
 */
public final class InstrumentationType {
  public static final InstrumentationType HTTP = new InstrumentationType("http");
  public static final InstrumentationType DB = new InstrumentationType("db");
  public static final InstrumentationType MESSAGING = new InstrumentationType("messaging");
  public static final InstrumentationType RPC = new InstrumentationType("rpc");
  public static final InstrumentationType NONE = new InstrumentationType("any");
  public static final InstrumentationType GENERIC = new InstrumentationType("generic");

  private static final boolean IS_ENABLED = Config.get()
      .getBooleanProperty(
          "otel.instrumentation.experimental.span-suppression-by-type", false);

  private final String type;
  private final ContextKey<Span> contextKey;

  public static boolean isEnabled() {
    return IS_ENABLED;
  }

  public InstrumentationType(String instrumentationType) {
    this.type = instrumentationType;
    // TODO kind
    this.contextKey = ContextKey.named("opentelemetry-traces-client-span-key-" + instrumentationType);
  }

  public Context setSpan(Span span, Context context) {
    return context.with(contextKey, span);
  }

  public @Nullable Span getMatchingSpanOrNull(Context context) {
    return context.get(contextKey);
  }

  public boolean hasMatchingSpan(Context context) {
    if (GENERIC.type.equals(type)) {
      // custom instrumentation should never be suppressed
      return false;
    }

    return context.get(contextKey) != null;
  }
}
