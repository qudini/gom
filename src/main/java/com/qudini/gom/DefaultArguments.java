package com.qudini.gom;

import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
final class DefaultArguments implements Arguments {

    private static final String UNEXPECTED_NULL_ARGUMENT_MESSAGE_FORMAT = "'%s' must not be null";

    private final Map<String, Object> arguments;

    DefaultArguments(Map<String, Object> arguments) {
        this.arguments = unmodifiableMap(arguments);
    }

    DefaultArguments(DataFetchingEnvironment environment) {
        this(environment.getArguments());
    }

    private <T> T getNull(String name) {
        return (T) arguments.get(name);
    }

    @Override
    @Nonnull
    public <T> T get(String name) {
        return requireNonNull(getNull(name), format(UNEXPECTED_NULL_ARGUMENT_MESSAGE_FORMAT, name));
    }

    @Override
    @Nonnull
    public <T> Optional<T> getOptional(String name) {
        return ofNullable(getNull(name));
    }

    @Override
    @Nonnull
    public <T> Optional<Optional<T>> getNullable(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptional(name))
                : Optional.empty();
    }

    private <T extends Enum<T>> T getNullEnum(String name, Class<T> clazz) {
        try {
            return Enum.valueOf(clazz, get(name));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> T getEnum(String name, Class<T> clazz) {
        return requireNonNull(getNullEnum(name, clazz), format(UNEXPECTED_NULL_ARGUMENT_MESSAGE_FORMAT, name));
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> Optional<T> getOptionalEnum(String name, Class<T> clazz) {
        return ofNullable(getNullEnum(name, clazz));
    }

    @Override
    @Nonnull
    public <T extends Enum<T>> Optional<Optional<T>> getNullableEnum(String name, Class<T> clazz) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalEnum(name, clazz))
                : Optional.empty();
    }

    private Arguments getNullInput(String name) {
        Map<String, Object> input = getNull(name);
        return input == null ? null : new DefaultArguments(input);
    }

    @Override
    @Nonnull
    public Arguments getInput(String name) {
        return requireNonNull(getNullInput(name), format(UNEXPECTED_NULL_ARGUMENT_MESSAGE_FORMAT, name));
    }

    @Override
    @Nonnull
    public Optional<Arguments> getOptionalInput(String name) {
        return ofNullable(getNullInput(name));
    }

    @Override
    @Nonnull
    public Optional<Optional<Arguments>> getNullableInput(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalInput(name))
                : Optional.empty();
    }

    private List<Arguments> getNullInputArray(String name) {
        List<Map<String, Object>> inputArray = getNull(name);
        return inputArray == null ? null : inputArray.stream().map(DefaultArguments::new).collect(toList());
    }

    @Override
    @Nonnull
    public List<Arguments> getInputArray(String name) {
        return requireNonNull(getNullInputArray(name), format(UNEXPECTED_NULL_ARGUMENT_MESSAGE_FORMAT, name));
    }

    @Override
    @Nonnull
    public Optional<List<Arguments>> getOptionalInputArray(String name) {
        return ofNullable(getNullInputArray(name));
    }

    @Override
    @Nonnull
    public Optional<Optional<List<Arguments>>> getNullableInputArray(String name) {
        return arguments.containsKey(name)
                ? Optional.of(getOptionalInputArray(name))
                : Optional.empty();
    }

    @Override
    public int size() {
        return arguments.size();
    }

    @Override
    public String toString() {
        return arguments.toString();
    }

}
