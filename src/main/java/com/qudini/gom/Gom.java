package com.qudini.gom;

import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.BinaryOperator;

import static com.qudini.gom.Converters.newConverters;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Gom {

    public static final class Builder {

        private Collection<Object> resolvers;
        private Converters converters;

        private Builder() {
            this.resolvers = new HashSet<>();
            this.converters = newConverters(Object.class).build();
        }

        @Nonnull
        public Builder resolvers(Collection<Object> resolvers) {
            this.resolvers = resolvers;
            return this;
        }

        @Nonnull
        public Builder converters(Converters converters) {
            this.converters = converters;
            return this;
        }

        @Nonnull
        public Gom build() {
            ResolverInspection inspection = ResolverInspection.inspect(resolvers, converters);
            return new Gom(inspection.getFieldWirings(), inspection.getDataLoaderRegistrars());
        }

    }

    private final Collection<FieldWiring> fieldWirings;

    private final Collection<DataLoaderRegistrar> dataLoaderRegistrars;

    public void decorateRuntimeWiringBuilder(RuntimeWiring.Builder builder) {
        fieldWirings
                .stream()
                .collect(groupingBy(FieldWiring::getTypeName))
                .entrySet()
                .stream()
                .map(entry -> entry
                        .getValue()
                        .stream()
                        .reduce(
                                newTypeWiring(entry.getKey()),
                                (typeWiring, fieldWiring) -> typeWiring.dataFetcher(fieldWiring.getFieldName(), fieldWiring.getDataFetcher()),
                                fail()
                        ))
                .forEach(builder::type);
    }

    public void decorateDataLoaderRegistry(DataLoaderRegistry registry) {
        dataLoaderRegistrars.forEach(registrar -> registrar.register(registry));
    }

    @Nonnull
    public static Builder newGom() {
        return new Builder();
    }

    private static <T> BinaryOperator<T> fail() {
        return (x, y) -> {
            throw new IllegalStateException("This combiner shouldn't have been called");
        };
    }

}
