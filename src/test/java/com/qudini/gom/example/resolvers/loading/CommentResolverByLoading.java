package com.qudini.gom.example.resolvers.loading;

import com.qudini.gom.Batched;
import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Article;
import com.qudini.gom.example.entities.Comment;
import com.qudini.gom.example.services.ArticleService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Comment")
public final class CommentResolverByLoading {

    private static final AtomicInteger GET_ARTICLE_CALL_COUNT = new AtomicInteger();

    @Batched
    @FieldResolver("article")
    public Map<Comment, Article> getArticle(Set<Comment> comments) {
        GET_ARTICLE_CALL_COUNT.incrementAndGet();
        return ArticleService.findManyByComments(comments);
    }

    public static int getArticleCallCount() {
        return GET_ARTICLE_CALL_COUNT.get();
    }

    public static void resetCounts() {
        GET_ARTICLE_CALL_COUNT.set(0);
    }

}
