package com.qudini.gom;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Converters {

    @RequiredArgsConstructor(access = PRIVATE)
    @EqualsAndHashCode
    private static final class Converter implements Comparable<Converter> {

        private final Class<?> clazz;

        @EqualsAndHashCode.Exclude
        private final BiFunction<Object, Object, Object> function;

        private Object convert(Object value, Object context) {
            return function.apply(value, context);
        }

        @Override
        public int compareTo(Converter o) {
            final int result;
            if (clazz.equals(o.clazz)) {
                result = 0;
            } else if (clazz.isAssignableFrom(o.clazz)) {
                result = 1;
            } else if (o.clazz.isAssignableFrom(clazz)) {
                result = -1;
            } else {
                throw new IllegalStateException("Cannot compare classes that aren't from the same hierarchy");
            }
            return result;
        }

    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class Builder<C> {

        private final Set<Converter> converters = new HashSet<>();

        @Nonnull
        public <T> Builder<C> converter(Class<T> clazz, BiFunction<T, C, Object> converter) {
            converters.add(new Converter(clazz, (BiFunction<Object, Object, Object>) converter));
            return this;
        }

        @Nonnull
        public Converters build() {
            return new Converters(unmodifiableSet(converters));
        }

    }

    private final Collection<Converter> converters;

    CompletableFuture<Object> convert(Object value, Object context) {
        return value instanceof CompletableFuture
                ? (CompletableFuture<Object>) value
                : converters
                .stream()
                .filter(converter -> converter.clazz.isInstance(value))
                .sorted()
                .findFirst()
                .map(converter -> converter.convert(value, context))
                .map(object -> convert(object, context))
                .orElse(completedFuture(value));
    }

    @Nonnull
    public static <C> Builder<C> newConverters(Class<C> contextClass) {
        return new Builder<>();
    }

}
