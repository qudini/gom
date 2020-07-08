package com.qudini.gom.example.resolvers.loading;

import com.qudini.gom.Arguments;
import com.qudini.gom.Batched;
import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Article;
import com.qudini.gom.example.entities.Blog;
import com.qudini.gom.example.entities.Comment;
import com.qudini.gom.example.services.BlogService;
import com.qudini.gom.example.services.CommentService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Article")
public final class ArticleResolverByLoading {

    private static final AtomicInteger GET_BLOG_CALL_COUNT = new AtomicInteger(0);
    private static final AtomicInteger GET_COMMENTS_CALL_COUNT = new AtomicInteger(0);

    @Batched
    @FieldResolver("blog")
    public Map<Article, Blog> getBlog(Set<Article> articles) {
        GET_BLOG_CALL_COUNT.incrementAndGet();
        return BlogService.findManyByArticles(articles);
    }

    @Batched
    @FieldResolver("comments")
    public Map<Article, List<Comment>> getComments(Set<Article> articles, Arguments arguments) {
        GET_COMMENTS_CALL_COUNT.incrementAndGet();
        return CommentService.findManyByArticles(articles, arguments.getOptional("containing"));
    }

    public static int getGetBlogCallCount() {
        return GET_BLOG_CALL_COUNT.get();
    }

    public static int getCommentsCallCount() {
        return GET_COMMENTS_CALL_COUNT.get();
    }

    public static void resetCounts() {
        GET_BLOG_CALL_COUNT.set(0);
        GET_COMMENTS_CALL_COUNT.set(0);
    }

}
