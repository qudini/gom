package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Maps {

    public static <K, V, R> Function<Map.Entry<K, V>, R> entry(BiFunction<K, V, R> mapper) {
        return entry -> mapper.apply(entry.getKey(), entry.getValue());
    }

    public static <K, V> Map<K, V> merge(Map<K, V> firstMap, Map<K, V> secondMap) {
        Map<K, V> resultingMap = new HashMap<>();
        resultingMap.putAll(firstMap);
        resultingMap.putAll(secondMap);
        return resultingMap;
    }

}
