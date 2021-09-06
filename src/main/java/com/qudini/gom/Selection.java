package com.qudini.gom;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public interface Selection {

    int size();

    boolean contains(String field);

    Stream<String> stream();

    Selection subSelection(String prefix);

    static Selection empty() {
        return new DefaultSelection(emptySet());
    }

    static Selection of(String... fields) {
        return new DefaultSelection(Arrays.stream(fields).collect(toSet()));
    }

}
