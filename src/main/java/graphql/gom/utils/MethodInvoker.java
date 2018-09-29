package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class MethodInvoker {

    public static Object invoke(Method method, Object instance, Object... arguments) {
        try {
            return method.invoke(instance, arguments);
        } catch (Exception e) {
            throw new IllegalStateException(e); // FIXME
        }
    }

}
