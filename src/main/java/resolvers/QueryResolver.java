package resolvers;

import db.Blog;
import db.Repository;
import reactor.core.publisher.Flux;

public final class QueryResolver {

    public static final QueryResolver INSTANCE = new QueryResolver();

    private QueryResolver() {
    }

    public Flux<Blog> getBlogs() {
        return Repository
                .findAllBlogs();
    }

}
