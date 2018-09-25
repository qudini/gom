package graph;

import db.Article;
import db.Blog;
import db.Comment;
import db.InMemoryDb;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.dataloader.MappedBatchLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dataloader.DataLoader.newMappedDataLoader;

public final class Wiring {

    private static MappedBatchLoader<Integer, Blog> blogsByIdsBatchLoader = ids -> {
        System.out.println("#################### blogsByIdsBatchLoader for ids " + ids);
        return InMemoryDb
                .findAllBlogsByIds(ids)
                .thenApply(
                        blogs -> blogs
                                .stream()
                                .collect(toMap(blog -> blog.id, identity()))
                );
    };

    private static MappedBatchLoader<Integer, Article> articlesByIdsBatchLoader = ids -> {
        System.out.println("#################### articlesByIdsBatchLoader for ids " + ids);
        return InMemoryDb
                .findAllArticlesByIds(ids)
                .thenApply(
                        articles -> articles
                                .stream()
                                .collect(toMap(article -> article.id, identity()))
                );
    };

    private static MappedBatchLoader<Integer, List<Article>> articlesByBlogIdsBatchLoader = blogIds -> {
        System.out.println("#################### articlesByBlogIdsBatchLoader for blog ids " + blogIds);
        return InMemoryDb
                .findAllArticlesByBlogIds(blogIds)
                .thenApply(
                        articles -> articles
                                .stream()
                                .collect(groupingBy(article -> article.blog.id))
                );
    };

    private static MappedBatchLoader<Integer, List<Comment>> commentsByArticleIdsBatchLoader = articleIds -> {
        System.out.println("#################### commentsByArticleIdsBatchLoader for article ids " + articleIds);
        return InMemoryDb
                .findAllCommentsByArticleIds(articleIds)
                .thenApply(
                        articles -> articles
                                .stream()
                                .collect(groupingBy(comment -> comment.article.id))
                );
    };

    public static DataLoader<Integer, Blog> newBlogsByIdsBatchLoader() {
        return newMappedDataLoader(blogsByIdsBatchLoader);
    }

    public static DataLoader<Integer, Article> newArticlesByIdsBatchLoader() {
        return newMappedDataLoader(articlesByIdsBatchLoader);
    }

    public static DataLoader<Integer, List<Article>> newArticlesByBlogIdsBatchLoader() {
        return newMappedDataLoader(articlesByBlogIdsBatchLoader);
    }

    public static DataLoader<Integer, List<Comment>> newCommentsByArticleIdsBatchLoader() {
        return newMappedDataLoader(commentsByArticleIdsBatchLoader);
    }

    public static DataFetcher<CompletableFuture<List<Blog>>> blogsDataFetcher = environment -> {
        System.out.println("#################### blogsDataFetcher");
        return InMemoryDb
                .findAllBlogs()
                .thenApply(ArrayList::new);
    };

    public static DataFetcher<CompletableFuture<Blog>> blogByArticleDataFetcher = environment -> {
        Context context = environment.getContext();
        Article article = environment.getSource();
        System.out.println("#################### blogByArticleDataFetcher for " + article);
        return context
                .getBlogsByIdsBatchLoader()
                .load(article.blog.id);
    };

    public static DataFetcher<CompletableFuture<Article>> articleByCommentDataFetcher = environment -> {
        Context context = environment.getContext();
        Comment comment = environment.getSource();
        System.out.println("#################### articleByCommentDataFetcher for " + comment);
        return context
                .getArticlesByIdsBatchLoader()
                .load(comment.article.id);
    };

    public static DataFetcher<CompletableFuture<List<Article>>> articlesByBlogDataFetcher = environment -> {
        Context context = environment.getContext();
        Blog blog = environment.getSource();
        System.out.println("#################### articlesByBlogDataFetcher for " + blog);
        return context
                .getArticlesByBlogIdsBatchLoader()
                .load(blog.id);
    };

    public static DataFetcher<CompletableFuture<List<Blog>>> commentsByArticleDataFetcher = environment -> {
        Context context = environment.getContext();
        Article article = environment.getSource();
        System.out.println("#################### commentsByArticleDataFetcher for " + article);
        return context
                .getCommentsByArticleIdsBatchLoader()
                .load(article.id);
    };

}
