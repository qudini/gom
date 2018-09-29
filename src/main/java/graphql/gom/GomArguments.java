package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@EqualsAndHashCode
public final class GomArguments {

    private final Map<String, Object> arguments;

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name) {
        return ofNullable((T) arguments.get(name));
    }

}
