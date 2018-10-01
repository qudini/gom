package graphql.gom;

import graphql.gom.utils.Context;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static graphql.gom.ArgumentsTest.MyEnum.MY_VALUE;
import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.*;

@NoArgsConstructor(access = PUBLIC)
public final class ArgumentsTest {

    public enum MyEnum {
        MY_VALUE
    }

    @Test
    public void getAbsent() {
        Arguments arguments = new Arguments(new HashMap<String, Object>() {{
            put("key", "value");
        }});
        assertNull(arguments.get("wrongkey"));
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
        assertNull(arguments.getEnum("wrongkey", MyEnum.class));
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
        assertNull(arguments.getInput("wrongkey"));
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
        @Resolver("Query")
        final class QueryResolver {

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
        @Resolver("Query")
        final class QueryResolver {

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

}
