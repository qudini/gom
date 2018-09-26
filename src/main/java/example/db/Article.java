package example.db;

import java.util.ArrayList;
import java.util.List;

public final class Article extends Entity {

    public final Blog blog;
    public final List<Comment> comments = new ArrayList<>();

    public Article(int id, Blog blog) {
        super(id);
        this.blog = blog;
        blog.articles.add(this);
    }

}
