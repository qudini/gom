package com.qudini.gom.paging;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class ConnectionTest {

    static class TestPageArguments implements PageArguments {

        private final int first;
        private final String after;

        TestPageArguments(int first, String after) {
            this.first = first;
            this.after = after;
        }

        @Override
        public int getFirst() {
            return first;
        }

        @Override
        public Optional<String> getAfter() {
            return Optional.ofNullable(after);
        }

    }

    @Test
    public void empty() {
        Connection<Integer> connection = Connection.empty();
        assertEquals(0, connection.getTotalCount());
        assertFalse(connection.getPageInfo().isHasPreviousPage());
        assertFalse(connection.getPageInfo().isHasNextPage());
        assertNull(connection.getPageInfo().getStartCursor());
        assertNull(connection.getPageInfo().getEndCursor());
        assertTrue(connection.getEdges().isEmpty());
    }

    @Test
    public void singlePage() {
        List<Edge<Integer>> edges = asList(buildEdge(1));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, null));
        assertEquals(42, connection.getTotalCount());
        assertFalse(connection.getPageInfo().isHasPreviousPage());
        assertFalse(connection.getPageInfo().isHasNextPage());
        assertEquals("c1", connection.getPageInfo().getStartCursor());
        assertEquals("c1", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(0)), connection.getEdges());
    }

    @Test
    public void nextPage() {
        List<Edge<Integer>> edges = asList(buildEdge(1), buildEdge(2), buildEdge(3));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, null));
        assertEquals(42, connection.getTotalCount());
        assertFalse(connection.getPageInfo().isHasPreviousPage());
        assertTrue(connection.getPageInfo().isHasNextPage());
        assertEquals("c1", connection.getPageInfo().getStartCursor());
        assertEquals("c2", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(0), edges.get(1)), connection.getEdges());
    }

    @Test
    public void singlePageAfterNotFound() {
        List<Edge<Integer>> edges = asList(buildEdge(2));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, "c1"));
        assertEquals(42, connection.getTotalCount());
        assertFalse(connection.getPageInfo().isHasPreviousPage());
        assertFalse(connection.getPageInfo().isHasNextPage());
        assertEquals("c2", connection.getPageInfo().getStartCursor());
        assertEquals("c2", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(0)), connection.getEdges());
    }

    @Test
    public void nextPageAfterNotFound() {
        List<Edge<Integer>> edges = asList(buildEdge(2), buildEdge(3), buildEdge(4));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, "c1"));
        assertEquals(42, connection.getTotalCount());
        assertFalse(connection.getPageInfo().isHasPreviousPage());
        assertTrue(connection.getPageInfo().isHasNextPage());
        assertEquals("c2", connection.getPageInfo().getStartCursor());
        assertEquals("c3", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(0), edges.get(1)), connection.getEdges());
    }

    @Test
    public void emptyAfterFound() {
        List<Edge<Integer>> edges = asList(buildEdge(1));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, "c1"));
        assertEquals(42, connection.getTotalCount());
        assertTrue(connection.getPageInfo().isHasPreviousPage());
        assertFalse(connection.getPageInfo().isHasNextPage());
        assertNull(connection.getPageInfo().getStartCursor());
        assertNull(connection.getPageInfo().getEndCursor());
        assertTrue(connection.getEdges().isEmpty());
    }

    @Test
    public void singlePageAfterFound() {
        List<Edge<Integer>> edges = asList(buildEdge(1), buildEdge(2));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, "c1"));
        assertEquals(42, connection.getTotalCount());
        assertTrue(connection.getPageInfo().isHasPreviousPage());
        assertFalse(connection.getPageInfo().isHasNextPage());
        assertEquals("c2", connection.getPageInfo().getStartCursor());
        assertEquals("c2", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(1)), connection.getEdges());
    }

    @Test
    public void nextPageAfterFound() {
        List<Edge<Integer>> edges = asList(buildEdge(1), buildEdge(2), buildEdge(3), buildEdge(4));
        Connection<Integer> connection = Connection.build(42, edges, new TestPageArguments(2, "c1"));
        assertEquals(42, connection.getTotalCount());
        assertTrue(connection.getPageInfo().isHasPreviousPage());
        assertTrue(connection.getPageInfo().isHasNextPage());
        assertEquals("c2", connection.getPageInfo().getStartCursor());
        assertEquals("c3", connection.getPageInfo().getEndCursor());
        assertEquals(asList(edges.get(1), edges.get(2)), connection.getEdges());
    }

    @Test
    public void mergeNoConnections() {
        Connection<Integer> mergedConnection = Connection.merge(emptyList(), 2);
        assertEquals(Connection.empty(), mergedConnection);
    }

    @Test
    public void mergeOneConnection() {
        Connection<Integer> connection = Connection.empty();
        Connection<Integer> mergedConnection = Connection.merge(asList(connection), 2);
        assertEquals(Connection.empty(), mergedConnection);
    }

    @Test
    public void mergeTwoEmptyConnections() {
        Connection<Integer> connection1 = Connection.empty();
        Connection<Integer> connection2 = Connection.empty();
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(Connection.empty(), mergedConnection);
    }

    @Test
    public void mergeWithoutPrevious() {
        Edge<Integer> e1 = buildEdge(1);
        Edge<Integer> e2 = buildEdge(2);
        Connection<Integer> connection1 = new Connection<>(1, PageInfo.builder().build(), asList(e2));
        Connection<Integer> connection2 = new Connection<>(1, PageInfo.builder().build(), asList(e1));
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(2, mergedConnection.getTotalCount());
        assertFalse(mergedConnection.getPageInfo().isHasPreviousPage());
        assertFalse(mergedConnection.getPageInfo().isHasNextPage());
        assertEquals("c1", mergedConnection.getPageInfo().getStartCursor());
        assertEquals("c2", mergedConnection.getPageInfo().getEndCursor());
        assertEquals(asList(e1, e2), mergedConnection.getEdges());
    }

    @Test
    public void mergeWithPrevious() {
        Edge<Integer> e1 = buildEdge(1);
        Edge<Integer> e2 = buildEdge(2);
        Connection<Integer> connection1 = new Connection<>(1, PageInfo.builder().build(), asList(e2));
        Connection<Integer> connection2 = new Connection<>(1, PageInfo.builder().hasPreviousPage(true).build(), asList(e1));
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(2, mergedConnection.getTotalCount());
        assertTrue(mergedConnection.getPageInfo().isHasPreviousPage());
        assertFalse(mergedConnection.getPageInfo().isHasNextPage());
        assertEquals("c1", mergedConnection.getPageInfo().getStartCursor());
        assertEquals("c2", mergedConnection.getPageInfo().getEndCursor());
        assertEquals(asList(e1, e2), mergedConnection.getEdges());
    }

    @Test
    public void mergeWithoutNext() {
        Edge<Integer> e1 = buildEdge(1);
        Edge<Integer> e2 = buildEdge(2);
        Connection<Integer> connection1 = new Connection<>(1, PageInfo.builder().build(), asList(e2));
        Connection<Integer> connection2 = new Connection<>(1, PageInfo.builder().build(), asList(e1));
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(2, mergedConnection.getTotalCount());
        assertFalse(mergedConnection.getPageInfo().isHasPreviousPage());
        assertFalse(mergedConnection.getPageInfo().isHasNextPage());
        assertEquals("c1", mergedConnection.getPageInfo().getStartCursor());
        assertEquals("c2", mergedConnection.getPageInfo().getEndCursor());
        assertEquals(asList(e1, e2), mergedConnection.getEdges());
    }

    @Test
    public void mergeWithNext() {
        Edge<Integer> e1 = buildEdge(1);
        Edge<Integer> e2 = buildEdge(2);
        Connection<Integer> connection1 = new Connection<>(1, PageInfo.builder().build(), asList(e2));
        Connection<Integer> connection2 = new Connection<>(1, PageInfo.builder().hasNextPage(true).build(), asList(e1));
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(2, mergedConnection.getTotalCount());
        assertFalse(mergedConnection.getPageInfo().isHasPreviousPage());
        assertTrue(mergedConnection.getPageInfo().isHasNextPage());
        assertEquals("c1", mergedConnection.getPageInfo().getStartCursor());
        assertEquals("c2", mergedConnection.getPageInfo().getEndCursor());
        assertEquals(asList(e1, e2), mergedConnection.getEdges());
    }

    @Test
    public void mergeWithImplicitNext() {
        Edge<Integer> e1 = buildEdge(1);
        Edge<Integer> e2 = buildEdge(2);
        Edge<Integer> e3 = buildEdge(3);
        Connection<Integer> connection1 = new Connection<>(2, PageInfo.builder().build(), asList(e3, e2));
        Connection<Integer> connection2 = new Connection<>(1, PageInfo.builder().build(), asList(e1));
        Connection<Integer> mergedConnection = Connection.merge(asList(connection1, connection2), 2);
        assertEquals(3, mergedConnection.getTotalCount());
        assertFalse(mergedConnection.getPageInfo().isHasPreviousPage());
        assertTrue(mergedConnection.getPageInfo().isHasNextPage());
        assertEquals("c1", mergedConnection.getPageInfo().getStartCursor());
        assertEquals("c2", mergedConnection.getPageInfo().getEndCursor());
        assertEquals(asList(e1, e2), mergedConnection.getEdges());
    }

    private Edge<Integer> buildEdge(int node) {
        return new Edge<>("c" + node, node);
    }

}
