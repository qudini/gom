package com.qudini.gom;

import graphql.GraphQLContext;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
        private final BiFunction<Object, GraphQLContext, Object> function;

        private Object convert(Object value, GraphQLContext context) {
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
    public static final class Builder {

        private final Set<Converter> converters = new HashSet<>();

        @Nonnull
        public <T> Builder converter(Class<T> clazz, BiFunction<T, GraphQLContext, Object> converter) {
            converters.add(new Converter(clazz, (BiFunction<Object, GraphQLContext, Object>) converter));
            return this;
        }

        @Nonnull
        public Converters build() {
            return new Converters(unmodifiableSet(converters));
        }

    }

    private final Collection<Converter> converters;

    private final Map<Class<?>, Optional<Converter>> cache = new ConcurrentHashMap<>();

    CompletableFuture<Object> convert(@Nullable Object value, GraphQLContext context) {
        if (value == null) {
            return completedFuture(null);
        } else if (value instanceof CompletableFuture) {
            return (CompletableFuture<Object>) value;
        } else {
            return cache
                    .computeIfAbsent(value.getClass(), this::findConverter)
                    .map(converter -> convert(converter.convert(value, context), context))
                    .orElseGet(() -> completedFuture(value));
        }
    }

    private Optional<Converter> findConverter(Class<?> from) {
        return converters
                .stream()
                .filter(converter -> converter.clazz.isAssignableFrom(from))
                .sorted()
                .findFirst();
    }

    @Nonnull
    public static Builder newConverters() {
        return new Builder();
    }

}
