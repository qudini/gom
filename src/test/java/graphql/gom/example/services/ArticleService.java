package graphql.gom.example.services;

import graphql.gom.example.entities.Article;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.entities.Comment;
import graphql.gom.example.entities.Entity;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.gom.example.entities.Database.ARTICLES_BY_ID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ArticleService {

    public static List<Article> findManyByBlog(Blog blog) {
        return ARTICLES_BY_ID
                .values()
                .stream()
                .filter(article -> article.getBlog().getId() == blog.getId())
                .collect(toList());
    }

    public static Map<Blog, List<Article>> findManyByBlogs(Set<Blog> blogs) {
        Map<Integer, Blog> blogsById = blogs
                .stream()
                .collect(toMap(Entity::getId, identity()));
        Set<Integer> blogIds = blogsById.keySet();
        return ARTICLES_BY_ID
                .values()
                .stream()
                .filter(article -> blogIds.contains(article.getBlog().getId()))
                .collect(groupingBy(article -> blogsById.get(article.getBlog().getId())));
    }

    public static Article findOneByComment(Comment comment) {
        return ARTICLES_BY_ID.get(comment.getArticle().getId());
    }

    public static Map<Comment, Article> findManyByComments(Set<Comment> comments) {
        Set<Integer> articleIds = comments
                .stream()
                .map(comment -> comment.getArticle().getId())
                .collect(toSet());
        Map<Integer, Article> articlesById = ARTICLES_BY_ID
                .values()
                .stream()
                .filter(article -> articleIds.contains(article.getId()))
                .collect(toMap(Entity::getId, identity()));
        return comments
                .stream()
                .collect(toMap(identity(), comment -> articlesById.get(comment.getArticle().getId())));
    }

}
