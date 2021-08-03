package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import org.checkerframework.checker.nullness.qual.Nullable;

interface SuppressingSpanWrapper {
  static SuppressingSpanWrapper suppressNestedIfSameType(String type) {
    return new SuppressIfSameType(type);
  }

  static SuppressingSpanWrapper neverSuppress() {
    return new NeverSuppress();
  }

  Context setInContext(Context context, Span span);

  boolean hasMatchingSpan(Context context);

  @Nullable Span getMatchingSpanOrNull(Context context);

  final class SuppressIfSameType implements SuppressingSpanWrapper {
    private final ContextKey<Span> contextKey;

    public SuppressIfSameType(String type) {
      this.contextKey = ContextKey.named("opentelemetry-traces-span-key-" + type);
    }

    @Override
    public Context setInContext(Context context, Span span) {
      return context.with(contextKey, span);
    }

    @Override
    public @Nullable Span getMatchingSpanOrNull(Context context){
      return context.get(contextKey);
    }

    @Override
    public boolean hasMatchingSpan(Context context) {
      return context.get(contextKey) != null;
    }
  }

  final class NeverSuppress implements SuppressingSpanWrapper {
    @Override
    public Context setInContext(Context context, Span span) {
      return context;
    }

    @Override
    public @Nullable Span getMatchingSpanOrNull(Context context) {
      return null;
    }

    @Override
    public boolean hasMatchingSpan(Context context) {
      return false;
    }
  }
}

