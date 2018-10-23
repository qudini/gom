package graphql.gom.example.resolvers.fetching;

import graphql.gom.Arguments;
import graphql.gom.TypeResolver;
import graphql.gom.FieldResolver;
import graphql.gom.example.entities.Article;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.entities.Comment;
import graphql.gom.example.services.BlogService;
import graphql.gom.example.services.CommentService;

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
