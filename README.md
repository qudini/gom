
# GOM

GOM (GraphQL-Object Mapping) batches your resolvers in order to reduce your data queries, while still allowing them to be customised depending on the arguments and selection.

See [qudini-reactive](https://github.com/qudini/qudini-reactive/tree/master/qudini-reactive-graphql) for an example of how it can be used with Spring Boot and Spring WebFlux.

## Overview

Originally, [`BatchLoader`s are "DataFetchingEnvironment-agnostic" by design](https://github.com/graphql-java/java-dataloader/issues/26), meaning they take only _keys_ to allow fetching the corresponding values from your data source, for example:

```java
BatchLoader<Integer, Article> articleBatchLoader = new BatchLoader<Integer, Article>() {
    
    @Override
    public CompletionStage<List<Article>> load(List<Integer> keys) {
        return articleService.getArticles(keys);
    }
    
};
```

GOM "enhances" those keys by passing instances of `DataLoaderKey` instead (internal class):

```java
class DataLoaderKey {
    
    // the source (DataFetchingEnvironment#getSource)
    private Object source;
    
    // the arguments (DataFetchingEnvironment#getArguments)
    private Object arguments;
    
    // the selection (DataFetchingEnvironment#getSelectionSet)
    private Object selection;
    
    // the context (DataFetchingEnvironment#getGraphQlContext)
    private Object context;
    
}
```

This allows a `BatchLoader` to "group the keys by arguments and selection", and call _your resolvers_ as many times as there are distinct arguments and selection, but each time will all the sources.

GOM will prepare [`DataFetcher`s](https://www.graphql-java.com/documentation/master/data-fetching/) and [`DataLoader`s](https://www.graphql-java.com/documentation/master/batching/) for you, so that you just need to _decorate_ your `RuntimeWiring` and `DataLoaderRegistry` instances.

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.qudini/qudini-gom.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.qudini%22%20AND%20a:%22qudini-gom%22)

```xml
<dependency>
    <groupId>com.qudini</groupId>
    <artifactId>qudini-gom</artifactId>
    <version>${qudini-gom.version}</version>
</dependency>
```

## Usage

### Resolvers

To create a resolver, just annotate a class with `@com.qudini.gom.TypeResolver` while passing the GraphQL type this resolver handles:

```java
@TypeResolver("Article")
public class ArticleResolver {
    
}
```

Now, say your `Article` GraphQL type has a `blog: Blog!` field, just annotate one of `ArticleResolver`'s methods with `@com.qudini.gom.FieldResolver` while passing the field name this method handles:

```java
@TypeResolver("Article")
public class ArticleResolver {
    
    @FieldResolver("blog")
    public ... getArticleBlog(...) {
        ...
    }
    
}
```

A resolver's method takes one, two or three parameters, **in this specific order**:

1. the `source`, mandatory,
2. the `arguments`, optional (see the [Arguments](#arguments) section).
3. the `selection`, optional (see the [Selection](#selection) section).

Special case for types that have no `source` (e.g. `Query`): their resolver methods will only accept `arguments` and/or `selection`.

A resolver method can return anything (more details in the [Converters](#convertersmyconvertersinstance) section).

The following resolvers are all valid ones:

```java
@TypeResolver("Query")
public class QueryResolver {
    
    @FieldResolver("articles")
    public List<Article> listArticles() {
        return articleService.find();
    }
    
}

@TypeResolver("Query")
public class QueryResolver {
    
    @FieldResolver("articles")
    public List<Article> listArticles(Arguments arguments) {
        return articleService.find(arguments);
    }
    
}

@TypeResolver("Query")
public class QueryResolver {
    
    @FieldResolver("articles")
    public List<Article> listArticles(Selection selection) {
        return articleService.find(selection);
    }
    
}

@TypeResolver("Query")
public class QueryResolver {
    
    @FieldResolver("articles")
    public List<Article> listArticles(Arguments arguments, Selection selection) {
        return articleService.find(arguments, selection);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    @FieldResolver("blog")
    public Blog getArticleBlog(Article article) {
        return blogService.findByArticle(article);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    @FieldResolver("blog")
    public Blog getArticleBlog(Article article, Arguments arguments) {
        return blogService.findByArticle(article, arguments);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    @FieldResolver("blog")
    public Blog getArticleBlog(Article article, Selection selection) {
        return blogService.findByArticle(article, selection);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    @FieldResolver("blog")
    public Blog getArticleBlog(Article article, Arguments arguments, Selection selection) {
        return blogService.findByArticle(article, arguments, selection);
    }
    
}
```

The above resolvers will have one `DataFetcher` implemented for each declared method.

#### @Batched

To have a `DataLoader` implemented instead of only a `DataFetcher`, use the `@com.qudini.gom.Batched` annotation in addition to the `@FieldResolver` one:

```java
@TypeResolver("Article")
public class ArticleResolver {
    
    @Batched
    @FieldResolver("blog")
    public ... getArticleBlog(...) {
        ...
    }
    
}
```

What changes:

- the method now takes a `Set<Source>` instead of `Source` directly,
- the method now needs to return `Map<Source, Result>` instead of `Result` directly (`MappedBatchLoader` is used, see [java-dataloader](https://github.com/graphql-java/java-dataloader#returning-a-map-of-results-from-your-batch-loader)).

What doesn't change:

- the `arguments` and `selection` are still available as parameters.

For example:

```java
@TypeResolver("Article")
public class ArticleResolver {
    
    @Batched
    @FieldResolver("blog")
    public Map<Article, Blog> getArticleBlog(Set<Article> articles, Arguments arguments, Selection selection) {
        return blogService.findByArticles(articles, arguments, selection);
    }
    
    @Batched
    @FieldResolver("comments")
    public Map<Article, List<Comment>> getArticleComments(Set<Article> articles) {
        return commentService.findByArticles(articles);
    }
    
}
```

**Important note:** as `@Batched` resolvers take a `Set<Source>`, the `Source` class has to implement `equals`/`hashCode` carefully (i.e. not leave it to the default `Object`'s, as it is per-instance implemented).

#### Arguments

When requesting the `arguments` as a parameter of your resolvers, you will receive an instance of `graphql.gom.Arguments`. This is basically an abstraction of the value returned by `DataFetchingEnvironment#getArguments` (`Map<String, Object>`). It provides three main methods:

- `<T> T get(String name)`: to be used when an argument is mandatory according to the GraphQL schema (will fail if the returned value is `null` or absent).
- `<T> Optional<T> getOptional(String name)`: to be used when an argument is optional according to the GraphQL schema (an empty `Optional` will be returned if the value is `null` or absent).
- `<T> Optional<Optional<T>> getNullable(String name)`: to be used when you need to make a difference between "no argument given" and "argument given but explicitly set to null". It will return an empty `Optional` if the argument hasn't been found at all, an `Optional` of an empty `Optional` if the argument got found but was `null`, and an `Optional` of an `Optional` of the value otherwise. This is mostly useful when one of your _mutation_ can "optionally update an optional data field":
  - Empty `Optional`? Do nothing.
  - `Optional` of an empty `Optional`? Update the data field by emptying its value (e.g. setting `null`).
  - `Optional` of a valued `Optional`? Update the data field with the wrapped value.

You will also find:

- `<T extends Enum<T>> T getEnum(String name, Class<T> clazz)` and `<T extends Enum<T>> List<T> getEnumArray(String name, Class<T> clazz)` (plus the `Optional` and `Nullable` variants) to deserialise directly into an `Enum`.
- `Arguments getInput(String name)` and `List<Arguments> getInputArray(String name)` (plus the `Optional` and `Nullable` variants) when dealing with GraphQL `input`s.

#### Selection

When requesting the `selection` as a parameter of your resolvers, you will receive an instance of `graphql.gom.Selection`, which exposes the following methods:

- `boolean contains(String field)`: returns `true` if the given field is part of the selection.
- `Stream<String> stream()`: streams the selected fields.
- `Selection subSelection(String prefix)`: returns a new selection with the fields starting with the given `prefix` (those matching fields will have the given `prefix` removed, see the [@Depth](#depth) section).

For example, given the following query:

```text
article {
    id
    title
}
```

The `selection` injected in the resolver of `article` will behave the following way:

- `selection.contains("id")`: `true`
- `selection.stream().anyMatch("title"::equals)`: `true`
- `selection.contains("foobar")`: `false`

##### @Depth

By default, the selection will have a depth of 1, meaning the nested selections won't be taken into account. For example, given the following query:

```text
article {
    id
    title
    comments {
        content
    }
}
```

```java
@FieldResolver("article")
public Article getArticle(Selection selection) {...}
```

Those above `selection` will only contain `id`, `title` and `comments`. A resolver on `comments` though, would receive the `content` field in its `selection`.

To access `comments/content` in this parent `selection` directly, annotate the parameter with `@com.qudini.gom.Depth`:

```java
@FieldResolver("article")
public Article getArticle(@Depth(2) Selection selection) {...}
```

Those above `selection` will then contain `id`, `title`, `comments`, **as well as `comments/content`**. You can then use `selection.subSelection("comments/")` to receive a selection with the children of `comments/` only (i.e. `content` in this case).

This can end up particularly useful when doing [cursor-based pagination](https://graphql.org/learn/pagination/):

```text
articles(first: 10) {
    edges {
        node {
            title
        }
    }
}
```

```java
@FieldResolver("articles")
public Article getPaginatedArticles(@Depth(3) Selection selection) {
    Selection nodeSelection = selection.subSelection("edges/node/");
    nodeSelection.contains("title"); // true
    ...
}
```


### Gom

Once you've implemented your resolvers, you then need to create is an instance of `graphql.gom.Gom`. You should create only one instance of it, on server startup (i.e. _not_ per GraphQL query).

```java
Gom gom = Gom
    .newGom()
    .resolvers(myResolverInstances)
    .converters(myConvertersInstance)
    .build();
```

#### .resolvers(myResolverInstances)

This is where you pass the instances of your resolvers so that they get registered, e.g.:

```java
.resolvers(asList(
    new QueryResolver(),
    new BlogResolver(),
    new ArticleResolver(),
    new CommentResolver()
))
```

#### .converters(myConvertersInstance)

All your resolver methods will return `CompletableFuture`s behind the scene (if they don't, the returned value will be wrapped into `CompletableFuture#completedFuture` automatically). GOM's converters are useful if the returned value is "future-capable" but just needs a "conversion".

For example, say you're using [Project Reactor](https://projectreactor.io/)'s reactive types `Mono` and `Flux`, you can provide a converter for both of these types so that the returned value becomes "graphql-java compliant". A converter takes the instance it needs to convert plus the GraphQL query context, and returns the converted value. In this case:

```java
Converters converters = Converters
    .newConverters()
    .converter(Mono.class, (mono, context) -> mono.toFuture())
    .converter(Flux.class, (flux, context) -> flux.collectList().toFuture())
    .build(); 
```

You will then have to pass these `converters` to `Gom#converters` so that they get successfully registered.

The `context` parameter (of type `graphql.GraphQLContext`) looks a bit superfluous, but it can actually be pretty powerful, especially in cases like the above one: when building your `ExecutionInput`, if you make the GraphQL context hold an instance of Project Reactor's current `reactor.util.context.Context` thanks to `ExecutionInput.Builder#graphQLContext(...)`, you can pass it through to your resolvers transparently, which will then allow using Spring Security annotations on your resolvers for example:

```java
.converter(Mono.class, (mono, context) -> mono.contextWrite(context.get(REACTOR_CONTEXT_KEY)).toFuture())
.converter(Flux.class, (flux, context) -> flux.collectList())
```

#### Gom#decorateRuntimeWiringBuilder and Gom#decorateDataLoaderRegistry

Once you have your `Gom` instance created (either stored as a singleton or as a bean in a dependency-injection-aware architecture), when you create your graphql-java's `RuntimeWiring` (on server startup), just call:

```java
gom.decorateRuntimeWiringBuilder(runtimeWiringBuilder);
``` 

When you create your java-dataloader's `DataLoaderRegistry` ([usually at query runtime](https://www.graphql-java.com/documentation/master/batching/#per-request-data-loaders), depending on how you want the cache to behave), just call:

```java
gom.decorateDataLoaderRegistry(dataLoaderRegistry);
```

You're now good to go!

### Other utilities

According to the [GraphQL Cursor Connections Specification](https://relay.dev/graphql/connections.htm), predefined types have been made available:

- `com.qudini.gom.paging.Connection`
- `com.qudini.gom.paging.Edge`
- `com.qudini.gom.paging.PageArguments`
- `com.qudini.gom.paging.PageInfo`

## Example

This example has been implemented as a proof, see [src/test/java/graphql/gom/example](https://github.com/qudini/gom/tree/master/src/test/java/com/qudini/gom/example).

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

A consumer calls your GraphQL endpoint with the following query (intentionally overfetching):

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
@TypeResolver("Query")
public class QueryResolver {
    
    private BlogService blogService;
    
    @FieldResolver("blogs")
    public List<Blog> getBlogs() {
        return blogService.findAll();
    }
    
}

@TypeResolver("Blog")
public class BlogResolver {
    
    private ArticleService articleService;
    
    @FieldResolver("articles")
    public List<Article> getArticles(Blog blog) {
        return articleService.findManyByBlog(blog);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    private BlogService blogService;
    
    private CommentService commentService;
    
    @FieldResolver("blog")
    public Blog getBlog(Article article) {
        return blogService.findOneByArticle(article);
    }
    
    @FieldResolver("comments")
    public List<Comment> getComments(Article article, Arguments arguments) {
        return commentService.findManyByArticle(article, arguments.getOptional("containing"));
    }
    
}

@TypeResolver("Comment")
public class CommentResolver {
    
    private ArticleService articleService;
    
    @FieldResolver("article")
    public Article getArticle(Comment comment) {
        return articleService.findOneByComment(comment);
    }
    
}
```

Let's see how many times the above methods were run:

- `QueryResolver#getBlogs`: 1 time (entry point, returning 2 blogs),
- `BlogResolver#getArticles`: 2 times (2 blogs, returning 4 articles),
- `ArticleResolver#getBlog`: 4 times (4 articles, returning 4 blogs),
- `ArticleResolver#getComments`: 8 times (4 articles but two calls per article - one filtering by "foobar" and another one without filtering - returning respectively 4 and 8 comments, so 12 comments in total),
- `CommentResolver#getArticle`: 12 times (12 comments, returning 12 articles).

If resolvers are actually doing IOs to a database for example, this can quite quickly go crazy, especially if you have more data than in this small example.

### Resolving using `DataLoader`s

Let's improve this query resolution by using `DataLoader`s instead:

```java
@TypeResolver("Query")
public class QueryResolver {
    
    private BlogService blogService;
    
    @FieldResolver("blogs")
    public List<Blog> getBlogs() {
        return blogService.findAll();
    }
    
}

@TypeResolver("Blog")
public class BlogResolver {
    
    private ArticleService articleService;
    
    @Batched
    @FieldResolver("articles")
    public Map<Blog, List<Article>> getArticles(Set<Blog> blogs) {
        return articleService.findManyByBlogs(blogs);
    }
    
}

@TypeResolver("Article")
public class ArticleResolver {
    
    private BlogService blogService;
    
    private CommentService commentService;
    
    @Batched
    @FieldResolver("blog")
    public Map<Article, Blog> getBlog(Set<Article> articles) {
        return blogService.findManyByArticles(articles);
    }
    
    @Batched
    @FieldResolver("comments")
    public Map<Article, List<Comment>> getComments(Set<Article> articles, Arguments arguments) {
        return commentService.findManyByArticles(articles, arguments.getOptional("containing"));
    }
    
}

@TypeResolver("Comment")
public class CommentResolver {
    
    private ArticleService articleService;
    
    @Batched
    @FieldResolver("article")
    public Map<Comment, Article> getArticle(Set<Comment> comments) {
        return articleService.findManyByComments(comments);
    }
    
}
```

Let's compare how many times the above methods were now run:

- `QueryResolver#getBlogs`: 1 time (entry point, returning 2 blogs),
- `BlogResolver#getArticles`: 1 time (takes 2 blogs, returns 4 articles),
- `ArticleResolver#getGetBlog`: 1 time (takes 4 articles, returns 4 blogs),
- `ArticleResolver#getComments`: 2 times (takes 4 articles but still two calls - one filtering by "foobar" and another one without filtering - returning respectively 4 and 8 comments, so 12 comments in total),
- `CommentResolver#getArticle`: 1 time (takes 12 comments, returns 12 articles).

See? Ready for the load!
