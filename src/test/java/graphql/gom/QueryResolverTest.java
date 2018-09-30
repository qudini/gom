package graphql.gom;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.gom.utils.Context;
import graphql.gom.utils.ResourceReader;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoaderRegistry;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.utils.ResourceReader.readResource;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.junit.Assert.assertEquals;

@NoArgsConstructor(access = PUBLIC)
public final class QueryResolverTest {

    @NoArgsConstructor(access = PRIVATE)
    @GomResolver("Query")
    public static class TestQueryResolverMandatoryFuture {
        public CompletableFuture<String> foobar() {
            return CompletableFuture.completedFuture("foobar");
        }
    }

    @Test
    public void testQueryResolverMandatoryFuture() throws Exception {

        GomConfig gomConfig = newGomConfig()
                .resolvers(singletonList(new TestQueryResolverMandatoryFuture()))
                .build();

        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(readResource("/graphql/gom/testQueryResolverMandatoryFuture.graphql"));

        RuntimeWiring.Builder runtimeWiringBuilder = newRuntimeWiring();
        gomConfig.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
        RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiring
        );

        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        gomConfig.decorateDataLoaderRegistry(dataLoaderRegistry);

        ExecutionInput executionInput = ExecutionInput
                .newExecutionInput()
                .context(new Context(dataLoaderRegistry))
                .query(ResourceReader.readResource("/graphql/gom/testQueryResolverMandatoryFuture.query"))
                .build();

        GraphQL graphQL = GraphQL
                .newGraphQL(graphQLSchema)
                .instrumentation(new DataLoaderDispatcherInstrumentation(dataLoaderRegistry))
                .build();

        ExecutionResult result = graphQL.executeAsync(executionInput).get();
        assertEquals("foobar", result.<Map<?, ?>>getData().get("foobar"));

    }

}
