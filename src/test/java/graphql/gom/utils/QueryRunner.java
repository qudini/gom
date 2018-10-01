package graphql.gom.utils;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.gom.Gom;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.GraphQL.newGraphQL;
import static graphql.gom.utils.ResourceReader.readResource;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.lang.Thread.currentThread;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NoArgsConstructor(access = PRIVATE)
public final class QueryRunner {

    private static ExecutionResult call(Gom gom, Function<DataLoaderRegistry, ?> contextSupplier, GraphQLScalarType[] scalars) {

        StackTraceElement caller = currentThread().getStackTrace()[3];
        String testClassName;
        try {
            testClassName = Class.forName(caller.getClassName()).getSimpleName();
        } catch (ClassNotFoundException | NullPointerException e) {
            throw new IllegalStateException("Test class name couldn't be found", e);
        }
        String testMethodName = caller.getMethodName();
        String baseResourceName = "/graphql/gom/" + testClassName + "." + testMethodName;

        String graphqlFile = baseResourceName + ".graphql";
        TypeDefinitionRegistry typeDefinitionRegistry;
        try {
            typeDefinitionRegistry = new SchemaParser().parse(readResource(graphqlFile));
        } catch (NullPointerException e) {
            throw new IllegalStateException("File not found: " + graphqlFile, e);
        }

        RuntimeWiring.Builder runtimeWiringBuilder = newRuntimeWiring();
        Stream.of(scalars).forEach(runtimeWiringBuilder::scalar);
        gom.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
        RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiring
        );

        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        gom.decorateDataLoaderRegistry(dataLoaderRegistry);

        String queryFile = baseResourceName + ".query";
        ExecutionInput executionInput;
        try {
            executionInput = newExecutionInput()
                    .context(contextSupplier.apply(dataLoaderRegistry))
                    .query(readResource(queryFile))
                    .build();
        } catch (NullPointerException e) {
            throw new IllegalStateException("File not found: " + queryFile, e);
        }

        GraphQL graphQL = newGraphQL(graphQLSchema)
                .instrumentation(new DataLoaderDispatcherInstrumentation(dataLoaderRegistry))
                .build();

        try {
            return graphQL.executeAsync(executionInput).get();
        } catch (Exception e) {
            throw new AssertionError("An error occurred while executing the GraphQL query", e);
        }

    }

    public static Map<String, ?> callData(Gom gom, Function<DataLoaderRegistry, ?> contextSupplier, GraphQLScalarType... scalars) {
        ExecutionResult result = call(gom, contextSupplier, scalars);
        assertTrue(result.getErrors().isEmpty());
        return result.getData();
    }

    public static List<GraphQLError> callErrors(Gom gom, Function<DataLoaderRegistry, ?> contextSupplier, GraphQLScalarType... scalars) {
        ExecutionResult result = call(gom, contextSupplier, scalars);
        assertNull(result.getData());
        return result.getErrors();
    }

}
