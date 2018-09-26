package graph;

import db.Article;
import db.Blog;
import db.Comment;
import org.dataloader.DataLoader;
import resolvers.ArticleResolver;
import resolvers.BlogResolver;
import resolvers.CommentResolver;
import utils.FutureParalleliser;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.dataloader.DataLoader.newMappedDataLoader;

public final class DataLoaders {

    public static DataLoader<Article, Blog> articleToBlogBatchLoader() {
        return newMappedDataLoader(articles -> ArticleResolver
                .getBlog(articles)
                .toFuture());
    }

    public static DataLoader<Comment, Article> commentToArticleBatchLoader() {
        return newMappedDataLoader(comments -> CommentResolver
                .getArticle(comments)
                .toFuture());
    }

    public static DataLoader<DataLoaderKey<Blog>, List<Article>> blogToArticlesBatchLoader() {
        return newMappedDataLoader(blogKeys -> {
            Map<Map<String, Object>, List<DataLoaderKey<Blog>>> keysByArguments = blogKeys.stream().collect(groupingBy(key -> key.arguments));
            List<CompletableFuture<Map<DataLoaderKey<Blog>, List<Article>>>> futures = keysByArguments
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        Map<Blog, DataLoaderKey<Blog>> keysByBlog = entry.getValue().stream().collect(toMap(key -> key.source, identity()));
                        Set<Blog> blogs = keysByBlog.keySet();
                        Optional<String> maybeTitle = Optional.ofNullable((String) entry.getKey().get("title"));
                        return BlogResolver
                                .getArticles(blogs, maybeTitle)
                                .toFuture()
                                .thenApply(articlesByBlog -> articlesByBlog
                                        .entrySet()
                                        .stream()
                                        .collect(toMap(e -> keysByBlog.get(e.getKey()), Map.Entry::getValue))
                                );
                    })
                    .collect(toList());
            return FutureParalleliser
                    .parallelise(futures)
                    .thenApply(results -> results
                            .stream()
                            .reduce((r1, r2) -> {
                                Map<DataLoaderKey<Blog>, List<Article>> merged = new HashMap<>();
                                merged.putAll(r1);
                                merged.putAll(r2);
                                return merged;
                            })
                            .orElseGet(Collections::emptyMap)
                    );
        });
    }

    public static DataLoader<Article, List<Comment>> articleToCommentsBatchLoader() {
        return newMappedDataLoader(articles -> ArticleResolver
                .getComments(articles)
                .toFuture());
    }

}
