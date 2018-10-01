package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@EqualsAndHashCode
public final class Arguments {

    private final Map<String, Object> arguments;

    @SuppressWarnings("unchecked")
    private <T> T getNull(String name) {
        return (T) arguments.get(name);
    }

    @Nonnull
    public <T> T get(String name) {
        return requireNonNull(getNull(name));
    }

    @Nonnull
    public <T> Optional<T> getOptional(String name) {
        return ofNullable(getNull(name));
    }

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

    @Nonnull
    public <T extends Enum<T>> T getEnum(String name, Class<T> clazz) {
        return requireNonNull(getNullEnum(name, clazz));
    }

    @Nonnull
    public <T extends Enum<T>> Optional<T> getOptionalEnum(String name, Class<T> clazz) {
        return ofNullable(getNullEnum(name, clazz));
    }

    @Nonnull
    public <T extends Enum<T>> Optional<Optional<T>> getNullableEnum(String name, Class<T> clazz) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalEnum(name, clazz))
                : Optional.empty();
    }

    private Arguments getNullInput(String name) {
        Map<String, Object> arguments = getNull(name);
        return arguments == null
                ? null
                : new Arguments(arguments);
    }

    @Nonnull
    public Arguments getInput(String name) {
        return requireNonNull(getNullInput(name));
    }

    @Nonnull
    public Optional<Arguments> getOptionalInput(String name) {
        return ofNullable(getNullInput(name));
    }

    @Nonnull
    public Optional<Optional<Arguments>> getNullableInput(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalInput(name))
                : Optional.empty();
    }

}
