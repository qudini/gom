import graphql.gom.DataLoaderRegistryGetter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoaderRegistry;

@RequiredArgsConstructor
@Getter
public final class Context implements DataLoaderRegistryGetter {

    private final DataLoaderRegistry dataLoaderRegistry;

}
