package com.qudini.gom.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qudini.gom.Gom;
import com.qudini.gom.example.resolvers.QueryResolver;
import com.qudini.gom.example.resolvers.fetching.ArticleResolverByFetching;
import com.qudini.gom.example.resolvers.fetching.BlogResolverByFetching;
import com.qudini.gom.example.resolvers.fetching.CommentResolverByFetching;
import com.qudini.gom.example.resolvers.loading.ArticleResolverByLoading;
import com.qudini.gom.example.resolvers.loading.BlogResolverByLoading;
import com.qudini.gom.example.resolvers.loading.CommentResolverByLoading;
import com.qudini.gom.utils.ResourceReader;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import com.qudini.gom.utils.Context;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NoArgsConstructor;
import org.dataloader.DataLoaderRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.concurrent.CompletableFuture;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static graphql.ExecutionInput.newExecutionInput;
import static graphql.GraphQL.newGraphQL;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PUBLIC;

@NoArgsConstructor(access = PUBLIC)
public final class ExampleTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(INDENT_OUTPUT);

    private static String serialise(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Before
    public void resetCounts() {
        QueryResolver.resetCounts();
        BlogResolverByFetching.resetCounts();
        ArticleResolverByFetching.resetCounts();
        CommentResolverByFetching.resetCounts();
        BlogResolverByLoading.resetCounts();
        ArticleResolverByLoading.resetCounts();
        CommentResolverByLoading.resetCounts();
    }

    private Gom createGom(Object... resolvers) {
        return Gom.newGom()
                .resolvers(asList(resolvers))
                .build();
    }

    private GraphQLSchema createGraphQLSchema(Gom gom) {

        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser()
                .parse(ResourceReader.readResource("/com/qudini/gom/example/example.graphql"));

        RuntimeWiring.Builder runtimeWiringBuilder = newRuntimeWiring();
        gom.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
        RuntimeWiring runtimeWiring = runtimeWiringBuilder.build();

        return new SchemaGenerator().makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiring
        );
    }

    private CompletableFuture<ExecutionResult> runGraphQLQuery(Gom gom, GraphQLSchema graphQLSchema, String query) {

        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        gom.decorateDataLoaderRegistry(dataLoaderRegistry);

        ExecutionInput executionInput = newExecutionInput()
                .context(new Context())
                .query(query)
                .dataLoaderRegistry(dataLoaderRegistry)
                .build();

        GraphQL graphQL = newGraphQL(graphQLSchema)
                .instrumentation(new DataLoaderDispatcherInstrumentation())
                .build();

        return graphQL.executeAsync(executionInput);
    }

    @Test
    public void fetching() throws Exception {

        /*
         * Gom and GraphQLSchema instances must be built on server boot (i.e. not each time a GraphQL query needs to be run).
         * Store them as singleton or as beans in a dependency-injection architecture.
         */

        Gom gom = createGom(
                new QueryResolver(),
                new BlogResolverByFetching(),
                new ArticleResolverByFetching(),
                new CommentResolverByFetching()
        );

        GraphQLSchema graphQLSchema = createGraphQLSchema(gom);

        /*
         * To run a query, you'll need both Gom and GraphQLSchema instances (created on server boot).
         */

        String query = ResourceReader.readResource("/com/qudini/gom/example/example.query");
        ExecutionResult executionResult = runGraphQLQuery(gom, graphQLSchema, query).get();

        /*
         * Checks the results
         */

        String actualResult = serialise(executionResult.toSpecification());
        String expectedResult = ResourceReader.readResource("/com/qudini/gom/example/example.json");
        JSONAssert.assertEquals(expectedResult, actualResult, true);

        Assert.assertEquals(1, QueryResolver.getBlogsCallCount());
        Assert.assertEquals(2, BlogResolverByFetching.getArticlesCallCount());
        Assert.assertEquals(4, ArticleResolverByFetching.getGetBlogCallCount());
        Assert.assertEquals(8, ArticleResolverByFetching.getCommentsCallCount());
        Assert.assertEquals(12, CommentResolverByFetching.getArticleCallCount());

    }

    @Test
    public void loading() throws Exception {

        /*
         * Gom and GraphQLSchema instances must be built on server boot (i.e. not each time a GraphQL query needs to be run).
         * Store them as singleton or as beans in a dependency-injection architecture.
         */

        Gom gom = createGom(
                new QueryResolver(),
                new BlogResolverByLoading(),
                new ArticleResolverByLoading(),
                new CommentResolverByLoading()
        );

        GraphQLSchema graphQLSchema = createGraphQLSchema(gom);

        /*
         * To run a query, you'll need both Gom and GraphQLSchema instances (created on server boot).
         */

        String query = ResourceReader.readResource("/com/qudini/gom/example/example.query");
        ExecutionResult executionResult = runGraphQLQuery(gom, graphQLSchema, query).get();

        /*
         * Checks the results
         */

        String actualResult = serialise(executionResult.toSpecification());
        System.out.println(actualResult);
        String expectedResult = ResourceReader.readResource("/com/qudini/gom/example/example.json");
        JSONAssert.assertEquals(expectedResult, actualResult, true);

        Assert.assertEquals(1, QueryResolver.getBlogsCallCount());
        Assert.assertEquals(1, BlogResolverByLoading.getArticlesCallCount());
        Assert.assertEquals(1, ArticleResolverByLoading.getGetBlogCallCount());
        Assert.assertEquals(2, ArticleResolverByLoading.getCommentsCallCount());
        Assert.assertEquals(1, CommentResolverByLoading.getArticleCallCount());

    }

}
