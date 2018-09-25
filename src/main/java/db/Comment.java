package db;

public final class Comment {

    public final int id;
    public final String title;
    public final Article article;

    public Comment(int id, Article article) {
        this.id = id;
        this.title = "Comment #" + id;
        this.article = article;
        article.comments.add(this);
    }

    @Override
    public String toString() {
        return title;
    }

}
