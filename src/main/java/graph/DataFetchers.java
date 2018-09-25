package graph;

import db.Article;
import db.Blog;
import db.Comment;
import db.InMemoryDb;
import graphql.schema.DataFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DataFetchers {

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
