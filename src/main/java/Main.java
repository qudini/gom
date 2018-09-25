import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import graph.Context;
import graph.Schema;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import org.dataloader.DataLoaderRegistry;

public final class Main {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static String serialise(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String QUERY = "query {\n" +
            "  blogs {\n" +
            "    id\n" +
            "    title\n" +
            "    articles {\n" +
            "      id\n" +
            "      title\n" +
            "      comments {\n" +
            "        id\n" +
            "        title\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static void main(String[] args) {
        newQuery(QUERY);
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
                .thenApply(ExecutionResult::getData)
                .thenAccept(data -> System.out.println(serialise(data)));

    }

}
