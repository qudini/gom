package com.qudini.gom;

import graphql.GraphQLContext;
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

    private final GraphQLContext context;

    DataLoaderKey(DataFetchingEnvironment environment, int selectionDepth) {
        this.source = environment.getSource();
        this.discriminator = new Discriminator(
                new DefaultArguments(environment),
                new DefaultSelection(environment, selectionDepth)
        );
        this.context = environment.getGraphQlContext();
    }

}
