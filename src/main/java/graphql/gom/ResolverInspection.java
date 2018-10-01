package graphql.gom;

import graphql.gom.utils.Maps;
import graphql.gom.utils.Methods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dataloader.DataLoader;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static graphql.gom.utils.Futures.parallelise;
import static graphql.gom.utils.Reductions.failIfDifferent;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static org.dataloader.DataLoader.newMappedDataLoader;

@AllArgsConstructor(access = PRIVATE)
@Getter(PACKAGE)
final class ResolverInspection<C extends DataLoaderRegistryGetter> {

    private final Converters<C> converters;

    private final Set<FieldWiring> fieldWirings = new HashSet<>();

    private final Set<DataLoaderRegistrar> dataLoaderRegistrars = new HashSet<>();

    private <R> CompletableFuture<R> invoke(Method method, Object instance, Object source, Arguments arguments, C context) {
        final Object returnedValue;
        switch (method.getParameterCount()) {
            case 0:
                if (source == null) {
                    returnedValue = Methods.invoke(method, instance);
                } else {
                    throw new IllegalStateException(); // FIXME
                }
                break;
            case 1:
                returnedValue = source == null
                        ? Methods.invoke(method, instance, arguments)
                        : Methods.invoke(method, instance, source);
                break;
            case 2:
                if (source == null) {
                    throw new IllegalStateException(); // FIXME
                } else {
                    returnedValue = Methods.invoke(method, instance, source, arguments);
                }
                break;
            default:
                throw new IllegalStateException(); // FIXME
        }
        return converters.convert(returnedValue, context);
    }

    private <S, R> void createBatchedFieldWiring(String type, Method method, Object instance) {
        String dataLoaderKey = randomUUID().toString();
        Supplier<DataLoader<DataLoaderKey<S, C>, R>> dataLoaderSupplier = () -> newMappedDataLoader(keys -> {
            Optional<C> maybeContext = keys
                    .stream()
                    .map(DataLoaderKey::getContext)
                    .reduce(failIfDifferent());
            List<CompletableFuture<Map<DataLoaderKey<S, C>, R>>> futures = keys
                    .stream()
                    .collect(groupingBy(DataLoaderKey::getArguments))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        Map<S, DataLoaderKey<S, C>> sameArgumentsKeysBySource = entry
                                .getValue()
                                .stream()
                                .collect(toMap(DataLoaderKey::getSource, identity()));
                        return this
                                .<Map<S, R>>invoke(
                                        method,
                                        instance,
                                        sameArgumentsKeysBySource.keySet(),
                                        entry.getKey(),
                                        maybeContext.orElseThrow(IllegalStateException::new)
                                )
                                .thenApply(resultsBySource -> resultsBySource
                                        .entrySet()
                                        .stream()
                                        .collect(toMap(
                                                sourceResultEntry -> sameArgumentsKeysBySource.get(sourceResultEntry.getKey()),
                                                Map.Entry::getValue
                                        ))
                                );
                    })
                    .collect(toList());
            return parallelise(futures)
                    .thenApply(results -> results
                            .stream()
                            .reduce(Maps::merge)
                            .orElseGet(Collections::emptyMap)
                    );
        });
        dataLoaderRegistrars.add(
                new DataLoaderRegistrar<>(
                        dataLoaderKey,
                        dataLoaderSupplier
                )
        );
        fieldWirings.add(new FieldWiring<>(
                type,
                method.getName(),
                environment -> {
                    C context = environment.getContext();
                    return context
                            .getDataLoaderRegistry()
                            .<DataLoaderKey<S, C>, R>getDataLoader(dataLoaderKey)
                            .load(new DataLoaderKey<>(
                                    environment.getSource(),
                                    new Arguments(environment.getArguments()),
                                    context
                            ));
                }
        ));
    }

    private void createSimpleFieldWiring(String type, Method method, Object instance) {
        fieldWirings.add(new FieldWiring<>(
                type,
                method.getName(),
                environment -> invoke(
                        method,
                        instance,
                        environment.getSource(),
                        new Arguments(environment.getArguments()),
                        environment.getContext()
                )
        ));
    }

    private void inspect(Object resolver) {
        Stream
                .of(resolver)
                .map(Object::getClass)
                .filter(clazz -> clazz.isAnnotationPresent(Resolver.class))
                .forEach(clazz -> {
                    String type = clazz.getAnnotation(Resolver.class).value();
                    Stream
                            .of(clazz.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(Fetcher.class))
                            .forEach(method -> {
                                if (!method.isAccessible()) {
                                    method.setAccessible(true);
                                }
                                if (method.isAnnotationPresent(Batched.class)) {
                                    createBatchedFieldWiring(type, method, resolver);
                                } else {
                                    createSimpleFieldWiring(type, method, resolver);
                                }
                            });
                });
    }

    static <C extends DataLoaderRegistryGetter> ResolverInspection<C> inspect(Collection<Object> resolvers, Converters<C> converters) {
        ResolverInspection<C> inspector = new ResolverInspection<>(converters);
        resolvers.forEach(inspector::inspect);
        return inspector;
    }

}
