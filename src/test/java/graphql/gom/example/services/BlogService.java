package graphql.gom.example.services;

import graphql.gom.example.entities.Article;
import graphql.gom.example.entities.Blog;
import graphql.gom.example.entities.Entity;
import lombok.NoArgsConstructor;

import java.util.*;

import static graphql.gom.example.entities.Database.BLOGS_BY_ID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class BlogService {

    public static List<Blog> findAll() {
        return new ArrayList<>(BLOGS_BY_ID.values());
    }

    public static Blog findOneByArticle(Article article) {
        return BLOGS_BY_ID.get(article.getBlog().getId());
    }

    public static Map<Article, Blog> findManyByArticles(Collection<Article> articles) {
        Set<Integer> blogIds = articles
                .stream()
                .map(article -> article.getBlog().getId())
                .collect(toSet());
        Map<Integer, Blog> blogsById = BLOGS_BY_ID
                .values()
                .stream()
                .filter(blog -> blogIds.contains(blog.getId()))
                .collect(toMap(Entity::getId, identity()));
        return articles
                .stream()
                .collect(toMap(identity(), article -> blogsById.get(article.getBlog().getId())));
    }

}
