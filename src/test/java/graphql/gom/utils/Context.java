package graphql.gom.utils;

import graphql.gom.DataLoaderRegistryGetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

import static lombok.AccessLevel.PUBLIC;

@RequiredArgsConstructor(access = PUBLIC)
@Getter
public final class Context implements DataLoaderRegistryGetter {

    private final DataLoaderRegistry dataLoaderRegistry;

}
