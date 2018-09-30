package resolvers;

import db.Blog;
import db.Repository;
import graphql.gom.GomArguments;
import graphql.gom.GomResolver;
import reactor.core.publisher.Flux;

@GomResolver("Query")
public final class QueryResolver {

    public static final QueryResolver INSTANCE = new QueryResolver();

    private QueryResolver() {
    }

    public Flux<Blog> blogs(GomArguments arguments) {
        return Repository.findAllBlogs();
    }

}
