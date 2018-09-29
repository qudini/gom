package graphql.gom;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@Getter(PACKAGE)
@EqualsAndHashCode
final class DataLoaderKey<S> {

    private final S source;

    private final Map<String, Object> arguments;

}
