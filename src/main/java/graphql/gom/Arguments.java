package graphql.gom;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public interface Arguments {

    @Nonnull
    <T> T get(String name);

    @Nonnull
    <T> Optional<T> getOptional(String name);

    @Nonnull
    <T> Optional<Optional<T>> getNullable(String name);

    @Nonnull
    <T extends Enum<T>> T getEnum(String name, Class<T> clazz);

    @Nonnull
    <T extends Enum<T>> Optional<T> getOptionalEnum(String name, Class<T> clazz);

    @Nonnull
    <T extends Enum<T>> Optional<Optional<T>> getNullableEnum(String name, Class<T> clazz);

    @Nonnull
    Arguments getInput(String name);

    @Nonnull
    Optional<Arguments> getOptionalInput(String name);

    @Nonnull
    Optional<Optional<Arguments>> getNullableInput(String name);

    int size();

    static DefaultArguments empty() {
        return new DefaultArguments(emptyMap());
    }

}
