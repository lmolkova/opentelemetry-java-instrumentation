/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

public class InstrumentationTypeTests {

  private static final Span SPAN = Span.getInvalid();

  @ParameterizedTest
  @EnumSource(
      value = SpanKind.class,
      names = {"SERVER", "CONSUMER"})
  public void serverSpan_sameEverywhere(SpanKind kind) {
    Context context =
        InstrumentationType.NONE.spanWrapper(kind).storeInContext(Context.root(), SPAN);

    allInstrumentationTypes()
        .forEach(type -> assertThat(type.spanWrapper(kind).hasMatchingSpan(context)).isTrue());
    allInstrumentationTypes()
        .forEach(
            type -> assertThat(type.spanWrapper(kind).fromContextOrNull(context)).isSameAs(SPAN));

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CLIENT).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CLIENT).fromContextOrNull(context)).isNull());

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.PRODUCER).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.PRODUCER).fromContextOrNull(context))
                    .isNull());

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.INTERNAL).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.INTERNAL).fromContextOrNull(context))
                    .isNull());
  }

  @Test
  public void serverSpan_differentThanConsumer() {
    Context contextServer =
        InstrumentationType.NONE.spanWrapper(SpanKind.SERVER).storeInContext(Context.root(), SPAN);
    Context contextConsumer =
        InstrumentationType.NONE
            .spanWrapper(SpanKind.CONSUMER)
            .storeInContext(Context.root(), SPAN);

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CONSUMER).hasMatchingSpan(contextServer))
                    .isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CONSUMER).fromContextOrNull(contextServer))
                    .isNull());

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.SERVER).hasMatchingSpan(contextConsumer))
                    .isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.SERVER).fromContextOrNull(contextConsumer))
                    .isNull());
  }

  @ParameterizedTest
  @MethodSource("allInstrumentationTypes")
  public void clientSpan_differentForAllTyps(InstrumentationType instrumentationType) {
    Context context =
        instrumentationType.spanWrapper(SpanKind.CLIENT).storeInContext(Context.root(), SPAN);

    if (instrumentationType != InstrumentationType.GENERIC) {
      assertThat(instrumentationType.spanWrapper(SpanKind.CLIENT).hasMatchingSpan(context))
          .isTrue();
      assertThat(instrumentationType.spanWrapper(SpanKind.CLIENT).fromContextOrNull(context))
          .isNotNull();
    }

    allInstrumentationTypes()
        .filter(type -> type != instrumentationType)
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CLIENT).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .filter(type -> type != instrumentationType)
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.CLIENT).fromContextOrNull(context)).isNull());
  }

  @ParameterizedTest
  @MethodSource("allInstrumentationTypes")
  public void client_sameAsProducer(InstrumentationType instrumentationType) {
    Context contextClient =
        instrumentationType.spanWrapper(SpanKind.CLIENT).storeInContext(Context.root(), SPAN);
    Context contextProducer =
        instrumentationType.spanWrapper(SpanKind.PRODUCER).storeInContext(Context.root(), SPAN);

    if (instrumentationType == InstrumentationType.GENERIC) {
      return;
    }

    assertThat(instrumentationType.spanWrapper(SpanKind.CLIENT).hasMatchingSpan(contextProducer))
        .isTrue();
    assertThat(instrumentationType.spanWrapper(SpanKind.CLIENT).fromContextOrNull(contextProducer))
        .isSameAs(SPAN);
    assertThat(instrumentationType.spanWrapper(SpanKind.PRODUCER).hasMatchingSpan(contextClient))
        .isTrue();
    assertThat(instrumentationType.spanWrapper(SpanKind.PRODUCER).fromContextOrNull(contextClient))
        .isSameAs(SPAN);
  }

  @ParameterizedTest
  @EnumSource(
      value = SpanKind.class,
      names = {"CLIENT", "PRODUCER"})
  public void clientSpan_GENERIC_neverHasMatchingSpan(SpanKind kind) {
    Context context =
        InstrumentationType.GENERIC.spanWrapper(kind).storeInContext(Context.root(), SPAN);

    allInstrumentationTypes()
        .forEach(type -> assertThat(type.spanWrapper(kind).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .forEach(type -> assertThat(type.spanWrapper(kind).fromContextOrNull(context)).isNull());
  }

  @Test
  public void internalSpan_neverHasMatchingSpan() {
    Context context =
        InstrumentationType.NONE
            .spanWrapper(SpanKind.INTERNAL)
            .storeInContext(Context.root(), SPAN);

    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.INTERNAL).hasMatchingSpan(context)).isFalse());
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.spanWrapper(SpanKind.INTERNAL).fromContextOrNull(context))
                    .isNull());
  }

  @Test
  public void serverContextKey() {
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.serverContextKey())
                    .isSameAs(InstrumentationType.NONE.serverContextKey()));
    allInstrumentationTypes()
        .forEach(
            type ->
                assertThat(type.consumerContextKey())
                    .isSameAs(InstrumentationType.NONE.consumerContextKey()));
    assertThat(InstrumentationType.NONE.consumerContextKey())
        .isNotSameAs(InstrumentationType.NONE.serverContextKey());
  }

  @Test
  public void clientContextKey() {
    allInstrumentationTypes()
        .forEach(
            type1 ->
                allInstrumentationTypes()
                    .forEach(
                        type2 -> {
                          if (type1 == type2 && type1 != InstrumentationType.GENERIC) {
                            assertThat(type1.clientContextKey()).isSameAs(type2.clientContextKey());
                          } else {
                            assertThat(type1.clientContextKey())
                                .isNotSameAs(type2.clientContextKey());
                          }
                        }));
  }

  private static Stream<InstrumentationType> allInstrumentationTypes() {
    return Stream.of(
        InstrumentationType.NONE,
        InstrumentationType.HTTP,
        InstrumentationType.DB,
        InstrumentationType.RPC,
        InstrumentationType.GENERIC);
  }
}
