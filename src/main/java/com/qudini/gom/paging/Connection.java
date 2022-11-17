package com.qudini.gom.paging;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * https://relay.dev/graphql/connections.htm#sec-Connection-Types
 */
@Value
@Builder
public class Connection<T> {

    long totalCount;
    PageInfo pageInfo;
    List<Edge<T>> edges;

    public <U> Connection<U> mapNodes(Function<T, U> mapper) {
        return mapEdges(edge -> new Edge<>(edge.getCursor(), mapper.apply(edge.getNode())));
    }

    public <U> Connection<U> mapEdges(Function<Edge<T>, Edge<U>> mapper) {
        return new Connection<>(totalCount, pageInfo, edges.stream().map(mapper).collect(collectingAndThen(toList(), Collections::unmodifiableList)));
    }

    public static <T> Connection<T> empty() {
        return new Connection<>(0, PageInfo.builder().build(), emptyList());
    }

    /**
     * <p>
     * If arguments.after is provided, to help build the page info,
     * the list of edges is expected to hold arguments.first + 2 elements:
     * </p>
     *
     * <ul>
     * <li>first, the element matching arguments.after</li>
     * <li>then, the following arguments.first elements (which will be the ones in the returned connection eventually)</li>
     * <li>finally, the following one element</li>
     * </ul>
     *
     * <p>In other words, in pseudo-SQL, instead of:</p>
     *
     * <pre>{@literal
     * WHERE column > :after ORDER BY column LIMIT :first
     * }</pre>
     *
     * <p>Use:</p>
     *
     * <pre>{@literal
     * WHERE column >= :after ORDER BY column LIMIT :first + 2
     * }</pre>
     * </p>
     *
     * <p>
     * If arguments.after is not provided, to help build the page info,
     * the list of edges is expected to hold arguments.first + 1 element:
     * </p>
     *
     * <ul>
     * <li>first, the arguments.first elements (which will be the ones in the returned connection eventually)</li>
     * <li>then finally, the following one element</li>
     * </ul>
     *
     * <p>In other words, in pseudo-SQL, instead of:</p>
     *
     * <pre>{@literal
     * ORDER BY column LIMIT :first
     * }</pre>
     *
     * <p>Use:</p>
     *
     * <pre>{@literal
     * ORDER BY column LIMIT :first + 1
     * }</pre>
     * </p>
     */
    public static <T> Connection<T> build(long totalCount, List<Edge<T>> edges, PageArguments arguments) {
        return arguments
                .getAfter()
                .map(after -> build(totalCount, edges, arguments.getFirst(), after))
                .orElseGet(() -> build(totalCount, edges, arguments.getFirst(), false));
    }

    private static <T> Connection<T> build(long totalCount, List<Edge<T>> edges, int first, String after) {
        boolean hasPreviousPage = edges.get(0).getCursor().equals(after);
        List<Edge<T>> actualEdges = hasPreviousPage ? edges.subList(1, edges.size()) : edges;
        return actualEdges.isEmpty()
                ? new Connection<>(totalCount, PageInfo.builder().hasPreviousPage(true).build(), emptyList())
                : build(totalCount, actualEdges, first, hasPreviousPage);
    }

    private static <T> Connection<T> build(long totalCount, List<Edge<T>> edges, int first, boolean hasPreviousPage) {
        boolean hasNextPage = edges.size() > first;
        List<Edge<T>> actualEdges = hasNextPage ? edges.subList(0, first) : edges;
        PageInfo pageInfo = PageInfo
                .builder()
                .hasPreviousPage(hasPreviousPage)
                .hasNextPage(hasNextPage)
                .startCursor(actualEdges.get(0).getCursor())
                .endCursor(actualEdges.get(actualEdges.size() - 1).getCursor())
                .build();
        return new Connection<>(totalCount, pageInfo, unmodifiableList(actualEdges));
    }

    public static <T extends Comparable<T>> Connection<T> merge(Collection<Connection<T>> connections, int maxSize) {
        if (connections.size() == 0) {
            return Connection.empty();
        }
        if (connections.size() == 1) {
            return connections.iterator().next();
        }
        long totalCount = connections.stream().mapToLong(Connection::getTotalCount).sum();
        if (totalCount == 0) {
            return Connection.empty();
        }
        List<Edge<T>> allEdges = connections
                .stream()
                .flatMap(connection -> connection.getEdges().stream())
                .sorted(comparing(Edge::getNode))
                .collect(toList());
        List<Edge<T>> edges = allEdges.size() <= maxSize ? allEdges : allEdges.subList(0, maxSize);
        boolean hasPreviousPage = connections.stream().anyMatch(connection -> connection.getPageInfo().isHasPreviousPage());
        boolean hasNextPage = connections.stream().anyMatch(connection -> connection.getPageInfo().isHasNextPage())
                || edges.size() < allEdges.size();
        PageInfo pageInfo = PageInfo
                .builder()
                .hasPreviousPage(hasPreviousPage)
                .hasNextPage(hasNextPage)
                .startCursor(edges.get(0).getCursor())
                .endCursor(edges.get(edges.size() - 1).getCursor())
                .build();
        return new Connection<>(totalCount, pageInfo, unmodifiableList(edges));
    }

}
