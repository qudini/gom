package db;

import java.util.ArrayList;
import java.util.List;

public final class Blog extends Entity {

    public final List<Article> articles = new ArrayList<>();

    public Blog(int id) {
        super(id);
    }

}
