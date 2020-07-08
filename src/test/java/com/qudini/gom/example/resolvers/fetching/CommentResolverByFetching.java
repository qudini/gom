package com.qudini.gom.example.resolvers.fetching;

import com.qudini.gom.FieldResolver;
import com.qudini.gom.TypeResolver;
import com.qudini.gom.example.entities.Article;
import com.qudini.gom.example.entities.Comment;
import com.qudini.gom.example.services.ArticleService;

import java.util.concurrent.atomic.AtomicInteger;

@TypeResolver("Comment")
public final class CommentResolverByFetching {

    private static final AtomicInteger GET_ARTICLE_CALL_COUNT = new AtomicInteger();

    @FieldResolver("article")
    public Article getArticle(Comment comment) {
        GET_ARTICLE_CALL_COUNT.incrementAndGet();
        return ArticleService.findOneByComment(comment);
    }

    public static int getArticleCallCount() {
        return GET_ARTICLE_CALL_COUNT.get();
    }

    public static void resetCounts() {
        GET_ARTICLE_CALL_COUNT.set(0);
    }

}
