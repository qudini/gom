package graph;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;

import java.io.File;

public final class Schema {

    public static final GraphQLSchema INSTANCE = new SchemaGenerator().makeExecutableSchema(
            new SchemaParser().parse(new File(
                    Schema
                            .class
                            .getClassLoader()
                            .getResource("schema.graphql")
                            .getFile()
            )),
            Wiring.INSTANCE
    );

}
