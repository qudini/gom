# graphql-gom

GOM stands for GraphQL-Object Mapping (just like ORM stands for Object-Relational Mapping). Largely inspired by [graphql-java-tools](https://github.com/graphql-java-kickstart/graphql-java-tools), its purpose is to still allow batching resolvers, but by implementing the recommended approach of [using `DataLoader`s](https://graphql-java.readthedocs.io/en/latest/batching.html). Put simply, GOM will prepare `DataFetcher`s and `DataLoader`s for you, so that you just need to _decorate_ your `RuntimeWiring` and `DataLoaderRegistry` instances.

## Why?

[graphql-java-tools](https://github.com/graphql-java-kickstart/graphql-java-tools) used to cover everything that GOM does, but unfortunately (yet legitimately as not spec conform), [BatchedExecutionStrategy got deprecated](https://github.com/graphql-java/graphql-java/issues/963). The recommended way is now to [use `DataLoader`s](https://graphql-java.readthedocs.io/en/latest/batching.html), but [it's not yet (officially?) supported by graphql-java-tools](https://github.com/graphql-java-kickstart/graphql-java-tools/issues/58). Also, [`DataLoader`s make it way harder to customise the queries to your data source](https://github.com/graphql-java/java-dataloader/issues/26), because they are "DataFetchingEnvironment-agnostic" by design.

So basically, **GOM tries to provide a Java-friendly match between data query optimisation and `DataLoader`s**.

### How?

You know how `BatchLoader`s are supposed to take only _keys_ to allow fetching the corresponding values from your data source? For example:

```java
BatchLoader<Integer, Article> articleBatchLoader = new BatchLoader<Integer, Article>() {
    
    @Override
    public CompletionStage<List<Article>> load(List<Integer> keys) {
        return articleService.getArticles(keys);
    }
    
};
```

Well, what GOM does, is basically "enhancing" those keys by passing instances of `DataLoaderKey` instead (internal class):

```java
class DataLoaderKey {
    
    // the source, got via DataFetchingEnvironment#getSource
    private Object source;
    
    // the arguments, got via DataFetchingEnvironment#getArguments
    private Object arguments;
    
    // the context, got via DataFetchingEnvironment#getContext
    private Object context;
    
}
```

This trick then allows a `BatchLoader` to "group the keys by arguments", and thus call _your resolvers_ as many times as there are distinct arguments, but each time will all the sources.

### Anything else?

I just wanted to add that [graphql-java-tools](https://github.com/graphql-java-kickstart/graphql-java-tools) brings a very nice feature that isn't implemented by GOM (yet?): the validation of your resolvers **on server startup**. GOM will make your server startup successfully anyway, and will only fail **at query runtime** if any mapping is wrong (well, this is actually how [graphql-java](https://github.com/graphql-java/graphql-java) behaves, GOM adds no logic here).

## Specs

### Resolvers

To create a resolver, just annotate a class with `@graphq.gom.Resolver` while passing the GraphQL type this resolver will handle:

```java
@Resolver("Article")
public class ArticleResolver {
    
}
```

Now, say your `Article` GraphQL type has a `blog: Blog!` field, just annotation one of `ArticleResolver`'s methods with `@graphq.gom.Resolving` while passing the field name this method will handle:

```java
@Resolver("Article")
public class ArticleResolver {
    
    @Resolving("blog")
    public ... getArticleBlog(...) {
        ...
    }
    
}
```

A resolver's method takes one or two parameters:

- the source, mandatory,
- the arguments, optional (see the [Arguments](#arguments) section).

Special case for types that have no source (e.g. `Query`), their resolvers' methods will only accept an optional parameter for the arguments (i.e. no source).

A resolver's method can return anything (more details in the [Converters](#convertersmyconvertersinstance) section).

All of the following resolvers are thus valid ones:

```java
@Resolver("Query")
public class QueryResolver {
    
    @Resolving("articles")
    public List<Article> listArticles() {
        return articleService.find();
    }
    
}

@Resolver("Query")
public class QueryResolver {
    
    @Resolving("articles")
    public List<Article> listArticles(Arguments arguments) {
        return articleService.find(arguments);
    }
    
}

@Resolver("Article")
public class ArticleResolver {
    
    @Resolving("blog")
    public Blog getArticleBlog(Article article) {
        return blogService.findByArticle(article);
    }
    
}

@Resolver("Article")
public class ArticleResolver {
    
    @Resolving("blog")
    public Blog getArticleBlog(Article article, Arguments arguments) {
        return blogService.findByArticle(article, arguments);
    }
    
}
```

The above resolvers will have one `DataFetcher` implemented by method.

#### @Batched

To have a `DataLoader` implemented instead of only a `DataFetcher`, use the `@graphq.gom.Batched` annotation in addition to the `@Resolving` one:

```java
@Resolver("Article")
public class ArticleResolver {
    
    @Batched
    @Resolving("blog")
    public ... getArticleBlog(...) {
        ...
    }
    
}
```

What changes:

- the method now takes a `Set<Source>` instead of `Source` directly,
- the method now needs to return `Map<Source, Result>` instead of `Result` directly (`MappedBatchLoader` is used, see [java-dataloader](https://github.com/graphql-java/java-dataloader#returning-a-map-of-results-from-your-batch-loader)).

What doesn't change:

- the arguments are still available as a second parameter.

For example:

```java
@Resolver("Article")
public class ArticleResolver {
    
    @Batched
    @Resolving("blog")
    public Map<Article, Blog> getArticleBlog(Set<Article> articles, Arguments arguments) {
        return blogService.findByArticles(articles, arguments);
    }
    
    @Batched
    @Resolving("comments")
    public Map<Article, List<Comment>> getArticleComments(Set<Article> articles) {
        return commentService.findByArticles(articles);
    }
    
}
```

**Important note:** great care is needed concerning the sources identities: as `@Batched` resolvers take a `Set<Source> sources`, the `Source` class has to implement `equals`/`hashCode` "the right way" (i.e. not leave it to the default `Object`'s, as they are per-instance implemented).

#### Arguments

When asking for the arguments as a second parameter of your resolvers, you'll receive an instance of `graphql.gom.Arguments`. This is basically an abstraction of the value returned by `DataFetchingEnvironment#getArguments` (`Map<String, Object>`). It provides three main methods:

- `<T> T get(String name)`: to be used when an argument is mandatory according to the GraphQL schema (will fail if the returned value is `null` or absent).
- `<T> Optional<T> getOptional(String name)`: to be used when an argument is optional according to the GraphQL schema (an empty `Optional` will be returned if the value is `null` or absent).
- `<T> Optional<Optional<T>> getNullable(String name)`: to be used when you need to make a difference between "no argument given" and "argument given but explicitly set to null". It will return an empty `Optional` if the argument hasn't been found at all, an `Optional` of an empty `Optional` if the argument got found but was `null`, and an `Optional` of an `Optional` of the value otherwise. This is mostly useful when one of your _mutation_ can "optionally update an optional data field":
  - Empty `Optional`? Do nothing.
  - `Optional` of an empty `Optional`? Update the data field by emptying its value (e.g. setting `null`).
  - `Optional` of a valued `Optional`? Update the data field with the wrapped value.

You'll also find:

- `<T extends Enum<T>> T getEnum(String name, Class<T> clazz)` (plus the `Optional` and `Nullable` variants) to deserialise directly into an `Enum`.
- `Arguments getNullInput(String name)` (plus the `Optional` and `Nullable` variants) when dealing with GraphQL `input`s so that you can use `Arguments` for these too.

### Gom

Once you've implemented your resolvers, you then need to create is an instance of `graphql.gom.Gom`. You should create only one instance of it, on server startup (i.e. _not_ per GraphQL query).

```java
Gom gom = Gom
    .newGom(MyGraphQLContext.class)
    .resolvers(myResolverInstances)
    .converters(myConvertersInstance)
    .build();
```

#### .newGom(MyGraphQLContext.class)

GOM needs the context class (the one that you'll pass to `ExecutionInput.Builder#context`) to implement `graphql.gom.DataLoaderRegistryGetter`. The simplest implementation could be:

```java
public class MyGraphQLContext implements DataLoaderRegistryGetter {

    private final DataLoaderRegistry dataLoaderRegistry;
    
    public MyGraphQLContext(DataLoaderRegistry dataLoaderRegistry) {
        this.dataLoaderRegistry = dataLoaderRegistry;
    }
    
    @Override
    public DataLoaderRegistry getDataLoaderRegistry() {
        return this.dataLoaderRegistry;
    }

}
```

`Gom#newGom` then needs to know this class (only to have its generics happy): `.newGom(MyGraphQLContext.class)`.

#### .resolvers(myResolverInstances)

As you can guess, this is where you pass the instances of your resolvers so that they get registered, e.g.:

```java
.resolvers(asList(
    new QueryResolver(),
    new BlogResolver(),
    new ArticleResolver(),
    new CommentResolver()
))
```

#### .converters(myConvertersInstance)

All your resolver methods will return `CompletableFuture`s behind the scene. If their don't, then the returned value will be wrapped into `CompletableFuture#completedFuture` anyway, so don't worry. But what if the returned value is "future-capable", while just needs a "conversion"? This is where GOM's converters are useful.

For example, say you're using [Project Reactor](https://projectreactor.io/)'s reactive types `Mono` and `Flux`, you can provide a converter for both of these types so that the returned value becomes "graphql-java compliant". A converter takes the instance it needs to convert plus the GraphQL query context, and returns the converted value. In this case:

```java
Converters converters = Converters
    .newConverters(MyGraphQLContext.class) // again, just like Gom#newGom, the class of the GraphQL query context
    .converter(Mono.class, (mono, context) -> mono.toFuture())
    .converter(Flux.class, (flux, context) -> flux.collectList().toFuture())
    .build(); 
```

You'll then have to pass `converters` to `Gom#converters` so that they get successfully registered.

The `context` parameter looks a bit superfluous, but it can actually be pretty powerful, especially in cases like this one. If you make `MyGraphQLContext` hold an instance of a Project Reactor's `reactor.util.context.Context`, then you can pass it through to your resolvers transparently (and thus being able to use Spring Security with your resolvers for example):

```java
.converter(Mono.class, (mono, context) -> mono
    .subscriberContext(context.getMySubscriberContext())
    .toFuture())
.converter(Flux.class, (flux, context) -> flux
    .collectList()
    .subscriberContext(context.getMySubscriberContext())
    .toFuture())
```

#### Gom#decorateRuntimeWiringBuilder and Gom#decorateDataLoaderRegistry

Once you have your `Gom` instance created (either stored as a singleton or as a bean in a dependency-injection-aware architecture), when you create your graphql-java's `RuntimeWiring` (on server startup), just call:

```java
gom.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
``` 

When you create your java-dataloader's `DataLoaderRegistry` ([usually at query runtime](https://graphql-java.readthedocs.io/en/latest/batching.html#per-request-data-loaders), depending on how you want the cache to behave), just call:

```java
gom.decorateDataLoaderRegistry(dataLoaderRegistry);
```

You're now good to go!

## Example

This example has been implemented as a proof, see src/test/java/graphql/gom/example.

### Database model

Say you have the following database model:

```java
public class Blog {
    private int id;
    private String name;
    private List<Article> articles;
    // accessors
    // equals/hashCode on id
}

public class Article {
    private int id;
    private String title;
    private Blog blog;
    private List<Comment> comments;
    // accessors
    // equals/hashCode on id
}

public class Comment {
    private int id;
    private String content;
    private Article article;
    // accessors
    // equals/hashCode on id
}
```

As you can see:

- _one_ `Blog` has _many_ `Article`s,
- _one_ `Article` has _one_ `Blog` and _many_ `Comment`s,
- _one_ `Comment` has _one_ `Article`.

### GraphQL schema

Here could be your GraphQL schema:

```graphql
type Query {
    # Lists all the blogs:
    blogs: [Blog!]!
}

type Blog {
    # Gets a blog's id:
    id: Int!
    # Gets a blog's name:
    name: String!
    # Lists all articles of a blog:
    articles: [Article!]!
}

type Article {
    # Gets an article's id:
    id: Int!
    # Gets an article's title:
    title: String!
    # Gets an article's blog:
    blog: Blog!
    # Lists all comments of an article, possibly filtered by content:
    comments(containing: String): [Comment!]!
}

type Comment {
    # Gets a comment's id:
    id: Int!
    # Gets a comment's content:
    content: String!
    # Gets a comment's article:
    article: Article!
}
```

### GraphQL query

A consumer calls your GraphQL endpoint with the following query (intentionally stupidly overfetching):

```text
query {
    blogs {
        name
        articles {
            title
            blog {
                name
            }
            allComments: comments {
                content
                article {
                    title
                }
            }
            commentsContainingFoobar: comments(containing: "foobar") {
                 content
                 article {
                     title
                 }
             }
        }
    }
}
```

For the following examples, let's imagine your DB has two blogs, two articles per blog, and two comments per article. For each article, one of the comment contains "foobar" while the other doesn't.

### Resolving using `DataFetcher`s

A naive implementation could be to implement the resolution of this GraphQL schema via `DataFetcher`s. GOM allows you to do it in a Java-friendly way:

```java
@Resolver("Query")
public class QueryResolver {
    
    private BlogService blogService;
    
    @Resolving("blogs")
    public List<Blog> getBlogs() {
        return blogService.findAll();
    }
    
}

@Resolver("Blog")
public class BlogResolver {
    
    private ArticleService articleService;
    
    @Resolving("articles")
    public List<Article> getArticles(Blog blog) {
        return articleService.findManyByBlog(blog);
    }
    
}

@Resolver("Article")
public class ArticleResolver {
    
    private BlogService blogService;
    
    private CommentService commentService;
    
    @Resolving("blog")
    public Blog getBlog(Article article) {
        return blogService.findOneByArticle(article);
    }
    
    @Resolving("comments")
    public List<Comment> getComments(Article article, Arguments arguments) {
        return commentService.findManyByArticle(article, arguments.getOptional("containing"));
    }
    
}

@Resolver("Comment")
public class CommentResolver {
    
    private ArticleService articleService;
    
    @Resolving("article")
    public Article getArticle(Comment comment) {
        return articleService.findOneByComment(comment);
    }
    
}
```

Let see how many times the above methods were run:

- `QueryResolver#getBlogs`: 1 time (entry point, returning 2 blogs),
- `BlogResolver#getArticles`: 2 times (2 blogs, returning 4 articles),
- `ArticleResolver#getGetBlog`: 4 times (4 articles, returning 4 blogs),
- `ArticleResolver#getComments`: 8 times (4 articles but two calls per article - one filtering by "foobar" and another one without filtering - returning respectively 4 and 8 comments, so 12 comments in total),
- `CommentResolver#getArticle`: 12 times (12 comments, returning 12 articles).

If resolvers are actually doing IOs to a database for example, this can quite quickly go crazy, especially if you have more data than in this small example.

### Resolving using `DataLoader`s

Let's improve this query resolution by using `DataLoader`s instead:

```java
@Resolver("Query")
public class QueryResolver {
    
    private BlogService blogService;
    
    @Resolving("blogs")
    public List<Blog> getBlogs() {
        return blogService.findAll();
    }
    
}

@Resolver("Blog")
public class BlogResolver {
    
    private ArticleService articleService;
    
    @Batched
    @Resolving("articles")
    public Map<Blog, List<Article>> getArticles(Set<Blog> blogs) {
        return articleService.findManyByBlogs(blogs);
    }
    
}

@Resolver("Article")
public class ArticleResolver {
    
    private BlogService blogService;
    
    private CommentService commentService;
    
    @Batched
    @Resolving("blog")
    public Map<Article, Blog> getBlog(Set<Article> articles) {
        return blogService.findManyByArticles(articles);
    }
    
    @Batched
    @Resolving("comments")
    public Map<Article, List<Comment>> getComments(Set<Article> articles, Arguments arguments) {
        return commentService.findManyByArticles(articles, arguments.getOptional("containing"));
    }
    
}

@Resolver("Comment")
public class CommentResolver {
    
    private ArticleService articleService;
    
    @Batched
    @Resolving("article")
    public Map<Comment, Article> getArticle(Set<Comment> comments) {
        return articleService.findManyByComments(comments);
    }
    
}
```

Let compare how many times the above methods were run:

- `QueryResolver#getBlogs`: 1 time (entry point, returning 2 blogs),
- `BlogResolver#getArticles`: 1 time (takes 2 blogs, returns 4 articles),
- `ArticleResolver#getGetBlog`: 1 time (takes 4 articles, returns 4 blogs),
- `ArticleResolver#getComments`: 2 times (takes 4 articles but still two calls - one filtering by "foobar" and another one without filtering - returning respectively 4 and 8 comments, so 12 comments in total),
- `CommentResolver#getArticle`: 1 time (takes 12 comments, returns 12 articles).

See? Ready for the load!
