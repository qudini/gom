package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Methods {

    public static Object invoke(Method method, Object instance, Object... arguments) {
        try {
            return method.invoke(instance, arguments);
        } catch (Exception e) {
            throw new IllegalStateException(
                    format(
                            "An error occurred while invoking %s on %s with arguments %s",
                            method,
                            instance,
                            asList(arguments)
                    ),
                    e
            );
        }
    }

}
