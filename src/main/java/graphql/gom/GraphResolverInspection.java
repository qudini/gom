package graphql.gom;

import graphql.gom.utils.FutureParalleliser;
import graphql.gom.utils.MapMerger;
import graphql.gom.utils.MethodInvoker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static graphql.gom.utils.MapEntryMapper.entry;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Getter(PACKAGE)
final class GraphResolverInspection {

    private final Set<GraphFieldWiring> fieldWirings = new HashSet<>();

    private final Set<DataLoaderRegistrar> dataLoaderRegistrars = new HashSet<>();

    private <R> R invoke(Method method, Object instance, Object source, Map<String, Object> arguments) {
        final Object returnedValue;
        switch (method.getParameterCount()) {
            case 0:
                returnedValue = MethodInvoker.invoke(method, instance);
                break;
            case 1:
                returnedValue = source == null
                        ? MethodInvoker.invoke(method, instance, arguments)
                        : MethodInvoker.invoke(method, instance, source);
                break;
            case 2:
                returnedValue = MethodInvoker.invoke(method, instance, source, arguments);
                break;
            default:
                throw new IllegalStateException(); // FIXME
        }
        return (R) returnedValue;
    }

    private <S, R> void createBatchedFieldWiring(String type, Method method, Object instance) {
        String dataLoaderKey = UUID.randomUUID().toString();
        Supplier<DataLoader<DataLoaderKey<S>, R>> dataLoaderSupplier = () -> DataLoader.newMappedDataLoader(keys -> {
            List<CompletableFuture<Map<DataLoaderKey<S>, R>>> futures = keys
                    .stream()
                    .collect(groupingBy(DataLoaderKey::getArguments))
                    .entrySet()
                    .stream()
                    .map(entry((arguments, sameArgumentsKeys) -> {
                        Map<S, DataLoaderKey<S>> sameArgumentsKeysBySource = sameArgumentsKeys
                                .stream()
                                .collect(toMap(DataLoaderKey::getSource, identity()));
                        return this
                                .<CompletableFuture<Map<S, R>>>invoke(method, instance, sameArgumentsKeysBySource.keySet(), arguments)
                                .thenApply(resultsBySource -> resultsBySource
                                        .entrySet()
                                        .stream()
                                        .collect(toMap(
                                                sourceResultEntry -> sameArgumentsKeysBySource.get(sourceResultEntry.getKey()),
                                                Map.Entry::getValue
                                        ))
                                );
                    }))
                    .collect(toList());
            return FutureParalleliser
                    .parallelise(futures)
                    .thenApply(results -> results
                            .stream()
                            .reduce(MapMerger::merge)
                            .orElseGet(Collections::emptyMap)
                    );
        });
        dataLoaderRegistrars.add(
                new DataLoaderRegistrar<>(
                        dataLoaderKey,
                        dataLoaderSupplier
                )
        );
        fieldWirings.add(new GraphFieldWiring<>(
                type,
                method.getName(),
                environment -> environment
                        .<DataLoaderRegistryGetter>getContext()
                        .getDataLoaderRegistry()
                        .<DataLoaderKey<S>, R>getDataLoader(dataLoaderKey)
                        .load(new DataLoaderKey<>(
                                environment.getSource(),
                                environment.getArguments()
                        ))
        ));
    }

    private void createSimpleFieldWiring(String type, Method method, Object instance) {
        fieldWirings.add(new GraphFieldWiring<>(
                type,
                method.getName(),
                environment -> {
                    Object source = environment.getSource();
                    Map<String, Object> arguments = environment.getArguments();
                    return invoke(method, instance, source, arguments);
                }
        ));
    }

    private void inspect(Object resolver) {
        Stream
                .of(resolver)
                .map(Object::getClass)
                .filter(clazz -> clazz.isAnnotationPresent(GraphResolver.class))
                .forEach(clazz -> {
                    String type = clazz.getAnnotation(GraphResolver.class).value();
                    Stream
                            .of(clazz.getDeclaredMethods())
                            .filter(method -> Modifier.isPublic(method.getModifiers()))
                            .filter(method -> !Modifier.isStatic(method.getModifiers()))
                            .forEach(method -> {
                                if (method.isAnnotationPresent(GraphBatched.class)) {
                                    createBatchedFieldWiring(type, method, resolver);
                                } else {
                                    createSimpleFieldWiring(type, method, resolver);
                                }
                            });
                });
    }

    static GraphResolverInspection inspect(Collection<Object> resolvers) {
        GraphResolverInspection inspector = new GraphResolverInspection();
        resolvers.forEach(inspector::inspect);
        return inspector;
    }

}
