package example.db;

public final class Comment extends Entity {

    public final Article article;

    public Comment(int id, Article article) {
        super(id);
        this.article = article;
        article.comments.add(this);
    }

}
