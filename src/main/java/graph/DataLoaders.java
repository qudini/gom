package graph;

import db.Article;
import db.Blog;
import db.Comment;
import db.Db;
import org.dataloader.DataLoader;

import java.util.List;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.dataloader.DataLoader.newMappedDataLoader;

public final class DataLoaders {

    public static DataLoader<Integer, Blog> newBlogsByIdsBatchLoader() {
        return newMappedDataLoader(ids -> {
            System.out.println("#################### blogsByIdsBatchLoader for ids " + ids);
            return Db
                    .findAllBlogsByIds(ids)
                    .collect(toMap(blog -> blog.id, identity()))
                    .toFuture();
        });
    }

    public static DataLoader<Integer, Article> newArticlesByIdsBatchLoader() {
        return newMappedDataLoader(ids -> {
            System.out.println("#################### articlesByIdsBatchLoader for ids " + ids);
            return Db
                    .findAllArticlesByIds(ids)
                    .collect(toMap(article -> article.id, identity()))
                    .toFuture();
        });
    }

    public static DataLoader<Integer, List<Article>> newArticlesByBlogIdsBatchLoader() {
        return newMappedDataLoader(blogIds -> {
            System.out.println("#################### articlesByBlogIdsBatchLoader for blog ids " + blogIds);
            return Db
                    .findAllArticlesByBlogIds(blogIds)
                    .collect(groupingBy(article -> article.blog.id))
                    .toFuture();
        });
    }

    public static DataLoader<Integer, List<Comment>> newCommentsByArticleIdsBatchLoader() {
        return newMappedDataLoader(articleIds -> {
            System.out.println("#################### commentsByArticleIdsBatchLoader for article ids " + articleIds);
            return Db
                    .findAllCommentsByArticleIds(articleIds)
                    .collect(groupingBy(comment -> comment.article.id))
                    .toFuture();
        });
    }

}
