/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;

abstract class SpanSuppressionStrategy {

  static SpanSuppressionStrategy from(SpanKey spanKey) {
    return new SuppressIfSameType(spanKey);
  }

  static SpanSuppressionStrategy neverSuppress() {
    return NeverSuppress.INSTANCE;
  }

  abstract Context storeInContext(SpanKind spanKind, Context context, Span span);

  abstract boolean shouldSuppress(SpanKind spanKind, Context parentContext);

  static final class SuppressIfSameType extends SpanSuppressionStrategy {

    private final SpanKey outgoingSpanKey;

    SuppressIfSameType(SpanKey outgoingSpanKey) {
      this.outgoingSpanKey = outgoingSpanKey;
    }

    @Override
    Context storeInContext(SpanKind spanKind, Context context, Span span) {
      switch (spanKind) {
        case CLIENT:
        case PRODUCER:
          return outgoingSpanKey.with(context, span);
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
          return outgoingSpanKey.fromContextOrNull(parentContext) != null;
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
