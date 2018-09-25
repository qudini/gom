package db;

import java.util.ArrayList;
import java.util.List;

public final class Article {

    public final int id;
    public final String title;
    public final Blog blog;
    public final List<Comment> comments = new ArrayList<>();

    public Article(int id, Blog blog) {
        this.id = id;
        this.title = "Article #" + id;
        this.blog = blog;
        blog.articles.add(this);
    }

    @Override
    public String toString() {
        return title;
    }

}
