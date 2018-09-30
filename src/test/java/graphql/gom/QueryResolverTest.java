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

        StackTraceElement caller = currentThread().getStackTrace()[3];
        String testClassName;
        try {
            testClassName = Class.forName(caller.getClassName()).getSimpleName();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        String testMethodName = caller.getMethodName();
        String baseResourceName = "/graphql/gom/" + testClassName + "." + testMethodName;

        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(readResource(baseResourceName + ".graphql"));

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
                .query(readResource(baseResourceName + ".query"))
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
