package graphql.gom;

import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
final class DataLoaderRegistrar<R> {

    private final String dataLoaderKey;

    private final Supplier<DataLoader<DataLoaderKey, R>> dataLoaderSupplier;

    void register(DataLoaderRegistry registry) {
        registry.register(dataLoaderKey, dataLoaderSupplier.get());
    }

}
