package graphql.gom;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode
final class DefaultSelection implements Selection {

    private final Set<String> fields;

    DefaultSelection(Set<String> fields) {
        this.fields = unmodifiableSet(fields);
    }

    DefaultSelection(DataFetchingEnvironment environment) {
        this.fields = environment
                .getSelectionSet()
                .getFields("*")
                .stream()
                .map(SelectedField::getName)
                .collect(toSet());
    }

    @Override
    public boolean contains(String field) {
        return fields.contains(field);
    }

    @Override
    public Stream<String> stream() {
        return fields.stream();
    }

}
