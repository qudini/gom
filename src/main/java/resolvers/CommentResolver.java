package resolvers;

import db.Article;
import db.Comment;
import db.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public final class CommentResolver {

    public static final CommentResolver INSTANCE = new CommentResolver();

    private CommentResolver() {
    }

    public Mono<Map<Comment, Article>> getArticle(Set<Comment> comments) {
        return Repository
                .findAllArticlesByIds(comments.stream().map(comment -> comment.article.id).collect(toSet()))
                .collect(toMap(article -> article.id, identity()))
                .map(articlesById -> comments
                        .stream()
                        .collect(toMap(identity(), comment -> articlesById.get(comment.article.id)))
                );
    }

}
