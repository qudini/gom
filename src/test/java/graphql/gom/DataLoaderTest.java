package graphql.gom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@NoArgsConstructor(access = PUBLIC)
public final class DataLoaderTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @RequiredArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class QueryResolver {

        private final boolean fooFirst;

        public List<MyType> myTypes() {
            return asList(
                    fooFirst ? new MyType("foo") : new MyType("bar"),
                    fooFirst ? new MyType("bar") : new MyType("foo")
            );
        }

    }

    @Test
    public void withoutSourcesNorArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Batched
            public Map<MyType, String> name() {
                return new HashMap<>();
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

    @Test
    public void withSources() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Batched
            public Map<MyType, String> name(Set<MyType> myTypes) {
                return myTypes
                        .stream()
                        .collect(toMap(identity(), myType -> myType.getName() + "bar"));
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callData(gom).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name"));
        assertEquals("barbar", myTypes.get(1).get("name"));
    }

    @Test
    public void withArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Batched
            public Map<MyType, String> name(Arguments arguments) {
                return new HashMap<>();
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

    @Test
    public void withSourcesAndArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Batched
            public Map<MyType, String> name(Set<MyType> myTypes, Arguments arguments) {
                return myTypes
                        .stream()
                        .collect(toMap(identity(), myType -> myType.getName() + arguments.get("suffix")));
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(true), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callData(gom).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name"));
        assertEquals("barbar", myTypes.get(1).get("name"));
    }

    @Test
    public void sourceOrder() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("MyType")
        final class MyTypeResolver {

            @Batched
            public Map<MyType, String> name(Set<MyType> myTypes) {
                return myTypes
                        .stream()
                        .collect(toMap(identity(), myType -> myType.getName() + "bar"));
            }

        }
        Gom gom = newGom()
                .resolvers(asList(new QueryResolver(false), new MyTypeResolver()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callData(gom).get("myTypes");
        assertEquals("barbar", myTypes.get(0).get("name"));
        assertEquals("foobar", myTypes.get(1).get("name"));
    }

}
