package graphql.gom;

import graphql.gom.utils.Context;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class DataFetcherTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class QueryResolver {

        @Resolving("myType")
        public MyType myType() {
            return new MyType("foo");
        }

    }

    @Test
    public void defaultsToGetter() {
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foo", ((Map<String, ?>) callData(gom, Context::new).get("myType")).get("name"));
    }

    @Test
    public void withoutSourceNorArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Resolving("name")
            public String name() {
                called.set(true);
                return "foobar";
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withSource() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Resolving("name")
            public String name(MyType myType) {
                called.set(true);
                return myType.getName() + "bar";
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gom, Context::new).get("myType")).get("name"));
        assertTrue(called.get());
    }

    @Test
    public void withArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Resolving("name")
            public String name(Arguments arguments) {
                called.set(true);
                return arguments.get("name");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withSourceAndArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Resolving("name")
            public String name(MyType myType, Arguments arguments) {
                called.set(true);
                return myType.getName() + arguments.get("suffix");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gom, Context::new).get("myType")).get("name"));
        assertTrue(called.get());
    }

}
