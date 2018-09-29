package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Futures {

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
