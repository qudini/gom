package graphql.gom.example.resolvers;

import graphql.gom.Resolver;
import graphql.gom.Resolving;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.services.BlogService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Resolver("Query")
public final class QueryResolver {

    private static final AtomicInteger GET_BLOGS_CALL_COUNT = new AtomicInteger();

    @Resolving("blogs")
    public List<Blog> getBlogs() {
        GET_BLOGS_CALL_COUNT.incrementAndGet();
        return BlogService.findAll();
    }

    public static int getBlogsCallCount() {
        return GET_BLOGS_CALL_COUNT.get();
    }

    public static void resetCounts() {
        GET_BLOGS_CALL_COUNT.set(0);
    }

}
