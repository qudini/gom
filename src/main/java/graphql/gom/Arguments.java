package graphql.gom;

import javax.annotation.Nonnull;
import java.util.List;
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

    @Nonnull
    List<Arguments> getInputArray(String name);

    @Nonnull
    Optional<List<Arguments>> getOptionalInputArray(String name);

    @Nonnull
    Optional<Optional<List<Arguments>>> getNullableInputArray(String name);

    int size();

    static Arguments empty() {
        return new DefaultArguments(emptyMap());
    }

}
