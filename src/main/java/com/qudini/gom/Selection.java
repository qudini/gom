package com.qudini.gom;

import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public interface Selection {

    int size();

    boolean contains(String field);

    Stream<String> stream();

    Selection subSelection(String prefix);

    static Selection empty() {
        return new DefaultSelection(emptySet());
    }

}
