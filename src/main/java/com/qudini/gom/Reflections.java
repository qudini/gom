package com.qudini.gom;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Reflections {

    public static <T extends Annotation> Optional<Method> getMethodAnnotatedWith(Method method, Class<T> annotationClass) {
        if (method.isAnnotationPresent(annotationClass)) {
            return Optional.of(method);
        }
        Class<?> superClass = method.getDeclaringClass().getSuperclass();
        while (superClass != null) {
            try {
                method = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return getMethodAnnotatedWith(method, annotationClass);
            } catch (NoSuchMethodException e) {
                superClass = superClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

}
