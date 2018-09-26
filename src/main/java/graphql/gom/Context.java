package graphql.gom;

import example.db.Article;
import example.db.Blog;
import example.db.Comment;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;

public final class Context {

    private final DataLoaderRegistry dataLoaderRegistry;

    public Context() {
        dataLoaderRegistry = new DataLoaderRegistry()
                .register("articleToBlogBatchLoader", DataLoaders.articleToBlogBatchLoader())
                .register("commentToArticleBatchLoader", DataLoaders.commentToArticleBatchLoader())
                .register("blogToArticlesBatchLoader", DataLoaders.blogToArticlesBatchLoader())
                .register("articleToCommentsBatchLoader", DataLoaders.articleToCommentsBatchLoader());
    }

    public DataLoaderRegistry getDataLoaderRegistry() {
        return dataLoaderRegistry;
    }

    public DataLoader<Article, Blog> articleToBlogBatchLoader() {
        return dataLoaderRegistry.getDataLoader("articleToBlogBatchLoader");
    }

    public DataLoader<Comment, Article> commentToArticleBatchLoader() {
        return dataLoaderRegistry.getDataLoader("commentToArticleBatchLoader");
    }

    public DataLoader<DataLoaderKey<Blog>, List<Article>> blogToArticlesBatchLoader() {
        return dataLoaderRegistry.getDataLoader("blogToArticlesBatchLoader");
    }

    public DataLoader<Article, List<Comment>> articleToCommentsBatchLoader() {
        return dataLoaderRegistry.getDataLoader("articleToCommentsBatchLoader");
    }

}
