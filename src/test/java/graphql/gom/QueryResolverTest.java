package graphql.gom;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.gom.utils.Context;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoaderRegistry;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.GraphQL.newGraphQL;
import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.ResourceReader.readResource;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NoArgsConstructor(access = PUBLIC)
public final class QueryResolverTest {

    private ExecutionResult call(GomConfig gomConfig) {

        String testMethodName = currentThread().getStackTrace()[3].getMethodName();

        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(readResource("/graphql/gom/" + testMethodName + ".graphql"));

        RuntimeWiring.Builder runtimeWiringBuilder = newRuntimeWiring();
        gomConfig.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
        RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiring
        );

        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        gomConfig.decorateDataLoaderRegistry(dataLoaderRegistry);

        ExecutionInput executionInput = newExecutionInput()
                .context(new Context(dataLoaderRegistry))
                .query(readResource("/graphql/gom/" + testMethodName + ".query"))
                .build();

        GraphQL graphQL = newGraphQL(graphQLSchema)
                .instrumentation(new DataLoaderDispatcherInstrumentation(dataLoaderRegistry))
                .build();

        try {
            return graphQL.executeAsync(executionInput).get();
        } catch (Exception e) {
            throw new AssertionError("An error occurred while executing the GraphQL query", e);
        }

    }

    private Map<String, ?> callData(GomConfig gomConfig) {
        ExecutionResult result = call(gomConfig);
        assertEquals(0, result.getErrors().size());
        return result.getData();
    }

    private List<GraphQLError> callErrors(GomConfig gomConfig) {
        ExecutionResult result = call(gomConfig);
        assertNull(result.getData());
        return result.getErrors();
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverMandatoryFuture {

        public CompletableFuture<String> foobar() {
            return completedFuture("foobar");
        }

    }

    @Test
    public void testQueryResolverMandatoryFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverMandatoryFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverMandatoryNoFuture {

        public String foobar() {
            return "foobar";
        }

    }

    @Test
    public void testQueryResolverMandatoryNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverMandatoryNoFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverOptionalPresentFuture {

        public CompletableFuture<Optional<String>> foobar() {
            return completedFuture(Optional.of("foobar"));
        }

    }

    @Test
    public void testQueryResolverOptionalPresentFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverOptionalPresentFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverOptionalPresentNoFuture {

        public Optional<String> foobar() {
            return Optional.of("foobar");
        }

    }

    @Test
    public void testQueryResolverOptionalPresentNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverOptionalPresentNoFuture()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverOptionalAbsentFuture {

        public CompletableFuture<Optional<String>> foobar() {
            return completedFuture(Optional.empty());
        }

    }

    @Test
    public void testQueryResolverOptionalAbsentFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverOptionalAbsentFuture()))
                .build();
        assertNull(callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverOptionalAbsentNoFuture {

        public Optional<String> foobar() {
            return Optional.empty();
        }

    }

    @Test
    public void testQueryResolverOptionalAbsentNoFuture() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverOptionalAbsentNoFuture()))
                .build();
        assertNull(callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverWithArguments {

        public String foobar(GomArguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void testQueryResolverWithArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverWithArguments()))
                .build();
        assertEquals("foobar", callData(gomConfig).get("foobar"));
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverWithSource {

        public static final class Source {
        }

        public String foobar(Source source) {
            return "foobar";
        }

    }

    @Test
    public void testQueryResolverWithSource() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverWithSource()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static final class TestQueryResolverWithSourceAndArguments {

        public static final class Source {
        }

        public String foobar(Source source, GomArguments arguments) {
            return arguments.get("foobar");
        }

    }

    @Test
    public void testQueryResolverWithSourceAndArguments() {
        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverWithSourceAndArguments()))
                .build();
        assertEquals(1, callErrors(gomConfig).size());
    }

}
