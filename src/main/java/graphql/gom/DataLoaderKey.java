package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@Getter(PACKAGE)
@EqualsAndHashCode
final class DataLoaderKey<S, C extends DataLoaderRegistryGetter> {

    private final S source;

    private final Arguments arguments;

    private final C context;

}
