package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class SuppressableSpan {
  private static final SuppressableSpan NEVER_SUPPRESS = new NeverSuppress();

  protected ContextKey<Span> contextKey;

  protected SuppressableSpan(ContextKey<Span> contextKey) {
    this.contextKey = contextKey;
  }

  public static SuppressableSpan suppressNestedIfSameType(String type) {
    return new SuppressIfSameType(type);
  }

  public static SuppressableSpan neverSuppress() {
    return NEVER_SUPPRESS;
  }

  public Context setSpanInContext(Span span, Context context) {
    return context.with(contextKey, span);
  }

  public @Nullable Span getMatchingSpanOrNull(Context context) {
    return context.get(contextKey);
  }

  public boolean hasMatchingSpan(Context context) {
    return context.get(contextKey) != null;
  }

  private static final class SuppressIfSameType extends SuppressableSpan {
    public SuppressIfSameType(String instrumentationType) {
      super(ContextKey.named("opentelemetry-traces-span-key-client-" + instrumentationType));
    }
  }

  private final static class SuppressNested extends SuppressableSpan {
    public SuppressNested() {
      super(ContextKey.named("opentelemetry-traces-span-key-single"));
    }
  }

  private final static class NeverSuppress extends SuppressableSpan {
    public NeverSuppress() {
      super(ContextKey.named("opentelemetry-traces-span-key-generic"));
    }

    @Override
    public Context setSpanInContext(Span span, Context context) {
      return context;
    }

    @Override
    public @Nullable Span getMatchingSpanOrNull(Context context) {
      return null;
    }
  }
}
