package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class MapMerger {

    public static <K, V> Map<K, V> merge(Map<K, V> firstMap, Map<K, V> secondMap) {
        Map<K, V> resultingMap = new HashMap<>();
        resultingMap.putAll(firstMap);
        resultingMap.putAll(secondMap);
        return resultingMap;
    }

}
