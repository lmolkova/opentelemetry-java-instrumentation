/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates the context key for storing the current {@link SpanKind#CLIENT} span in
 * the {@link Context}.
 */
public final class ClientSpan {

  // Keeps track of the client span in a subtree corresponding to a client request.
  private static final ContextKey<Span> KEY =
      ContextKey.named("opentelemetry-traces-client-span-key");

  private static final Map<Integer, ContextKey<Span>> instrumentationKeys = new HashMap<>();
  static {
    instrumentationKeys.put(InstrumentationType.NONE, ContextKey.named("opentelemetry-traces-client-span-key-" + InstrumentationType.NONE));
    instrumentationKeys.put(InstrumentationType.HTTP, ContextKey.named("opentelemetry-traces-client-span-key-" + InstrumentationType.HTTP));
    instrumentationKeys.put(InstrumentationType.DB, ContextKey.named("opentelemetry-traces-client-span-key-" + InstrumentationType.DB));
    instrumentationKeys.put(InstrumentationType.RPC, ContextKey.named("opentelemetry-traces-client-span-key-" + InstrumentationType.RPC));
    instrumentationKeys.put(InstrumentationType.MESSAGING, ContextKey.named("opentelemetry-traces-client-span-key-" + InstrumentationType.MESSAGING));
  }

  /** Returns true when a {@link SpanKind#CLIENT} span is present in the passed {@code context}. */
  public static boolean exists(Context context) {
    return fromContextOrNull(context) != null;
  }

  public static boolean exists(Context context, InstrumentationType instrumentationType) {
    if (instrumentationType == InstrumentationType.NONE_TYPE) {
      // we want to allow nested layers of generic instrumentation
      // as it's likely to come from users
      return false;
    }

    ContextKey<Span> contextKey = instrumentationKeys.get(instrumentationType.getValue());
    if (contextKey == null) {
      return false;
    }

    return context.get(contextKey) != null;
  }

  /**
   * Returns span of type {@link SpanKind#CLIENT} from the given context or {@code null} if not
   * found.
   */
  @Nullable
  public static Span fromContextOrNull(Context context) {
    return context.get(KEY);
  }

   public static Context with(Context context, Span clientSpan, InstrumentationType instrumentationType) {

    if (instrumentationType != InstrumentationType.NONE_TYPE) {
      ContextKey<Span> typedKey = instrumentationKeys.get(instrumentationType.getValue());
      if (typedKey == null) {
        typedKey = ContextKey
            .named("opentelemetry-traces-client-span-key-" + instrumentationType.getValue());
        instrumentationKeys.put(instrumentationType.getValue(), typedKey);
      }

      context = context.with(typedKey, clientSpan);
    }

    return context.with(KEY, clientSpan);
  }

  private ClientSpan() {}
}
