package com.qudini.gom;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
