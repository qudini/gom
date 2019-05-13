package graphql.gom;

import graphql.gom.utils.Context;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callExpectingData;
import static graphql.gom.utils.QueryRunner.callExpectingErrors;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.*;

public final class DataLoaderTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @RequiredArgsConstructor(access = PRIVATE)
    @TypeResolver("Query")
    public static final class QueryResolver {

        private final boolean fooFirst;

        @FieldResolver("myTypes")
        public List<MyType> myTypes() {
            return asList(
                    fooFirst ? new MyType("foo") : new MyType("bar"),
                    fooFirst ? new MyType("bar") : new MyType("foo")
            );
        }

    }

    @Test
    public void withSources() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Set<MyType> myTypes) {
                callCount.incrementAndGet();
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> myType.getName() + "bar"
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name"));
        assertEquals("barbar", myTypes.get(1).get("name"));
        assertEquals(1, callCount.get());
    }

    @Test
    public void withArguments() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Arguments arguments) {
                callCount.incrementAndGet();
                return new HashMap<>();
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertEquals(0, callCount.get());
    }

    @Test
    public void withSelection() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Selection selection) {
                callCount.incrementAndGet();
                return new HashMap<>();
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        assertFalse(callExpectingErrors(gom, Context::new).isEmpty());
        assertEquals(0, callCount.get());
    }

    @Test
    public void withSourcesAndArguments() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Set<MyType> myTypes, Arguments arguments) {
                callCount.incrementAndGet();
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> myType.getName() + arguments.get("suffix")
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name"));
        assertEquals("barbar", myTypes.get(1).get("name"));
        assertEquals(1, callCount.get());
    }

    @Test
    public void withSourcesAndSelection() {
        AtomicInteger callCount = new AtomicInteger(0);
        AtomicBoolean containsValue = new AtomicBoolean(false);
        @RequiredArgsConstructor(access = PRIVATE)
        @Getter
        final class MyName {

            private final String value;

        }
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, MyName> name(Set<MyType> myTypes, Selection selection) {
                callCount.incrementAndGet();
                containsValue.set(selection.contains("value"));
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> new MyName(myType.getName())
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Map<String, Object>>> myTypes = (List<Map<String, Map<String, Object>>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foo", myTypes.get(0).get("name").get("value"));
        assertEquals("bar", myTypes.get(1).get("name").get("value"));
        assertEquals(1, callCount.get());
        assertTrue(containsValue.get());
    }

    @Test
    public void withSourcesArgumentsAndSelection() {
        AtomicInteger callCount = new AtomicInteger(0);
        AtomicBoolean containsValue = new AtomicBoolean(false);
        @RequiredArgsConstructor(access = PRIVATE)
        @Getter
        final class MyName {

            private final String value;

        }
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, MyName> name(Set<MyType> myTypes, Arguments arguments, Selection selection) {
                callCount.incrementAndGet();
                containsValue.set(selection.contains("value"));
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> new MyName(myType.getName() + arguments.get("suffix"))
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Map<String, Object>>> myTypes = (List<Map<String, Map<String, Object>>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name").get("value"));
        assertEquals("barbar", myTypes.get(1).get("name").get("value"));
        assertEquals(1, callCount.get());
        assertTrue(containsValue.get());
    }

    @Test
    public void sourceOrder() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Set<MyType> myTypes) {
                callCount.incrementAndGet();
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> myType.getName() + "bar"
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(false), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("barbar", myTypes.get(0).get("name"));
        assertEquals("foobar", myTypes.get(1).get("name"));
        assertEquals(1, callCount.get());
    }

    @Test
    public void distinctByArguments() {
        AtomicInteger callCount = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Set<MyType> myTypes, Arguments arguments) {
                callCount.incrementAndGet();
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> myType.getName() + arguments.getOptional("suffix").orElse("")
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foo", myTypes.get(0).get("nameWithoutSuffix"));
        assertEquals("foofoo", myTypes.get(0).get("nameWithFooSuffix"));
        assertEquals("foobar", myTypes.get(0).get("nameWithBarSuffix"));
        assertEquals("bar", myTypes.get(1).get("nameWithoutSuffix"));
        assertEquals("barfoo", myTypes.get(1).get("nameWithFooSuffix"));
        assertEquals("barbar", myTypes.get(1).get("nameWithBarSuffix"));
        assertEquals(3, callCount.get());
    }

    @Test
    public void sameByArguments() {
        AtomicInteger count = new AtomicInteger(0);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("MyType")
        final class MyTypeResolver {

            @Batched
            @FieldResolver("name")
            public Map<MyType, String> name(Set<MyType> myTypes, Arguments arguments) {
                count.incrementAndGet();
                return myTypes
                        .stream()
                        .collect(toMap(
                                identity(),
                                myType -> myType.getName() + arguments.get("suffix")
                        ));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callExpectingData(gom, new Context()).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("nameWithSuffix1"));
        assertEquals("foobar", myTypes.get(0).get("nameWithSuffix2"));
        assertEquals("barbar", myTypes.get(1).get("nameWithSuffix1"));
        assertEquals("barbar", myTypes.get(1).get("nameWithSuffix2"));
        assertEquals(1, count.get());
    }

}
