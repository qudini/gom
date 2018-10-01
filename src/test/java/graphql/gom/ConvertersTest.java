package graphql.gom;

import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class ConvertersTest {

    @Test
    public void mandatoryFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<String> foobar() {
                called.set(true);
                return completedFuture("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void mandatoryNoFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public String foobar() {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalPresentFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<Optional<String>> foobar() {
                called.set(true);
                return completedFuture(Optional.of("foobar"));
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalPresentNoFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public Optional<String> foobar() {
                called.set(true);
                return Optional.of("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalAbsentFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<Optional<String>> foobar() {
                called.set(true);
                return completedFuture(Optional.empty());
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalAbsentNoFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public Optional<String> foobar() {
                called.set(true);
                return Optional.empty();
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom).get("foobar"));
        assertTrue(called.get());
    }

}
