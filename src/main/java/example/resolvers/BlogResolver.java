package example.resolvers;

import example.db.Article;
import example.db.Blog;
import example.db.Repository;
import graphql.gom.GraphBatched;
import graphql.gom.GraphResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@GraphResolver("Blog")
public final class BlogResolver {

    public static final BlogResolver INSTANCE = new BlogResolver();

    private BlogResolver() {
    }

    @GraphBatched
    public CompletableFuture<Map<Blog, List<Article>>> articles(Set<Blog> blogs, Map<String, Object> arguments) {
        return Repository
                .findAllArticlesByBlogIds(
                        blogs.stream().map(blog -> blog.id).collect(toSet()),
                        Optional.ofNullable((String) arguments.get("title"))
                )
                .collect(groupingBy(article -> article.blog))
                .toFuture();
    }

}
