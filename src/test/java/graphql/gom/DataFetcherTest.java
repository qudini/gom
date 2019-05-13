package graphql.gom;

import graphql.gom.utils.Context;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callExpectingData;
import static graphql.gom.utils.QueryRunner.callExpectingErrors;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class DataFetcherTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @NoArgsConstructor(access = PRIVATE)
    @TypeResolver("Query")
    public static final class QueryResolver {

        @FieldResolver("myType")
        public MyType myType() {
            return new MyType("foo");
        }

    }

    @Test
    public void defaultsToGetter() {
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foo", ((Map<String, ?>) callExpectingData(gom, new Context()).get("myType")).get("name"));
    }

    @Test
    public void withSource() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public String name(MyType myType) {
                called.set(true);
                return myType.getName() + "bar";
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callExpectingData(gom, new Context()).get("myType")).get("name"));
        assertTrue(called.get());
    }

    @Test
    public void withArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public String name(Arguments arguments) {
                called.set(true);
                return arguments.get("name");
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withSelection() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public String name(Selection selection) {
                called.set(true);
                return selection.stream().collect(joining());
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertFalse(called.get());
    }

    @Test
    public void withSourceAndArguments() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public String name(MyType myType, Arguments arguments) {
                called.set(true);
                return myType.getName() + arguments.get("suffix");
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callExpectingData(gom, new Context()).get("myType")).get("name"));
        assertTrue(called.get());
    }

    @Test
    public void withSourceAndSelection() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicBoolean containsValue = new AtomicBoolean(false);
        @RequiredArgsConstructor(access = PRIVATE)
        @Getter
        final class MyName {

            private final String value;

        }
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public MyName name(MyType myType, Selection selection) {
                called.set(true);
                containsValue.set(selection.contains("value"));
                return new MyName(myType.getName());
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foo", ((Map<String, Map<String, ?>>) callExpectingData(gom, new Context()).get("myType")).get("name").get("value"));
        assertTrue(called.get());
        assertTrue(containsValue.get());
    }

    @Test
    public void withSourceArgumentsAndSelection() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicBoolean containsValue = new AtomicBoolean(false);
        @RequiredArgsConstructor(access = PRIVATE)
        @Getter
        final class MyName {

            private final String value;

        }
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public MyName name(MyType myType, Arguments arguments, Selection selection) {
                called.set(true);
                containsValue.set(selection.contains("value"));
                return new MyName(myType.getName() + arguments.get("suffix"));
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, Map<String, ?>>) callExpectingData(gom, new Context()).get("myType")).get("name").get("value"));
        assertTrue(called.get());
        assertTrue(containsValue.get());
    }

}
