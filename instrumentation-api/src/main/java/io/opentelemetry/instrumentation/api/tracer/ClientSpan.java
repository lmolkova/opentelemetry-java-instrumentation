/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class encapsulates the context key for storing the current {@link SpanKind#CLIENT} span in
 * the {@link Context}.
 */
public final class ClientSpan {
  // Keeps track of the client span in a subtree corresponding to a client request.
  private static final ContextKey<Span> KEY =
      ContextKey.named("opentelemetry-traces-client-span-key");

  private static final String TYPED_CONTEXT_KEY_PREFIX = "opentelemetry-traces-client-span-key-";
  private static final Map<InstrumentationType, ContextKey<Span>> instrumentationKeys =
      new HashMap<>();

  static {
    instrumentationKeys.put(InstrumentationType.HTTP, ContextKey.named(TYPED_CONTEXT_KEY_PREFIX + InstrumentationType.HTTP));
    instrumentationKeys.put(InstrumentationType.DB, ContextKey.named(TYPED_CONTEXT_KEY_PREFIX + InstrumentationType.DB));
    instrumentationKeys.put(InstrumentationType.RPC, ContextKey.named(TYPED_CONTEXT_KEY_PREFIX + InstrumentationType.RPC));
    instrumentationKeys.put(
        InstrumentationType.MESSAGING, ContextKey.named(TYPED_CONTEXT_KEY_PREFIX + InstrumentationType.MESSAGING));
  }

  /** Returns true when a {@link SpanKind#CLIENT} span is present in the passed {@code context}. */
  /*public static boolean exists(Context context) {
    return fromContextOrNull(context) != null;
  }*/

  public static boolean exists(Context context, @Nullable InstrumentationType instrumentationType) {
    if (instrumentationType == null) {
      // keep current behavior - suppress all nested client spans
      return fromContextOrNull(context) != null;
    }

    if (instrumentationType.isNone()) {
      // we allow nested layers of generic instrumentation as it's likely to come from users
      return false;
    }

    ContextKey<Span> contextKey = instrumentationKeys.get(instrumentationType);
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
  static Span fromContextOrNull(Context context) {
    return context.get(KEY);
  }

  public static Context with(
      Context context, Span clientSpan, @Nullable InstrumentationType instrumentationType) {

    if (instrumentationType != null && !instrumentationType.isNone()) {
      ContextKey<Span> typedKey = instrumentationKeys.get(instrumentationType);
      if (typedKey == null) {
        typedKey = ContextKey.named(TYPED_CONTEXT_KEY_PREFIX + instrumentationType);
        instrumentationKeys.put(instrumentationType, typedKey);
      }

      context = context.with(typedKey, clientSpan);
    }

    return context.with(KEY, clientSpan);
  }

  private ClientSpan() {}
}
