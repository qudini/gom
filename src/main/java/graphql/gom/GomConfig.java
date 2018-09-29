package graphql.gom;

import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import java.util.Collection;

import static graphql.gom.utils.Reductions.fail;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.stream.Collectors.groupingBy;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class GomConfig {

    private final Collection<FieldWiring> fieldWirings;

    private final Collection<DataLoaderRegistrar> dataLoaderRegistrars;

    public static <C extends DataLoaderRegistryGetter> GomConfig newGomConfig(Collection<Object> resolvers, GomConverters<C> converters) {
        GomResolverInspection<C> inspection = GomResolverInspection.inspect(resolvers, converters);
        return new GomConfig(inspection.getFieldWirings(), inspection.getDataLoaderRegistrars());
    }

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

}
