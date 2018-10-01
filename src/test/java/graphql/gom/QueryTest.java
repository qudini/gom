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
import static org.junit.Assert.assertFalse;

@NoArgsConstructor(access = PUBLIC)
public final class QueryTest {

    @Test
    public void withoutSourceNorArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public String foobar() {
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void withSource() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            final class Source {
            }

            public String foobar(Source source) {
                return "foobar";
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

    @Test
    public void withArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            public String foobar(Arguments arguments) {
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertEquals("foobar", callData(gom).get("foobar"));
    }

    @Test
    public void withSourceAndArguments() {
        @NoArgsConstructor(access = PRIVATE)
        @Resolver("Query")
        final class QueryResolver {

            final class Source {
            }

            public String foobar(Source source, Arguments arguments) {
                return arguments.get("foobar");
            }

        }
        Gom gom = newGom()
                .resolvers(singletonList(new QueryResolver()))
                .build();
        assertFalse(callErrors(gom).isEmpty());
    }

}
