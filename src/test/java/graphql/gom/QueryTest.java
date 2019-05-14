package graphql.gom;

import graphql.gom.utils.Context;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callExpectingData;
import static graphql.gom.utils.QueryRunner.callExpectingErrors;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class QueryTest {

    @Test
    public void withoutSourceNorArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public String foobar() {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callExpectingData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void withSource() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            final class Source {
            }

            @FieldResolver("foobar")
            public String foobar(Source source) {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public String foobar(Arguments arguments) {
                called.set(true);
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callExpectingData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void withSourceAndArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            final class Source {
            }

            @FieldResolver("foobar")
            public String foobar(Source source, Arguments arguments) {
                called.set(true);
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

}
