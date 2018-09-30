package graphql.gom.utils;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.gom.GomConfig;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.Map;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.GraphQL.newGraphQL;
import static graphql.gom.utils.ResourceReader.readResource;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.lang.Thread.currentThread;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NoArgsConstructor(access = PRIVATE)
public final class QueryRunner {

    private static ExecutionResult call(GomConfig gomConfig) {

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

    public static Map<String, ?> callData(GomConfig gomConfig) {
        ExecutionResult result = call(gomConfig);
        assertEquals(0, result.getErrors().size());
        return result.getData();
    }

    public static List<GraphQLError> callErrors(GomConfig gomConfig) {
        ExecutionResult result = call(gomConfig);
        assertNull(result.getData());
        return result.getErrors();
    }


}
