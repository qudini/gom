package db;

import java.util.ArrayList;
import java.util.List;

public final class Blog {

    public final int id;
    public final String title;
    public final List<Article> articles = new ArrayList<>();

    public Blog(int id) {
        this.id = id;
        this.title = "Blog #" + id;
    }

    @Override
    public String toString() {
        return title;
    }

}
