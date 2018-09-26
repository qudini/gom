package graphql.gom;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;

public final class Wiring {

    public static final RuntimeWiring INSTANCE = RuntimeWiring
            .newRuntimeWiring()
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Query")
                            .dataFetcher("blogs", DataFetchers.queryToBlogsDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Blog")
                            .dataFetcher("articles", DataFetchers.blogToArticlesDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Article")
                            .dataFetcher("blog", DataFetchers.articleToBlogDataFetcher)
                            .dataFetcher("comments", DataFetchers.articleToCommentsDataFetcher)
            )
            .type(
                    TypeRuntimeWiring
                            .newTypeWiring("Comment")
                            .dataFetcher("article", DataFetchers.commentToArticleDataFetcher)
            )
            .build();

}
