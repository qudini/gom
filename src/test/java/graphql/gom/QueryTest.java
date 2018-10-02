package graphql.gom;

import graphql.gom.utils.Context;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class QueryTest {

    @Test
    public void withoutSourceNorArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            @Resolving("foobar")
            public String foobar() {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, Context::new).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void withSource() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            final class Source {
            }

            @Resolving("foobar")
            public String foobar(Source source) {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            @Resolving("foobar")
            public String foobar(Arguments arguments) {
                called.set(true);
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, Context::new).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void withSourceAndArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            final class Source {
            }

            @Resolving("foobar")
            public String foobar(Source source, Arguments arguments) {
                called.set(true);
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

}
