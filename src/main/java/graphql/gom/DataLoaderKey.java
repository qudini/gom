package graphql.gom;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

@Getter(PACKAGE)
@EqualsAndHashCode
final class DataLoaderKey {

    @RequiredArgsConstructor(access = PRIVATE)
    @Getter(PACKAGE)
    @EqualsAndHashCode
    static final class Discriminator {

        private final Arguments arguments;

        private final Selection selection;

    }

    private final Object source;

    private final Discriminator discriminator;

    private final Object context;

    DataLoaderKey(DataFetchingEnvironment environment) {
        this.source = environment.getSource();
        this.discriminator = new Discriminator(
                new DefaultArguments(environment),
                new DefaultSelection(environment)
        );
        this.context = environment.getContext();
    }

}
