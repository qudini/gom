package graphql.gom;

import lombok.NoArgsConstructor;
import org.junit.Test;

import static graphql.gom.Gom.newGom;
import static graphql.gom.utils.QueryRunner.callData;
import static graphql.gom.utils.QueryRunner.callErrors;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;

@NoArgsConstructor(access = PUBLIC)
public final class QueryTest {

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class WithoutSourceNorArguments {

        public String foobar() {
            return "foobar";
        }

    }

    @Test
    public void withoutSourceNorArguments() {
        Gom gom = newGom()
                .resolvers(singletonList(new WithoutSourceNorArguments()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class WithSource {

        public static final class Source {
        }

        public String foobar(Source source) {
            return "foobar";
        }

    }

    @Test
    public void withSource() {
        Gom gom = newGom()
                .resolvers(singletonList(new WithSource()))
                .build();
        assertEquals(1, callErrors(gom).size());
    }

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class WithArguments {

        public String foobar(Arguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void withArguments() {
        Gom gom = newGom()
                .resolvers(singletonList(new WithArguments()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @Resolver("Query")
    public static final class WithSourceAndArguments {

        public static final class Source {
        }

        public String foobar(Source source, Arguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void withSourceAndArguments() {
        Gom gom = newGom()
                .resolvers(singletonList(new WithSourceAndArguments()))
                .build();
        assertEquals(1, callErrors(gom).size());
    }

}
