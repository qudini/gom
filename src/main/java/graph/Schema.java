package graph;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import utils.ResourceReader;

public final class Schema {

    public static final GraphQLSchema INSTANCE = new SchemaGenerator().makeExecutableSchema(
            new SchemaParser().parse(ResourceReader.read("schema.graphql")),
            Wiring.INSTANCE
    );

}
