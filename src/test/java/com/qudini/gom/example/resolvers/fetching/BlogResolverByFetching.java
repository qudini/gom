package com.qudini.gom.example.resolvers.fetching;

import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Article;
import com.qudini.gom.example.entities.Blog;
import com.qudini.gom.example.services.ArticleService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Blog")
public final class BlogResolverByFetching {

    private static final AtomicInteger GET_ARTICLES_CALL_COUNT = new AtomicInteger();

    @FieldResolver("articles")
    public List<Article> getArticles(Blog blog) {
        GET_ARTICLES_CALL_COUNT.incrementAndGet();
        return ArticleService.findManyByBlog(blog);
    }

    public static int getArticlesCallCount() {
        return GET_ARTICLES_CALL_COUNT.get();
    }

    public static void resetCounts() {
        GET_ARTICLES_CALL_COUNT.set(0);
    }

}
