package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class SuppressingSpanWrapper {
  static SuppressingSpanWrapper suppressNestedIfSameType(String type) {
    return new SuppressIfSameType(type);
  }

  static SuppressingSpanWrapper neverSuppress() {
    return new NeverSuppress();
  }

  protected final ContextKey<Span> contextKey;
  protected SuppressingSpanWrapper(ContextKey<Span> contextKey) {
    this.contextKey = contextKey;
  }

  abstract Context storeInContext(Context context, Span span);

  boolean hasMatchingSpan(Context context) {
    return fromContextOrNull(context) != null;
  }

  ContextKey<Span> getContextKey() { return contextKey; }

  @Nullable abstract Span fromContextOrNull(Context context);

  final static class SuppressIfSameType extends SuppressingSpanWrapper {

    public SuppressIfSameType(String type) {
      super(ContextKey.named("opentelemetry-traces-span-key-" + type));
    }

    @Override
    public Context storeInContext(Context context, Span span) {
      return context.with(contextKey, span);
    }

    @Override
    public @Nullable Span fromContextOrNull(Context context){
      return context.get(contextKey);
    }
  }

  final static class NeverSuppress extends SuppressingSpanWrapper {
    private final static String NEVER_SUPPRESS_KEY_NAME = "opentelemetry-traces-span-key-noop";
    public NeverSuppress() {
      super(ContextKey.named(NEVER_SUPPRESS_KEY_NAME));
    }

    @Override
    public Context storeInContext(Context context, Span span) {
      return context;
    }

    @Override
    public @Nullable Span fromContextOrNull(Context context) { return null; }

    @Override
    ContextKey<Span> getContextKey() {
      // reusing context key for never-suppress span would cause suppression
      // in case it's being requested multiple times we'll return a new instance
      return ContextKey.named(NEVER_SUPPRESS_KEY_NAME);
    }
  }
}

