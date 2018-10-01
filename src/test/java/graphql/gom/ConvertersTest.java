package graphql.gom;

import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class ConvertersTest {

    @Test
    public void mandatoryFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<String> foobar() {
                return completedFuture("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void mandatoryNoFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public String foobar() {
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void optionalPresentFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<Optional<String>> foobar() {
                return completedFuture(Optional.of("foobar"));
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void optionalPresentNoFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public Optional<String> foobar() {
                return Optional.of("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void optionalAbsentFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public CompletableFuture<Optional<String>> foobar() {
                return completedFuture(Optional.empty());
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom).get("foobar"));
    }

    @Test
    public void optionalAbsentNoFuture() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public Optional<String> foobar() {
                return Optional.empty();
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom).get("foobar"));
    }

}
