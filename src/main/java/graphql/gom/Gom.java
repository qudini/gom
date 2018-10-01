package graphql.gom;

import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.BinaryOperator;

import static graphql.gom.Converters.newConverters;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Gom {

    public static final class Builder<C extends DataLoaderRegistryGetter> {

        private Collection<Object> resolvers;
        private Converters<C> converters;

        private Builder(Class<C> contextClass) {
            this.resolvers = new HashSet<>();
            this.converters = newConverters(contextClass).build();
        }

        public Builder<C> resolvers(Collection<Object> resolvers) {
            this.resolvers = resolvers;
            return this;
        }

        public Builder<C> converters(Converters<C> converters) {
            this.converters = converters;
            return this;
        }

        public Gom build() {
            ResolverInspection<C> inspection = ResolverInspection.inspect(resolvers, converters);
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

    public static <C extends DataLoaderRegistryGetter> Builder<C> newGom(Class<C> contextClass) {
        return new Builder<>(contextClass);
    }

    private static <T> BinaryOperator<T> fail() {
        return (x, y) -> {
            throw new IllegalStateException("This combiner shouldn't have been called");
        };
    }

}
