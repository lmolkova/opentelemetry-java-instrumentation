/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.instrumentation.api.config.Config;

/**
 * An instrumentation type that distinguishes spans within CLIENT or
 * PRODUCER kind as HTTP, DB, MESSAGING, RPC or GENERIC.
 * It is used to suppress multiple instrumentation layers of the same type and
 * to find and enrich spans of certain type in the current context.
 *
 * - CLIENT and PRODUCER nested spans are suppressed based on their type
 *   - GENERIC type spans are never suppressed.
 * - INTERNAL spans are never suppressed
 * - SERVER or CONSUMER nested spans are suppressed regardless of type.
 */
public class InstrumentationType {
  /**
   * Represents HTTP instrumentation type. Client or producer nested HTTP spans are suppressed.
   */
  public static final InstrumentationType HTTP = new InstrumentationType("http");

  /**
   * Represents DB instrumentation type. Client nested DB spans are suppressed.
   */
  public static final InstrumentationType DB = new InstrumentationType("db");

  /**
   * Represents MESSAGING instrumentation type. Client or producer nested MESSAGING spans are suppressed.
   */
  public static final InstrumentationType MESSAGING = new InstrumentationType("messaging");

  /**
   * Represents RPC instrumentation type. Client or producer nested RPC spans are suppressed.
   */
  public static final InstrumentationType RPC = new InstrumentationType("rpc");

  /**
   * Represents GENERIC instrumentation type. GENERIC spans are never suppressed. Used by default if
   * instrumentation type is enabled, and {@link InstrumenterBuilder} could not detect specific instrumentation type.
   */
  public static final InstrumentationType GENERIC = new InstrumentationType(SuppressingSpanWrapper.neverSuppress());

  /**
   * Represents disabled instrumentation type. Used when {@link IS_ENABLED} is false.
   */
  public static final InstrumentationType NONE = new InstrumentationType(SuppressingSpanWrapper.suppressNestedIfSameType("none"));

  /**
   * Instrumentation type suppression configuration property key.
   */
  static final boolean IS_ENABLED = Config.get().getBooleanProperty("otel.instrumentation.experimental.span-suppression-by-type", false);

  private static final SuppressingSpanWrapper INTERNAL_SPAN = SuppressingSpanWrapper.neverSuppress();
  private static final SuppressingSpanWrapper SERVER_SPAN = SuppressingSpanWrapper.suppressNestedIfSameType("server");
  private static final SuppressingSpanWrapper CONSUMER_SPAN = SuppressingSpanWrapper.suppressNestedIfSameType("consumer");

  private final SuppressingSpanWrapper clientSpan;

  private InstrumentationType(String instrumentationType) {
    this(SuppressingSpanWrapper.suppressNestedIfSameType("client-" + instrumentationType));
  }

  private InstrumentationType(SuppressingSpanWrapper clientSpanWrapper) {
    this.clientSpan = clientSpanWrapper;
  }

  /**
   * Returns CLIENT span context key for the type. Use it to unambiguously identify span of certain type
   * to enrich.
   *
   * @return {@link ContextKey} for CLIENT span of given type.
   */
  public ContextKey<Span> clientContextKey() {
    return clientSpan.getContextKey();
  }

  /**
   * Returns SERVER span context key. Use it to enrich SERVER spans.
   *
   * @return {@link ContextKey} for SERVER span.
   */
  public ContextKey<Span> serverContextKey() {
    return SERVER_SPAN.getContextKey();
  }

  /**
   * Returns CONSUMER span context key. Use it to enrich CONSUMER spans.
   *
   * @return {@link ContextKey} for CONSUMER span.
   */
  public ContextKey<Span> consumerContextKey() {
    return CONSUMER_SPAN.getContextKey();
  }

  /**
   * Returns spans wrapper for given kind.
   *
   * @param spanKind to get wrapper for.
   * @return {@link SuppressingSpanWrapper} instance.
   */
  SuppressingSpanWrapper spanWrapper(SpanKind spanKind) {
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