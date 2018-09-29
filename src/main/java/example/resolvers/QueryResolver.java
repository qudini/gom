package example.resolvers;

import example.db.Blog;
import example.db.Repository;
import graphql.gom.GomResolver;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@GomResolver("Query")
public final class QueryResolver {

    public static final QueryResolver INSTANCE = new QueryResolver();

    private QueryResolver() {
    }

    public CompletableFuture<List<Blog>> blogs() {
        return Repository
                .findAllBlogs()
                .collectList()
                .toFuture();
    }

}
