package graph;

import db.Article;
import db.Blog;
import db.Comment;
import org.dataloader.DataLoader;
import resolvers.ArticleResolver;
import resolvers.BlogResolver;
import resolvers.CommentResolver;

import java.util.List;

import static org.dataloader.DataLoader.newMappedDataLoader;

public final class DataLoaders {

    public static DataLoader<Article, Blog> articleToBlogBatchLoader() {
        return newMappedDataLoader(articles -> {
            System.out.println("####################");
            System.out.println("#################### newArticleToBlogBatchLoader for articles " + articles);
            System.out.println("####################");
            return ArticleResolver
                    .getBlog(articles)
                    .toFuture();
        });
    }

    public static DataLoader<Comment, Article> commentToArticleBatchLoader() {
        return newMappedDataLoader(comments -> {
            System.out.println("####################");
            System.out.println("#################### newCommentToArticleBatchLoader for comments " + comments);
            System.out.println("####################");
            return CommentResolver
                    .getArticle(comments)
                    .toFuture();
        });
    }

    public static DataLoader<Blog, List<Article>> blogToArticlesBatchLoader() {
        return newMappedDataLoader(blogs -> {
            System.out.println("####################");
            System.out.println("#################### newBlogToArticlesBatchLoader for blogs " + blogs);
            System.out.println("####################");
            return BlogResolver
                    .getArticles(blogs)
                    .toFuture();
        });
    }

    public static DataLoader<Article, List<Comment>> articleToCommentsBatchLoader() {
        return newMappedDataLoader(articles -> {
            System.out.println("####################");
            System.out.println("#################### newArticleToCommentsBatchLoader for articles " + articles);
            System.out.println("####################");
            return ArticleResolver
                    .getComments(articles)
                    .toFuture();
        });
    }

}
