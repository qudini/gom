package com.qudini.gom;

import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public interface Selection {

    int size();

    Selection subSelection(String prefix);

    boolean contains(String field);

    Stream<String> stream();

    static Selection empty() {
        return new DefaultSelection(emptySet());
    }

}
