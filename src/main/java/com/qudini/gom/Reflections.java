package com.qudini.gom;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Reflections {

    public static <T extends Annotation> Optional<Method> getMethodAnnotatedWith(Method method, Class<T> annotationClass) {
        final Optional<Method> annotatedMethod;
        if (method.isAnnotationPresent(annotationClass)) {
            annotatedMethod = Optional.of(method);
        } else {
            annotatedMethod = Optional.of(method.getDeclaringClass())
                    .map(Class::getSuperclass)
                    .map(superClass -> {
                        try {
                            return superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        } catch (NoSuchMethodException e) {
                            return null;
                        }
                    })
                    .flatMap(overriddenMethod -> getMethodAnnotatedWith(overriddenMethod, annotationClass));
        }
        return annotatedMethod;
    }

}
