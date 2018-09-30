package graphql.gom;

import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@EqualsAndHashCode
public final class GomArguments {

    private final Map<String, Object> arguments;

    @SuppressWarnings("unchecked")
    GomArguments(Map<String, Object> arguments) {
        this.arguments = new HashMap<>(arguments);
        arguments
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof Map)
                .forEach(entry -> this.arguments.put(
                        entry.getKey(),
                        new GomArguments((Map<String, Object>) entry.getValue())
                ));
    }

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

}
