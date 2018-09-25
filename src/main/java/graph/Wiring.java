package graph;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;

public final class Wiring {

    public static final RuntimeWiring INSTANCE = RuntimeWiring
            .newRuntimeWiring()
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Query")
                            .dataFetcher("blogs", DataFetchers.blogsDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Blog")
                            .dataFetcher("articles", DataFetchers.articlesByBlogDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Article")
                            .dataFetcher("blog", DataFetchers.blogByArticleDataFetcher)
                            .dataFetcher("comments", DataFetchers.commentsByArticleDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Comment")
                            .dataFetcher("article", DataFetchers.articleByCommentDataFetcher)
            )
            .build();

}
