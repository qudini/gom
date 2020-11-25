package com.qudini.gom;

import com.qudini.gom.utils.Context;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.qudini.gom.ArgumentsTest.MyEnum.MY_VALUE;
import static com.qudini.gom.Gom.newGom;
import static com.qudini.gom.utils.QueryRunner.callExpectingData;
import static graphql.schema.GraphQLScalarType.newScalar;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public final class ArgumentsTest {

    public enum MyEnum {
        MY_VALUE
    }

    @Test
    public void getAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertThrows(NullPointerException.class, () -> arguments.get("wrongkey"));
    }

    @Test
    public void getPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.get("key"));
    }

    @Test
    public void getOptionalAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getOptional("wrongkey"));
    }

    @Test
    public void getOptionalPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of("value"), arguments.getOptional("key"));
    }

    @Test
    public void getNullableAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getNullable("wrongkey"));
    }

    @Test
    public void getNullablePresentButNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.empty()), arguments.getNullable("key"));
    }

    @Test
    public void getNullablePresentNotNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.of("value")), arguments.getNullable("key"));
    }

    @Test
    public void getEnumAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertThrows(NullPointerException.class, () -> arguments.getEnum("wrongkey", MyEnum.class));
    }

    @Test
    public void getEnumPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertEquals(MY_VALUE, arguments.getEnum("key", MyEnum.class));
    }

    @Test
    public void getOptionalEnumAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getOptionalEnum("wrongkey", MyEnum.class));
    }

    @Test
    public void getOptionalEnumPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(MY_VALUE), arguments.getOptionalEnum("key", MyEnum.class));
    }

    @Test
    public void getNullableEnumAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getNullableEnum("wrongkey", MyEnum.class));
    }

    @Test
    public void getNullableEnumPresentButNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.empty()), arguments.getNullableEnum("key", MyEnum.class));
    }

    @Test
    public void getNullableEnumPresentNotNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.of(MY_VALUE)), arguments.getNullableEnum("key", MyEnum.class));
    }

    @Test
    public void getInputAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertThrows(NullPointerException.class, () -> arguments.getInput("wrongkey"));
    }

    @Test
    public void getInputPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getInput("key").get("subkey"));
    }

    @Test
    public void getOptionalInputAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getOptionalInput("wrongkey"));
    }

    @Test
    public void getOptionalInputPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getOptionalInput("key").get().get("subkey"));
    }

    @Test
    public void getNullableInputAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getNullableInput("wrongkey"));
    }

    @Test
    public void getNullableInputPresentButNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.empty()), arguments.getNullableInput("key"));
    }

    @Test
    public void getNullableInputPresentNotNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getNullableInput("key").get().get().get("subkey"));
    }

    @Test
    public void getInputArrayAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertThrows(NullPointerException.class, () -> arguments.getInputArray("wrongkey"));
    }

    @Test
    public void getInputArrayPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getInputArray("key").get(0).get("subkey"));
    }

    @Test
    public void getOptionalInputArrayAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getOptionalInputArray("wrongkey"));
    }

    @Test
    public void getOptionalInputArrayPresent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getOptionalInputArray("key").get().get(0).get("subkey"));
    }

    @Test
    public void getNullableInputArrayAbsent() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.empty(), arguments.getNullableInputArray("wrongkey"));
    }

    @Test
    public void getNullableInputArrayPresentButNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(1, arguments.size());
        assertEquals(Optional.of(Optional.empty()), arguments.getNullableInputArray("key"));
    }

    @Test
    public void getNullableInputArrayPresentNotNull() {
        Arguments arguments = new DefaultArguments(new HashMap<String, Object>() {{
            put("key", new ArrayList<Map<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put("subkey", "value");
                }});
            }});
        }});
        assertEquals(1, arguments.size());
        assertEquals("value", arguments.getNullableInputArray("key").get().get().get(0).get("subkey"));
    }

    @Test
    public void empty() {
        Arguments arguments = Arguments.empty();
        assertEquals(0, arguments.size());
    }

    @Test
    public void enumArgument() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public MyEnum foobar(Arguments arguments) {
                called.set(true);
                return arguments.getEnum("foobar", MyEnum.class);
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("MY_VALUE", callExpectingData(gom, new Context()).get("foobar"));
        assertTrue(called.get());
    }

    @Test
    public void inputArgument() {
        AtomicBoolean called = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foo")
            public String foo(Arguments arguments) {
                called.set(true);
                return arguments.getInput("foo").get("bar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callExpectingData(gom, new Context()).get("foo"));
        assertTrue(called.get());
    }

    @Test
    public void scalarArgument() {
        AtomicBoolean scalarCalled = new AtomicBoolean(false);
        final class ZonedDateTimeCoercing implements Coercing<ZonedDateTime, String> {

            @Override
            public String serialize(Object value) {
                scalarCalled.set(true);
                if (value instanceof ZonedDateTime) {
                    return ISO_OFFSET_DATE_TIME.format((ZonedDateTime) value);
                } else {
                    throw new CoercingSerializeException();
                }
            }

            @Override
            public ZonedDateTime parseValue(Object value) {
                scalarCalled.set(true);
                if (value instanceof String) {
                    return ZonedDateTime.parse((String) value, ISO_OFFSET_DATE_TIME);
                } else {
                    throw new CoercingParseValueException();
                }
            }

            @Override
            public ZonedDateTime parseLiteral(Object value) {
                scalarCalled.set(true);
                if (value instanceof StringValue) {
                    return ZonedDateTime.parse(((StringValue) value).getValue(), ISO_OFFSET_DATE_TIME);
                } else {
                    return null;
                }
            }

        }
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        @NoArgsConstructor(access = PRIVATE)
        @TypeResolver("Query")
        final class QueryResolver {

            @FieldResolver("foobar")
            public ZonedDateTime foobar(Arguments arguments) {
                resolverCalled.set(true);
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals(
                "2011-12-03T10:15:30+01:00",
                callExpectingData(
                        gom,
                        new Context(),
                        newScalar()
                                .name("DateTime")
                                .description("ZonedDateTime")
                                .coercing(new ZonedDateTimeCoercing())
                                .build()
                ).get("foobar")
        );
        assertTrue(resolverCalled.get());
        assertTrue(scalarCalled.get());
    }

}
