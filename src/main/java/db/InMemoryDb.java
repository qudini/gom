package db;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class InMemoryDb {

    private static final Map<Integer, Blog> blogs = Stream
            .of(
                    new Blog(1),
                    new Blog(2)
            )
            .collect(toMap(blog -> blog.id, identity()));

    private static final Map<Integer, Article> articles = Stream
            .of(
                    new Article(11, blogs.get(1)),
                    new Article(12, blogs.get(1)),
                    new Article(21, blogs.get(2)),
                    new Article(22, blogs.get(2))
            )
            .collect(toMap(article -> article.id, identity()));

    private static final Map<Integer, Comment> comments = Stream
            .of(
                    new Comment(111, articles.get(11)),
                    new Comment(112, articles.get(11)),
                    new Comment(121, articles.get(12)),
                    new Comment(122, articles.get(12)),
                    new Comment(211, articles.get(21)),
                    new Comment(212, articles.get(21)),
                    new Comment(221, articles.get(22)),
                    new Comment(222, articles.get(22))
            )
            .collect(toMap(comment -> comment.id, identity()));

    public static CompletableFuture<Collection<Blog>> findAllBlogs() {
        return CompletableFuture.completedFuture(blogs.values());
    }

    public static CompletableFuture<Collection<Article>> findAllArticlesByBlogIds(Collection<Integer> blogIds) {
        return CompletableFuture.completedFuture(
                articles
                        .values()
                        .stream()
                        .filter(article -> blogIds.contains(article.blog.id))
                        .collect(toList())
        );
    }

    public static CompletableFuture<Collection<Comment>> findAllCommentsByArticleIds(Collection<Integer> articleIds) {
        return CompletableFuture.completedFuture(
                comments
                        .values()
                        .stream()
                        .filter(comment -> articleIds.contains(comment.article.id))
                        .collect(toList())
        );
    }

}
