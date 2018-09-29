package example.resolvers;

import example.db.Article;
import example.db.Blog;
import example.db.Repository;
import graphql.gom.GomBatched;
import graphql.gom.GomResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@GomResolver("Blog")
public final class BlogResolver {

    public static final BlogResolver INSTANCE = new BlogResolver();

    private BlogResolver() {
    }

    @GomBatched
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
