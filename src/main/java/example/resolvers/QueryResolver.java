package example.resolvers;

import example.db.Blog;
import example.db.Repository;
import graphql.gom.inspecting.GraphResolver;
import reactor.core.publisher.Flux;

@GraphResolver("Query")
public final class QueryResolver {

    public static final QueryResolver INSTANCE = new QueryResolver();

    private QueryResolver() {
    }

    public Flux<Blog> getBlogs() {
        return Repository
                .findAllBlogs();
    }

}
