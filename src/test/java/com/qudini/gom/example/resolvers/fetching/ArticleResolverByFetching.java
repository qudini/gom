package com.qudini.gom.example.resolvers.fetching;

import com.qudini.gom.Arguments;
import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Article;
import com.qudini.gom.example.entities.Blog;
import com.qudini.gom.example.entities.Comment;
import com.qudini.gom.example.services.BlogService;
import com.qudini.gom.example.services.CommentService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Article")
public final class ArticleResolverByFetching {

    private static final AtomicInteger GET_BLOG_CALL_COUNT = new AtomicInteger(0);
    private static final AtomicInteger GET_COMMENTS_CALL_COUNT = new AtomicInteger(0);

    @FieldResolver("blog")
    public Blog getBlog(Article article) {
        GET_BLOG_CALL_COUNT.incrementAndGet();
        return BlogService.findOneByArticle(article);
    }

    @FieldResolver("comments")
    public List<Comment> getComments(Article article, Arguments arguments) {
        GET_COMMENTS_CALL_COUNT.incrementAndGet();
        return CommentService.findManyByArticle(article, arguments.getOptional("containing"));
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
