import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.gom.GomConfig;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import org.dataloader.DataLoaderRegistry;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import resolvers.ArticleResolver;
import resolvers.BlogResolver;
import resolvers.CommentResolver;
import resolvers.QueryResolver;

import static graphql.gom.GomConfig.newGomConfig;
import static graphql.gom.GomConverters.newGomConverters;
import static java.util.Arrays.asList;

public final class Main {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final GomConfig GOM_CONFIG = newGomConfig(
            asList(
                    ArticleResolver.INSTANCE,
                    BlogResolver.INSTANCE,
                    CommentResolver.INSTANCE,
                    QueryResolver.INSTANCE
            ),
            newGomConverters()
                    .with(Mono.class, (result, context) -> result.toFuture())
                    .with(Flux.class, (result, context) -> result.collectList().toFuture())
                    .build()
    );

    private static final GraphQLSchema SCHEMA;

    static {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        GOM_CONFIG.decorateRuntimeWiringBuilder(builder);
        builder.scalar(new DateTimeScalar());
        SCHEMA = new SchemaGenerator().makeExecutableSchema(
                new SchemaParser().parse(ResourceReader.read("schema.graphql")),
                builder.build()
        );
    }

    private static String serialise(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void newQuery() {

        String query = ResourceReader.read("query");

        DataLoaderRegistry registry = new DataLoaderRegistry();
        GOM_CONFIG.decorateDataLoaderRegistry(registry);
        Context context = new Context(registry);

        ExecutionInput executionInput = ExecutionInput
                .newExecutionInput()
                .query(query)
                .context(context)
                .build();

        DataLoaderDispatcherInstrumentation instrumentation = new DataLoaderDispatcherInstrumentation(registry);

        GraphQL graphQL = GraphQL
                .newGraphQL(SCHEMA)
                .instrumentation(instrumentation)
                .build();

        graphQL
                .executeAsync(executionInput)
                .thenAccept(result -> {
                    System.out.println(serialise(result.toSpecification()));
                });

    }

}
