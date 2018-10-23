package graphql.gom.example.resolvers.loading;

import graphql.gom.Arguments;
import graphql.gom.Batched;
import graphql.gom.TypeResolver;
import graphql.gom.FieldResolver;
import graphql.gom.example.entities.Article;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.entities.Comment;
import graphql.gom.example.services.BlogService;
import graphql.gom.example.services.CommentService;

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
