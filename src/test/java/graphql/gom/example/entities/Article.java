package graphql.gom.example.entities;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class Article extends Entity {

    private final String title;
    private final Blog blog;
    private final List<Comment> comments = new ArrayList<>();

    public Article(String title, Blog blog) {
        this.title = title;
        this.blog = blog;
        blog.getArticles().add(this);
    }

}
