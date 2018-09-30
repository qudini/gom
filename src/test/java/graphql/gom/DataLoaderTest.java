package graphql.gom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.QueryRunner.callData;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;

@NoArgsConstructor(access = PUBLIC)
public final class DataLoaderTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class FooBarQueryResolver {

        public List<MyType> myTypes() {
            return asList(
                    new MyType("foo"),
                    new MyType("bar")
            );
        }

    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class BarFooQueryResolver {

        public List<MyType> myTypes() {
            return asList(
                    new MyType("bar"),
                    new MyType("foo")
            );
        }

    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("MyType")
    public static final class WithSources {

        @GomBatched
        public Map<MyType, String> name(Set<MyType> myTypes) {
            return myTypes
                    .stream()
                    .collect(toMap(identity(), myType -> myType.getName() + "bar"));
        }

    }

    @Test
    public void withSources() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new FooBarQueryResolver(), new WithSources()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callData(gomConfig).get("myTypes");
        assertEquals("foobar", myTypes.get(0).get("name"));
        assertEquals("barbar", myTypes.get(1).get("name"));
    }

    @Test
    public void sourceOrder() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new BarFooQueryResolver(), new WithSources()))
                .build();
        List<Map<String, Object>> myTypes = (List<Map<String, Object>>) callData(gomConfig).get("myTypes");
        assertEquals("barbar", myTypes.get(0).get("name"));
        assertEquals("foobar", myTypes.get(1).get("name"));
    }

}
