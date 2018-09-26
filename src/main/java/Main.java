import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import graphql.gom.Context;
import graphql.gom.Schema;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import org.dataloader.DataLoaderRegistry;
import graphql.gom.utils.ResourceReader;

public final class Main {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static String serialise(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        String query = ResourceReader.read("query");
        newQuery(query);
    }

    private static void newQuery(String query) {

        Context context = new Context();

        ExecutionInput executionInput = ExecutionInput
                .newExecutionInput()
                .query(query)
                .context(context)
                .build();

        DataLoaderRegistry dataLoaderRegistry = context.getDataLoaderRegistry();
        DataLoaderDispatcherInstrumentation instrumentation = new DataLoaderDispatcherInstrumentation(dataLoaderRegistry);

        GraphQL graphQL = GraphQL
                .newGraphQL(Schema.INSTANCE)
                .instrumentation(instrumentation)
                .build();

        graphQL
                .executeAsync(executionInput)
                .thenAccept(result -> {
                    result.getErrors().forEach(error -> {
                        if (error instanceof ExceptionWhileDataFetching) {
                            ((ExceptionWhileDataFetching) error).getException().printStackTrace();
                        } else {
                            System.out.println(error);
                        }
                    });
                    System.out.println(serialise(result.getData()));
                });

    }

}
