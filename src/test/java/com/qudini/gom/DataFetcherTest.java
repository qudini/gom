package com.qudini.gom;

import com.qudini.gom.utils.Context;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.qudini.gom.Gom.newGom;
import static com.qudini.gom.utils.QueryRunner.callExpectingData;
import static com.qudini.gom.utils.QueryRunner.callExpectingErrors;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        AtomicBoolean validSelection = new AtomicBoolean(false);
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
                validSelection.set(selection.size() == 1 && selection.contains("value"));
                return new MyName(myType.getName());
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foo", ((Map<String, Map<String, ?>>) callExpectingData(gom, new Context()).get("myType")).get("name").get("value"));
        assertTrue(called.get());
        assertTrue(validSelection.get());
    }

    @Test
    public void withSourceAndDeeperSelection() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicBoolean validSelection = new AtomicBoolean(false);
        @RequiredArgsConstructor(access = PRIVATE)
        @Getter
        final class MyName {

            private final String value;
            private final String content;

            MyName(String value) {
                this(value, value);
            }

            public MyName getSelf() {
                return this;
            }

        }
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @FieldResolver("name")
            public MyName name(MyType myType, @Depth(2) Selection selection) {
                called.set(true);
                validSelection.set(
                        selection.size() == 5
                                && selection.contains("value")
                                && selection.contains("self")
                                && selection.contains("self/value")
                                && selection.contains("self/content")
                                && selection.contains("self/self")
                );
                return new MyName(myType.getName());
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foo", ((Map<String, Map<String, ?>>) callExpectingData(gom, new Context()).get("myType")).get("name").get("value"));
        assertTrue(called.get());
        assertTrue(validSelection.get());
    }

    @Test
    public void withSourceArgumentsAndSelection() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicBoolean validSelection = new AtomicBoolean(false);
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
                validSelection.set(selection.size() == 1 && selection.contains("value"));
                return new MyName(myType.getName() + arguments.get("suffix"));
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, Map<String, ?>>) callExpectingData(gom, new Context()).get("myType")).get("name").get("value"));
        assertTrue(called.get());
        assertTrue(validSelection.get());
    }

}
