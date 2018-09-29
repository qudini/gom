package graphql.gom;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE, onConstructor = @__(@JsonCreator))
public final class GomArguments {

    private final Map<String, Object> arguments;

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name) {
        return ofNullable((T) arguments.get(name));
    }

}
