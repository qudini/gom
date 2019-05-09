package graphql.gom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dataloader.DataLoader;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static org.dataloader.DataLoader.newMappedDataLoader;

@AllArgsConstructor(access = PRIVATE)
@Getter(PACKAGE)
final class ResolverInspection<C> {

    private final Converters<C> converters;

    private final Set<FieldWiring> fieldWirings = new HashSet<>();

    private final Set<DataLoaderRegistrar> dataLoaderRegistrars = new HashSet<>();

    private <R> CompletableFuture<R> invoke(MethodInvoker methodInvoker, @Nullable Object source, Arguments arguments, C context) {
        final Object returnedValue;
        int parameterCount = methodInvoker.getParameterCount();
        switch (parameterCount) {

            case 0:
                if (source == null) {
                    returnedValue = methodInvoker.invoke();
                } else {
                    throw new GomException(format(
                            "Method %s doesn't take the source(s) as its first argument, while isn't mapped to the GraphQL type Query",
                            methodInvoker
                    ));
                }
                break;

            case 1:
                returnedValue = source == null
                        ? methodInvoker.invoke(arguments)
                        : methodInvoker.invoke(source);
                break;

            case 2:
                if (source == null) {
                    throw new GomException(format(
                            "Method %s takes the source(s) as its first argument, while is mapped to the GraphQL type Query",
                            methodInvoker
                    ));
                } else {
                    returnedValue = methodInvoker.invoke(source, arguments);
                }
                break;

            default:
                int min = source == null
                        ? 0
                        : 1;
                throw new GomException(format(
                        "Method %s should take %d or %d argument(s), while currently expects %s",
                        methodInvoker,
                        min,
                        min + 1,
                        parameterCount
                ));

        }
        return converters.convert(returnedValue, context);
    }

    private <S, R> void createBatchedFieldWiring(String type, String field, MethodInvoker methodInvoker) {
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
                                        methodInvoker,
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
                            .reduce(ResolverInspection::merge)
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
                field,
                environment -> environment
                        .<DataLoaderKey<S, C>, R>getDataLoader(dataLoaderKey)
                        .load(new DataLoaderKey<>(
                                environment.getSource(),
                                new MapBasedArguments(environment.getArguments()),
                                environment.getContext()
                        ))
        ));
    }

    private void createSimpleFieldWiring(String type, String field, MethodInvoker methodInvoker) {
        fieldWirings.add(new FieldWiring<>(
                type,
                field,
                environment -> invoke(
                        methodInvoker,
                        environment.getSource(),
                        new MapBasedArguments(environment.getArguments()),
                        environment.getContext()
                )
        ));
    }

    private void inspect(Object resolver) {
        Stream
                .of(resolver)
                .map(Object::getClass)
                .filter(clazz -> clazz.isAnnotationPresent(TypeResolver.class))
                .forEach(clazz -> {
                    String type = clazz.getAnnotation(TypeResolver.class).value();
                    Stream
                            .of(clazz.getDeclaredMethods())
                            .filter(method -> method.isAnnotationPresent(FieldResolver.class))
                            .forEach(method -> {
                                String field = method.getAnnotation(FieldResolver.class).value();
                                MethodInvoker methodInvoker = MethodInvoker.of(method, resolver);
                                if (method.isAnnotationPresent(Batched.class)) {
                                    createBatchedFieldWiring(type, field, methodInvoker);
                                } else {
                                    createSimpleFieldWiring(type, field, methodInvoker);
                                }
                            });
                });
    }

    static <C> ResolverInspection<C> inspect(Collection<Object> resolvers, Converters<C> converters) {
        ResolverInspection<C> inspector = new ResolverInspection<>(converters);
        resolvers.forEach(inspector::inspect);
        return inspector;
    }

    private static <T> CompletableFuture<List<T>> parallelise(List<CompletableFuture<T>> futures) {
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(x -> futures
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(toList())
                );
    }

    private static <K, V> Map<K, V> merge(Map<K, V> firstMap, Map<K, V> secondMap) {
        Map<K, V> resultingMap = new HashMap<>();
        resultingMap.putAll(firstMap);
        resultingMap.putAll(secondMap);
        return resultingMap;
    }

    private static <T> BinaryOperator<T> failIfDifferent() {
        return (x, y) -> {
            if (x.equals(y)) {
                return x;
            } else {
                throw new IllegalStateException(format(
                        "%s and %s shouldn't have been different",
                        x,
                        y
                ));
            }
        };
    }

}
