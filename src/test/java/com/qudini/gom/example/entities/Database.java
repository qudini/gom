package com.qudini.gom.example.entities;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Database {

    static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private static <T extends Entity> Collector<T, ?, Map<Integer, T>> toLinkedMap() {
        return toMap(
                Entity::getId,
                identity(),
                (u, v) -> {
                    throw new IllegalStateException(format("Duplicate key %s", u));
                },
                LinkedHashMap::new
        );
    }

    private static <T extends Entity> T get(Map<Integer, T> entities, int index) {
        return new ArrayList<>(entities.values()).get(index);
    }

    public static final Map<Integer, Blog> BLOGS_BY_ID = Stream
            .of(
                    new Blog("Blog #1"),
                    new Blog("Blog #2")
            )
            .collect(toLinkedMap());

    public static final Map<Integer, Article> ARTICLES_BY_ID = Stream
            .of(
                    new Article("Article #11", get(BLOGS_BY_ID, 0)),
                    new Article("Article #12", get(BLOGS_BY_ID, 0)),
                    new Article("Article #21", get(BLOGS_BY_ID, 1)),
                    new Article("Article #22", get(BLOGS_BY_ID, 1))
            )
            .collect(toLinkedMap());

    public static final Map<Integer, Comment> COMMENTS_BY_ID = Stream
            .of(
                    new Comment("Comment #111 foobar", get(ARTICLES_BY_ID, 0)),
                    new Comment("Comment #112", get(ARTICLES_BY_ID, 0)),
                    new Comment("Comment #121", get(ARTICLES_BY_ID, 1)),
                    new Comment("Comment #122 foobar", get(ARTICLES_BY_ID, 1)),
                    new Comment("Comment #211 foobar", get(ARTICLES_BY_ID, 2)),
                    new Comment("Comment #212", get(ARTICLES_BY_ID, 2)),
                    new Comment("Comment #221", get(ARTICLES_BY_ID, 3)),
                    new Comment("Comment #222 foobar", get(ARTICLES_BY_ID, 3))
            )
            .collect(toLinkedMap());

}
