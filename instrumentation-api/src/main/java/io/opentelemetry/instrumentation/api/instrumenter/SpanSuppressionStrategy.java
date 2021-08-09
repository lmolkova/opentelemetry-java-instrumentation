/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import java.util.List;

abstract class SpanSuppressionStrategy {

  static SpanSuppressionStrategy from(List<SpanKey> spanKeys) {
    if (spanKeys.isEmpty()) {
      return NeverSuppress.INSTANCE;
    }
    return new SuppressIfSameType(spanKeys);
  }

  abstract Context storeInContext(SpanKind spanKind, Context context, Span span);

  abstract boolean shouldSuppress(SpanKind spanKind, Context parentContext);

  static final class SuppressIfSameType extends SpanSuppressionStrategy {

    private final List<SpanKey> outgoingSpanKeys;

    SuppressIfSameType(List<SpanKey> outgoingSpanKeys) {
      this.outgoingSpanKeys = outgoingSpanKeys;
    }

    @Override
    Context storeInContext(SpanKind spanKind, Context context, Span span) {
      switch (spanKind) {
        case CLIENT:
        case PRODUCER:
          for (SpanKey outgoingSpanKey : outgoingSpanKeys) {
            context = outgoingSpanKey.with(context, span);
          }
          return context;
        case SERVER:
          return SpanKey.SERVER.with(context, span);
        case CONSUMER:
          return SpanKey.CONSUMER.with(context, span);
        case INTERNAL:
          return context;
      }
      return context;
    }

    @Override
    boolean shouldSuppress(SpanKind spanKind, Context parentContext) {
      switch (spanKind) {
        case CLIENT:
        case PRODUCER:
          for (SpanKey outgoingSpanKey : outgoingSpanKeys) {
            if (outgoingSpanKey.fromContextOrNull(parentContext) == null) {
              return false;
            }
          }
          return true;
        case SERVER:
          return SpanKey.SERVER.fromContextOrNull(parentContext) != null;
        case CONSUMER:
          return SpanKey.CONSUMER.fromContextOrNull(parentContext) != null;
        case INTERNAL:
          return false;
      }
      return false;
    }
  }

  static final class NeverSuppress extends SpanSuppressionStrategy {

    private static final SpanSuppressionStrategy INSTANCE = new NeverSuppress();

    @Override
    Context storeInContext(SpanKind spanKind, Context context, Span span) {
      return context;
    }

    @Override
    boolean shouldSuppress(SpanKind spanKind, Context parentContext) {
      return false;
    }
  }
}
