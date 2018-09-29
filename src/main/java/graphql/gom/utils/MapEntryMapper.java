package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class MapEntryMapper {

    public static <K, V, R> Function<Map.Entry<K, V>, R> entry(BiFunction<K, V, R> mapper) {
        return entry -> mapper.apply(entry.getKey(), entry.getValue());
    }

}
