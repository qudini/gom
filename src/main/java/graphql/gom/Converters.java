package graphql.gom;

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
public final class Converters<C> {

    @RequiredArgsConstructor(access = PRIVATE)
    @EqualsAndHashCode
    private static final class Converter<T, C> implements Comparable<Converter<?, C>> {

        private final Class<?> clazz;

        @EqualsAndHashCode.Exclude
        private final BiFunction<T, C, ?> function;

        @SuppressWarnings("unchecked")
        private Object convert(Object value, C context) {
            return function.apply((T) value, context);
        }

        @Override
        public int compareTo(Converter<?, C> o) {
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

        private final Set<Converter<?, C>> converters = new HashSet<>();

        @Nonnull
        public <T> Builder<C> converter(Class<T> clazz, BiFunction<T, C, ?> converter) {
            converters.add(new Converter<>(clazz, converter));
            return this;
        }

        @Nonnull
        public Converters<C> build() {
            return new Converters<>(unmodifiableSet(converters));
        }

    }

    private final Collection<Converter<?, C>> converters;

    @SuppressWarnings("unchecked")
    <T, R> CompletableFuture<R> convert(T value, C context) {
        return value instanceof CompletableFuture
                ? (CompletableFuture<R>) value
                : (CompletableFuture<R>) converters
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
