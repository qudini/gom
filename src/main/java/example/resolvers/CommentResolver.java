package example.resolvers;

import example.db.Article;
import example.db.Comment;
import example.db.Repository;
import graphql.gom.GomBatched;
import graphql.gom.GomResolver;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@GomResolver("Comment")
public final class CommentResolver {

    public static final CommentResolver INSTANCE = new CommentResolver();

    private CommentResolver() {
    }

    @GomBatched
    public Mono<Map<Comment, Article>> article(Set<Comment> comments) {
        return Repository
                .findAllArticlesByIds(comments.stream().map(comment -> comment.article.id).collect(toSet()))
                .collect(toMap(article -> article.id, identity()))
                .map(articlesById -> comments
                        .stream()
                        .collect(toMap(identity(), comment -> articlesById.get(comment.article.id)))
                );
    }

}
