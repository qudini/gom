package graphql.gom.example.entities;

import lombok.Getter;

@Getter
public final class Comment extends Entity {

    private final String content;
    private final Article article;

    public Comment(String content, Article article) {
        this.content = content;
        this.article = article;
        article.getComments().add(this);
    }

}
