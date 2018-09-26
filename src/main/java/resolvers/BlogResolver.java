package resolvers;

import db.Article;
import db.Blog;
import db.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public final class BlogResolver {

    public static Mono<Map<Blog, List<Article>>> getArticles(Set<Blog> blogs, Optional<String> maybeTitle) {
        return Repository
                .findAllArticlesByBlogIds(blogs.stream().map(blog -> blog.id).collect(toSet()), maybeTitle)
                .collect(groupingBy(article -> article.blog));
    }

}
