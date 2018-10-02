package graphql.gom.example.resolvers.fetching;

import graphql.gom.Resolver;
import graphql.gom.Resolving;
import graphql.gom.example.entities.Article;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.services.ArticleService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Resolver("Blog")
public final class BlogResolverByFetching {

    private static final AtomicInteger GET_ARTICLES_CALL_COUNT = new AtomicInteger();

    @Resolving("articles")
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
