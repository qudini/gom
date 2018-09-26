package db;

import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class Repository {

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

    public static Flux<Blog> findAllBlogsByIds(Collection<Integer> ids) {
        System.out.println(">>>>>>>>>> select from Blog where id in " + ids);
        return Flux
                .fromIterable(ids)
                .map(blogs::get);
    }

    public static Flux<Blog> findAllBlogs() {
        System.out.println(">>>>>>>>>> select from Blog");
        return Flux.fromIterable(blogs.values());
    }

    public static Flux<Article> findAllArticlesByIds(Collection<Integer> ids) {
        System.out.println(">>>>>>>>>> select from Article where id in " + ids);
        return Flux
                .fromIterable(ids)
                .map(articles::get);
    }

    public static Flux<Article> findAllArticlesByBlogIds(Collection<Integer> blogIds, Optional<String> maybeTitle) {
        System.out.println(">>>>>>>>>> select from Article a join Blog b where b.id in " + blogIds + maybeTitle.map(title -> " and title like '" + title + "'").orElse(""));
        return Flux
                .fromIterable(articles.values())
                .filter(article -> blogIds.contains(article.blog.id))
                .filter(article -> maybeTitle.map(title -> article.title.toLowerCase().contains(title)).orElse(true));
    }

    public static Flux<Comment> findAllCommentsByArticleIds(Collection<Integer> articleIds) {
        System.out.println(">>>>>>>>>> select from Comment c join Article a where a.id in " + articleIds);
        return Flux
                .fromIterable(comments.values())
                .filter(comment -> articleIds.contains(comment.article.id));
    }

}
