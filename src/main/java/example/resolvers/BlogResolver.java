package example.resolvers;

import example.db.Article;
import example.db.Blog;
import example.db.Repository;
import graphql.gom.inspecting.GraphBatched;
import graphql.gom.inspecting.GraphResolver;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@GraphResolver("Blog")
public final class BlogResolver {

    public static final BlogResolver INSTANCE = new BlogResolver();

    private BlogResolver() {
    }

    @GraphBatched
    public Mono<Map<Blog, List<Article>>> getArticles(Set<Blog> blogs, Optional<String> maybeTitle) {
        return Repository
                .findAllArticlesByBlogIds(blogs.stream().map(blog -> blog.id).collect(toSet()), maybeTitle)
                .collect(groupingBy(article -> article.blog));
    }

}
