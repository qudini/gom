package graphql.gom;

import graphql.gom.utils.Context;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static graphql.gom.Converters.newConverters;
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
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public CompletableFuture<String> foobar() {
                called.set(true);
                return completedFuture("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void mandatoryNoFuture() {
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
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalPresentFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public CompletableFuture<Optional<String>> foobar() {
                called.set(true);
                return completedFuture(Optional.of("foobar"));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalPresentNoFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public Optional<String> foobar() {
                called.set(true);
                return Optional.of("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalAbsentFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public CompletableFuture<Optional<String>> foobar() {
                called.set(true);
                return completedFuture(Optional.empty());
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void optionalAbsentNoFuture() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public Optional<String> foobar() {
                called.set(true);
                return Optional.empty();
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertNull(callData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void customConverter() {
        @RequiredArgsConstructor(access = PRIVATE)
        final class MyWrapper {

            private final String value;

        }
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicBoolean converterCalled = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public MyWrapper foobar() {
                resolverCalled.set(true);
                return new MyWrapper("foobar");
            }

        }
        Context queryContext = new Context();
        AtomicReference<Context> converterContext = new AtomicReference<>();
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .converters(
                        newConverters(Context.class)
                                .converter(MyWrapper.class, (myWrapper, converterContextValue) -> {
                                    converterCalled.set(true);
                                    converterContext.set(converterContextValue);
                                    return myWrapper.value;
                                })
                                .build()
                )
                .build();
        assertEquals("foobar", callData(gom, queryContext).get("foobar"));
        assertEquals(queryContext, converterContext.get());
        assertTrue(converterCalled.get());
        assertTrue(resolverCalled.get());
    }

    @Test
    public void customNestedConverters() {
        @RequiredArgsConstructor(access = PRIVATE)
        final class MyWrapper<T> {

            private final T value;

        }
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicBoolean converterCalled = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public MyWrapper foobar() {
                resolverCalled.set(true);
                return new MyWrapper<>(new MyWrapper<>("foobar"));
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .converters(
                        newConverters(Context.class)
                                .converter(MyWrapper.class, (myWrapper, context) -> {
                                    converterCalled.set(true);
                                    return myWrapper.value;
                                })
                                .build()
                )
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(converterCalled.get());
        assertTrue(resolverCalled.get());
    }

    @Test
    public void customConvertersSuperFromSameHierarchy() {
        @RequiredArgsConstructor(access = PRIVATE)
        class MySuperWrapper {

            protected final String value;

        }
        final class MySubWrapper extends MySuperWrapper {

            private MySubWrapper(String value) {
                super(value);
            }

        }
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicBoolean superConverterCalled = new AtomicBoolean(false);
        AtomicBoolean subConverterCalled = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public MySuperWrapper foobar() {
                resolverCalled.set(true);
                return new MySuperWrapper("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .converters(
                        newConverters(Context.class)
                                .converter(MySuperWrapper.class, (mySuperWrapper, context) -> {
                                    superConverterCalled.set(true);
                                    return mySuperWrapper.value;
                                })
                                .converter(MySubWrapper.class, (mySuperWrapper, context) -> {
                                    subConverterCalled.set(true);
                                    return mySuperWrapper.value;
                                })
                                .build()
                )
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertTrue(superConverterCalled.get());
        assertFalse(subConverterCalled.get());
        assertTrue(resolverCalled.get());
    }

    @Test
    public void customConvertersSubFromSameHierarchy() {
        @RequiredArgsConstructor(access = PRIVATE)
        class MySuperWrapper {

            protected final String value;

        }
        final class MySubWrapper extends MySuperWrapper {

            private MySubWrapper(String value) {
                super(value);
            }

        }
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicBoolean superConverterCalled = new AtomicBoolean(false);
        AtomicBoolean subConverterCalled = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public MySubWrapper foobar() {
                resolverCalled.set(true);
                return new MySubWrapper("foobar");
            }

        }
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .converters(
                        newConverters(Context.class)
                                .converter(MySuperWrapper.class, (mySuperWrapper, context) -> {
                                    superConverterCalled.set(true);
                                    return mySuperWrapper.value;
                                })
                                .converter(MySubWrapper.class, (mySuperWrapper, context) -> {
                                    subConverterCalled.set(true);
                                    return mySuperWrapper.value;
                                })
                                .build()
                )
                .build();
        assertEquals("foobar", callData(gom, new Context()).get("foobar"));
        assertFalse(superConverterCalled.get());
        assertTrue(subConverterCalled.get());
        assertTrue(resolverCalled.get());
    }

}
