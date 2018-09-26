package graphql.gom.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public final class FutureParalleliser {

    public static <T> CompletableFuture<List<T>> parallelise(List<CompletableFuture<T>> futures) {
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(x -> futures
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(toList())
                );
    }

}
