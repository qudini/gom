package db;

public abstract class Entity {

    public final int id;
    public final String title;

    public Entity(int id) {
        this.id = id;
        this.title = getClass().getSimpleName() + " #" + id;
    }

    @Override
    public final boolean equals(Object obj) {
        return getClass().isInstance(obj) && id == ((Entity) obj).id;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public final String toString() {
        return title;
    }

}
