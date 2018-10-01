package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@EqualsAndHashCode
public final class Arguments {

    private final Map<String, Object> arguments;

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) arguments.get(name);
    }

    public <T> Optional<T> getOptional(String name) {
        return ofNullable(get(name));
    }

    public <T> Optional<Optional<T>> getNullable(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptional(name))
                : Optional.empty();
    }

    public <T extends Enum<T>> T getEnum(String name, Class<T> clazz) {
        try {
            return Enum.valueOf(clazz, get(name));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public <T extends Enum<T>> Optional<T> getOptionalEnum(String name, Class<T> clazz) {
        return ofNullable(getEnum(name, clazz));
    }

    public <T extends Enum<T>> Optional<Optional<T>> getNullableEnum(String name, Class<T> clazz) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalEnum(name, clazz))
                : Optional.empty();
    }

    public Arguments getInput(String name) {
        Map<String, Object> arguments = get(name);
        return arguments == null
                ? null
                : new Arguments(arguments);
    }

    public Optional<Arguments> getOptionalInput(String name) {
        return ofNullable(getInput(name));
    }

    public Optional<Optional<Arguments>> getNullableInput(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalInput(name))
                : Optional.empty();
    }

}
