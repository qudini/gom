package graphql.gom;

import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.QueryRunner.callData;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class ConvertersTest {

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class MandatoryFuture {

        public CompletableFuture<String> foobar() {
            return completedFuture("foobar");
        }

    }

    @Test
    public void mandatoryFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new MandatoryFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class MandatoryNoFuture {

        public String foobar() {
            return "foobar";
        }

    }

    @Test
    public void mandatoryNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new MandatoryNoFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class OptionalPresentFuture {

        public CompletableFuture<Optional<String>> foobar() {
            return completedFuture(Optional.of("foobar"));
        }

    }

    @Test
    public void optionalPresentFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new OptionalPresentFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class OptionalPresentNoFuture {

        public Optional<String> foobar() {
            return Optional.of("foobar");
        }

    }

    @Test
    public void optionalPresentNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new OptionalPresentNoFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class OptionalAbsentFuture {

        public CompletableFuture<Optional<String>> foobar() {
            return completedFuture(Optional.empty());
        }

    }

    @Test
    public void optionalAbsentFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new OptionalAbsentFuture()))
                .build();
        assertNull(callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class OptionalAbsentNoFuture {

        public Optional<String> foobar() {
            return Optional.empty();
        }

    }

    @Test
    public void optionalAbsentNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new OptionalAbsentNoFuture()))
                .build();
        assertNull(callData(gomConfig).get("foobar"));
    }

}
