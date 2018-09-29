package example.resolvers;

import example.db.Article;
import example.db.Comment;
import example.db.Repository;
import graphql.gom.GraphBatched;
import graphql.gom.GraphResolver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@GraphResolver("Comment")
public final class CommentResolver {

    public static final CommentResolver INSTANCE = new CommentResolver();

    private CommentResolver() {
    }

    @GraphBatched
    public CompletableFuture<Map<Comment, Article>> article(Set<Comment> comments) {
        return Repository
                .findAllArticlesByIds(comments.stream().map(comment -> comment.article.id).collect(toSet()))
                .collect(toMap(article -> article.id, identity()))
                .map(articlesById -> comments
                        .stream()
                        .collect(toMap(identity(), comment -> articlesById.get(comment.article.id)))
                )
                .toFuture();
    }

}
