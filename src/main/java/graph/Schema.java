package graph;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;

import java.io.File;
import java.net.URL;

public final class Schema {

    public static final GraphQLSchema INSTANCE;

    static {

        URL schemaUrl = Schema.class.getClassLoader().getResource("schema.graphql");
        File schemaFile = new File(schemaUrl.getFile());
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaFile);

        RuntimeWiring wiring = RuntimeWiring
                .newRuntimeWiring()
                .type(
                        TypeRuntimeWiring
                                .newTypeWiring("Query")
                                .dataFetcher("blogs", Wiring.blogsDataFetcher)
                )
                .type(
                        TypeRuntimeWiring
                                .newTypeWiring("Blog")
                                .dataFetcher("articles", Wiring.articlesByBlogDataFetcher)
                )
                .type(
                        TypeRuntimeWiring
                                .newTypeWiring("Article")
                                .dataFetcher("comments", Wiring.commentsByArticleDataFetcher)
                )
                .build();

        INSTANCE = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);

    }

}
