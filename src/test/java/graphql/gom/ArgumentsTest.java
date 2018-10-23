package graphql.gom;

import graphql.gom.utils.Context;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.NoArgsConstructor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.ArgumentsTest.MyEnum.MY_VALUE;
import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ArgumentsTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public enum MyEnum {
        MY_VALUE
    }

    @Test
    public void getAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        exception.expect(NullPointerException.class);
        arguments.get("wrongkey");
    }

    @Test
    public void getPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals("value", arguments.get("key"));
    }

    @Test
    public void getOptionalAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(Optional.empty(), arguments.getOptional("wrongkey"));
    }

    @Test
    public void getOptionalPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(Optional.of("value"), arguments.getOptional("key"));
    }

    @Test
    public void getNullableAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(Optional.empty(), arguments.getNullable("wrongkey"));
    }

    @Test
    public void getNullablePresentButNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(Optional.of(Optional.empty()), arguments.getNullable("key"));
    }

    @Test
    public void getNullablePresentNotNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertEquals(Optional.of(Optional.of("value")), arguments.getNullable("key"));
    }

    @Test
    public void getEnumAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        exception.expect(NullPointerException.class);
        arguments.getEnum("wrongkey", MyEnum.class);
    }

    @Test
    public void getEnumPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(MY_VALUE, arguments.getEnum("key", MyEnum.class));
    }

    @Test
    public void getOptionalEnumAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(Optional.empty(), arguments.getOptionalEnum("wrongkey", MyEnum.class));
    }

    @Test
    public void getOptionalEnumPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(Optional.of(MY_VALUE), arguments.getOptionalEnum("key", MyEnum.class));
    }

    @Test
    public void getNullableEnumAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(Optional.empty(), arguments.getNullableEnum("wrongkey", MyEnum.class));
    }

    @Test
    public void getNullableEnumPresentButNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(Optional.of(Optional.empty()), arguments.getNullableEnum("key", MyEnum.class));
    }

    @Test
    public void getNullableEnumPresentNotNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "MY_VALUE");
        }});
        assertEquals(Optional.of(Optional.of(MY_VALUE)), arguments.getNullableEnum("key", MyEnum.class));
    }

    @Test
    public void getInputAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        exception.expect(NullPointerException.class);
        arguments.getInput("wrongkey");
    }

    @Test
    public void getInputPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals("value", arguments.getInput("key").get("subkey"));
    }

    @Test
    public void getOptionalInputAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(Optional.empty(), arguments.getOptionalInput("wrongkey"));
    }

    @Test
    public void getOptionalInputPresent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals("value", arguments.getOptionalInput("key").get().get("subkey"));
    }

    @Test
    public void getNullableInputAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals(Optional.empty(), arguments.getNullableInput("wrongkey"));
    }

    @Test
    public void getNullableInputPresentButNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", null);
        }});
        assertEquals(Optional.of(Optional.empty()), arguments.getNullableInput("key"));
    }

    @Test
    public void getNullableInputPresentNotNull() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", new HashMap<String, Object>() {{
                put("subkey", "value");
            }});
        }});
        assertEquals("value", arguments.getNullableInput("key").get().get().get("subkey"));
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
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("MY_VALUE", callData(gom, Context::new).get("foobar"));
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
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom, Context::new).get("foo"));
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
        Gom gom = newGom(Context.class)
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals(
                "2011-12-03T10:15:30+01:00",
                callData(
                        gom,
                        Context::new,
                        new GraphQLScalarType("DateTime", "ZonedDateTime", new ZonedDateTimeCoercing())
                ).get("foobar")
        );
        assertTrue(resolverCalled.get());
        assertTrue(scalarCalled.get());
    }

}
