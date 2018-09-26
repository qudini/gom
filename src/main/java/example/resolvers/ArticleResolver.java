package example.resolvers;

import example.db.Article;
import example.db.Blog;
import example.db.Comment;
import example.db.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public final class ArticleResolver {

    public static final ArticleResolver INSTANCE = new ArticleResolver();

    private ArticleResolver() {
    }

    public Mono<Map<Article, Blog>> getBlog(Set<Article> articles) {
        return Repository
                .findAllBlogsByIds(articles.stream().map(article -> article.blog.id).collect(toSet()))
                .collect(toMap(blog -> blog.id, identity()))
                .map(blogsById -> articles
                        .stream()
                        .collect(toMap(identity(), article -> blogsById.get(article.blog.id)))
                );
    }

    public Mono<Map<Article, List<Comment>>> getComments(Set<Article> articles) {
        return Repository
                .findAllCommentsByArticleIds(articles.stream().map(article -> article.id).collect(toSet()))
                .collect(groupingBy(comment -> comment.article));
    }

}
