package graphql.gom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Map;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@NoArgsConstructor(access = PUBLIC)
public final class DataFetcherTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class QueryResolver {

        public MyType myType() {
            return new MyType("foo");
        }

    }

    @Test
    public void defaultsToGetter() {
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foo", ((Map<String, ?>) callData(gom).get("myType")).get("name"));
    }

    @Test
    public void withoutSourceNorArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            public String name() {
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

    @Test
    public void withSource() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            public String name(MyType myType) {
                return myType.getName() + "bar";
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gom).get("myType")).get("name"));
    }

    @Test
    public void withArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            public String name(Arguments arguments) {
                return arguments.get("name");
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

    @Test
    public void withSourceAndArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            public String name(MyType myType, Arguments arguments) {
                return myType.getName() + arguments.get("suffix");
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(), new MyTypeResolver()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gom).get("myType")).get("name"));
    }

}
