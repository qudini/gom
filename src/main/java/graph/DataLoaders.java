package graph;

import db.Article;
import db.Blog;
import db.Comment;
import db.InMemoryDb;
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
            return InMemoryDb
                    .findAllBlogsByIds(ids)
                    .thenApply(
                            blogs -> blogs
                                    .stream()
                                    .collect(toMap(blog -> blog.id, identity()))
                    );
        });
    }

    public static DataLoader<Integer, Article> newArticlesByIdsBatchLoader() {
        return newMappedDataLoader(ids -> {
            System.out.println("#################### articlesByIdsBatchLoader for ids " + ids);
            return InMemoryDb
                    .findAllArticlesByIds(ids)
                    .thenApply(
                            articles -> articles
                                    .stream()
                                    .collect(toMap(article -> article.id, identity()))
                    );
        });
    }

    public static DataLoader<Integer, List<Article>> newArticlesByBlogIdsBatchLoader() {
        return newMappedDataLoader(blogIds -> {
            System.out.println("#################### articlesByBlogIdsBatchLoader for blog ids " + blogIds);
            return InMemoryDb
                    .findAllArticlesByBlogIds(blogIds)
                    .thenApply(
                            articles -> articles
                                    .stream()
                                    .collect(groupingBy(article -> article.blog.id))
                    );
        });
    }

    public static DataLoader<Integer, List<Comment>> newCommentsByArticleIdsBatchLoader() {
        return newMappedDataLoader(articleIds -> {
            System.out.println("#################### commentsByArticleIdsBatchLoader for article ids " + articleIds);
            return InMemoryDb
                    .findAllCommentsByArticleIds(articleIds)
                    .thenApply(
                            articles -> articles
                                    .stream()
                                    .collect(groupingBy(comment -> comment.article.id))
                    );
        });
    }

}
