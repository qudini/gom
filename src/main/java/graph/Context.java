package graph;

import db.Article;
import db.Blog;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;

public final class Context {

    private final DataLoaderRegistry dataLoaderRegistry;

    public Context() {
        dataLoaderRegistry = new DataLoaderRegistry()
                .register("articlesByBlogIdsBatchLoader", Wiring.newArticlesByBlogIdsBatchLoader())
                .register("commentsByArticleIdsBatchLoader", Wiring.newCommentsByArticleIdsBatchLoader());
    }

    public DataLoaderRegistry getDataLoaderRegistry() {
        return dataLoaderRegistry;
    }

    public DataLoader<Integer, List<Article>> getArticlesByBlogIdsBatchLoader() {
        return dataLoaderRegistry.getDataLoader("articlesByBlogIdsBatchLoader");
    }

    public DataLoader<Integer, List<Blog>> getCommentsByArticleIdsBatchLoader() {
        return dataLoaderRegistry.getDataLoader("commentsByArticleIdsBatchLoader");
    }

}
