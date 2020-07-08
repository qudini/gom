package com.qudini.gom.example.resolvers;

import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Blog;
import com.qudini.gom.example.services.BlogService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Query")
public final class QueryResolver {

    private static final AtomicInteger GET_BLOGS_CALL_COUNT = new AtomicInteger();

    @FieldResolver("blogs")
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
