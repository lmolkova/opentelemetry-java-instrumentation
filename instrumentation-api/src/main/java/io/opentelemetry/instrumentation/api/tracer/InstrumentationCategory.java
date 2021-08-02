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
 * An instrumentation category that distinguishes span within kind: as HTTP, DB, MESSAGING, RPC,
 * CUSTOM, or any other type. It is used to suppress multiple instrumentation layers of the same category and
 * to find ancestor spans of certain categories to enrich.
 */
public final class InstrumentationCategory {
  public static final InstrumentationCategory HTTP = new InstrumentationCategory("http");
  public static final InstrumentationCategory DB = new InstrumentationCategory("db");
  public static final InstrumentationCategory MESSAGING = new InstrumentationCategory("messaging");
  public static final InstrumentationCategory RPC = new InstrumentationCategory("rpc");
  public static final InstrumentationCategory CUSTOM = new InstrumentationCategory("custom");

  public static final String ENABLE_INSTRUMENTATION_CATEGORY_SUPPRESSION_KEY = "otel.instrumentation.experimental.span-suppression-by-type";

  private final String category;
  private final ContextKey<Span> contextKey;

  public static boolean isEnabled() {
    return Config.get()
        .getBooleanProperty(
            ENABLE_INSTRUMENTATION_CATEGORY_SUPPRESSION_KEY, false);
  }

  public InstrumentationCategory(String category) {
    this.category = category;
    this.contextKey = ContextKey.named("opentelemetry-traces-client-span-key-" + category);
  }

  public Context setSpan(Span span, Context context) {
    if (CUSTOM.category.equals(category)) {
      // custom instrumentation should never be suppressed
      return context;
    }

    return context.with(contextKey, span);
  }

  public @Nullable Span getMatchingSpan(Context context) {
    return context.get(contextKey);
  }
}
