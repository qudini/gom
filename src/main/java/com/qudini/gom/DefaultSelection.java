package com.qudini.gom;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode
final class DefaultSelection implements Selection {

    private final Set<String> fields;

    DefaultSelection(Set<String> fields) {
        this.fields = unmodifiableSet(fields);
    }

    DefaultSelection(DataFetchingEnvironment environment, int depth) {
        this(extractFields(environment, depth));
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean contains(String field) {
        return fields.contains(field);
    }

    @Override
    public Stream<String> stream() {
        return fields.stream();
    }

    @Override
    public Selection subSelection(String prefix) {
        Set<String> subFields = fields
                .stream()
                .filter(field -> field.startsWith(prefix))
                .map(field -> field.substring(prefix.length()))
                .collect(toSet());
        return new DefaultSelection(subFields);
    }

    @Override
    public String toString() {
        return fields.toString();
    }

    private static Set<String> extractFields(DataFetchingEnvironment environment, int depth) {
        List<String> globs = buildGlobs(depth);
        String firstGlob = globs.remove(0);
        return environment
                .getSelectionSet()
                .getFields(firstGlob, globs.toArray(new String[0]))
                .stream()
                .map(SelectedField::getQualifiedName)
                .collect(toSet());
    }

    private static List<String> buildGlobs(int depth) {
        return IntStream
                .rangeClosed(1, depth)
                .mapToObj(i -> join("/", nCopies(i, "*")))
                .collect(toList());
    }

}
