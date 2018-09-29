package example.resolvers;

import example.db.Blog;
import example.db.Repository;
import graphql.gom.GomResolver;
import reactor.core.publisher.Flux;

@GomResolver("Query")
public final class QueryResolver {

    public static final QueryResolver INSTANCE = new QueryResolver();

    private QueryResolver() {
    }

    public Flux<Blog> blogs() {
        return Repository.findAllBlogs();
    }

}
