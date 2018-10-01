package graphql.gom.utils;

import graphql.gom.DataLoaderRegistryGetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
@Getter
final class Context implements DataLoaderRegistryGetter {

    private final DataLoaderRegistry dataLoaderRegistry;

}
