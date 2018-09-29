package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static java.util.Collections.unmodifiableCollection;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class GomConverters<C extends DataLoaderRegistryGetter> {

    @RequiredArgsConstructor(access = PRIVATE)
    @EqualsAndHashCode
    private static final class Converter<T, C extends DataLoaderRegistryGetter> implements Comparable<Converter<?, C>> {

        private final Class<?> clazz;

        @EqualsAndHashCode.Exclude
        private final BiFunction<T, C, CompletableFuture<?>> function;

        @SuppressWarnings("unchecked")
        private <R> CompletableFuture<R> convert(Object value, C context) {
            return (CompletableFuture<R>) function.apply((T) value, context);
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
    public static final class Builder<C extends DataLoaderRegistryGetter> {

        private final Set<Converter<?, C>> converters = new HashSet<>();

        public <T> Builder<C> with(Class<T> clazz, BiFunction<T, C, CompletableFuture<?>> converter) {
            converters.add(new Converter<>(clazz, converter));
            return this;
        }

        public GomConverters<C> build() {
            return new GomConverters<>(unmodifiableCollection(converters));
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
                .orElse(CompletableFuture.completedFuture(value));
    }

    public static <C extends DataLoaderRegistryGetter> Builder<C> newGomConverters() {
        return new Builder<>();
    }

}
