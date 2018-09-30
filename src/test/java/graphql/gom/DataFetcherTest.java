package graphql.gom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.util.Map;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;

@NoArgsConstructor(access = PUBLIC)
public final class DataFetcherTest {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter
    public static final class MyType {

        private final String name;

    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class QueryResolver {

        public MyType myType() {
            return new MyType("foo");
        }

    }

    @Test
    public void defaultsToGetter() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foo", ((Map<String, ?>) callData(gomConfig).get("myType")).get("name"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("MyType")
    public static final class WithoutSourceNorArguments {

        public String name() {
            return "foobar";
        }

    }

    @Test
    public void withoutSourceNorArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new QueryResolver(), new WithoutSourceNorArguments()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("MyType")
    public static final class WithSource {

        public String name(MyType myType) {
            return myType.getName() + "bar";
        }

    }

    @Test
    public void withSource() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new QueryResolver(), new WithSource()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gomConfig).get("myType")).get("name"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("MyType")
    public static final class WithArguments {

        public String name(GomArguments arguments) {
            return arguments.get("name");
        }

    }

    @Test
    public void withArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new QueryResolver(), new WithArguments()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("MyType")
    public static final class WithSourceAndArguments {

        public String name(MyType myType, GomArguments arguments) {
            return myType.getName() + arguments.get("suffix");
        }

    }

    @Test
    public void withSourceAndArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(asList(new QueryResolver(), new WithSourceAndArguments()))
                .build();
        assertEquals("foobar", ((Map<String, ?>) callData(gomConfig).get("myType")).get("name"));
    }

}
