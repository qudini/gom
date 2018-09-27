package graphql.gom.batching;

import graphql.schema.DataFetchingEnvironment;

import java.util.Map;
import java.util.Objects;

public final class DataLoaderKey<Source> {

    private final Source source;

    private final Map<String, Object> arguments;

    public DataLoaderKey(DataFetchingEnvironment environment) {
        this.source = environment.getSource();
        this.arguments = environment.getArguments();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataLoaderKey) {
            DataLoaderKey other = (DataLoaderKey) obj;
            return source.equals(other.source)
                    && arguments.equals(other.arguments);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, arguments);
    }

    public Source getSource() {
        return source;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

}
