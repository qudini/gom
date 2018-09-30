package graphql.gom;

import lombok.NoArgsConstructor;
import org.junit.Test;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;

@NoArgsConstructor(access = PUBLIC)
public final class QueryTest {

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class WithoutSourceNorArguments {

        public String foobar() {
            return "foobar";
        }

    }

    @Test
    public void withoutSourceNorArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new WithoutSourceNorArguments()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class WithSource {

        public static final class Source {
        }

        public String foobar(Source source) {
            return "foobar";
        }

    }

    @Test
    public void withSource() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new WithSource()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class WithArguments {

        public String foobar(GomArguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void withArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new WithArguments()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class WithSourceAndArguments {

        public static final class Source {
        }

        public String foobar(Source source, GomArguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void withSourceAndArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new WithSourceAndArguments()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

}
