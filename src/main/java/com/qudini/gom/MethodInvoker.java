package com.qudini.gom;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MethodInvoker {

    private final Method method;

    private final Object instance;

    @SneakyThrows
    Object invoke(Object... arguments) {
        try {
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException | IllegalArgumentException | NullPointerException | ExceptionInInitializerError e) {
            throw new IllegalStateException(
                    format(
                            "An error occurred while invoking %s on %s with arguments %s",
                            method,
                            instance,
                            asList(arguments)
                    ),
                    e
            );
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    int getParameterCount() {
        return method.getParameterCount();
    }

    boolean hasParameterType(Class<?> parameterType) {
        return asList(method.getParameterTypes()).contains(parameterType);
    }

    List<Annotation> getFirstParameterAnnotations(Class<?> parameterType) {
        int index = asList(method.getParameterTypes()).indexOf(parameterType);
        return index < 0
                ? Collections.emptyList()
                : asList(method.getParameterAnnotations()[index]);
    }

    <T> Optional<T> getFirstParameterAnnotation(Class<?> parameterType, Class<T> annotationType) {
        return getFirstParameterAnnotations(parameterType)
                .stream()
                .filter(annotationType::isInstance)
                .map(annotationType::cast)
                .findFirst();
    }

    @Override
    public String toString() {
        return method.toString();
    }

    static MethodInvoker of(Method method, Object instance) {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return new MethodInvoker(method, instance);
    }

}
