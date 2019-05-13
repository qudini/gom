package graphql.gom;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode
final class DefaultArguments implements Arguments {

    private final Map<String, Object> arguments;

    DefaultArguments(Map<String, Object> arguments) {
        this.arguments = unmodifiableMap(arguments);
    }

    DefaultArguments(DataFetchingEnvironment environment) {
        this(environment.getArguments());
    }

    private <T> T getNull(String name) {
        return (T) arguments.get(name);
    }

    @Override
    @Nonnull
    public <T> T get(String name) {
        return requireNonNull(getNull(name));
    }

    @Override
    @Nonnull
    public <T> Optional<T> getOptional(String name) {
        return ofNullable(getNull(name));
    }

    @Override
    @Nonnull
    public <T> Optional<Optional<T>> getNullable(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptional(name))
                : Optional.empty();
    }

    private <T extends Enum<T>> T getNullEnum(String name, Class<T> clazz) {
        try {
            return Enum.valueOf(clazz, get(name));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> T getEnum(String name, Class<T> clazz) {
        return requireNonNull(getNullEnum(name, clazz));
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> Optional<T> getOptionalEnum(String name, Class<T> clazz) {
        return ofNullable(getNullEnum(name, clazz));
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> Optional<Optional<T>> getNullableEnum(String name, Class<T> clazz) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalEnum(name, clazz))
                : Optional.empty();
    }

    private DefaultArguments getNullInput(String name) {
        Map<String, Object> input = getNull(name);
        return input == null ? null : new DefaultArguments(input);
    }

    @Override
    @Nonnull
    public DefaultArguments getInput(String name) {
        return requireNonNull(getNullInput(name));
    }

    @Override
    @Nonnull
    public Optional<Arguments> getOptionalInput(String name) {
        return ofNullable(getNullInput(name));
    }

    @Override
    @Nonnull
    public Optional<Optional<Arguments>> getNullableInput(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalInput(name))
                : Optional.empty();
    }

    @Override
    public int size() {
        return arguments.size();
    }

}
