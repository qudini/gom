package graphql.gom;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
final class ResolverMethodInvoker<C extends DataLoaderRegistryGetter> {

    private final Converters<C> converters;

    private Object invoke(Method method, Object instance, Object... arguments) {
        try {
            return method.invoke(instance, arguments);
        } catch (Exception e) {
            throw new GomException(
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

    <R> CompletableFuture<R> invoke(Method method, Object instance, Object source, Arguments arguments, C context) {
        final Object returnedValue;
        switch (method.getParameterCount()) {
            case 0:
                if (source == null) {
                    returnedValue = invoke(method, instance);
                } else {
                    throw new GomException(format(
                            "Method %s doesn't take the source(s) as its first argument, while isn't mapped to the GraphQL type Query",
                            method
                    ));
                }
                break;
            case 1:
                returnedValue = source == null
                        ? invoke(method, instance, arguments)
                        : invoke(method, instance, source);
                break;
            case 2:
                if (source == null) {
                    throw new GomException(format(
                            "Method %s takes the source(s) as its first argument, while is mapped to the GraphQL type Query",
                            method
                    ));
                } else {
                    returnedValue = invoke(method, instance, source, arguments);
                }
                break;
            default:
                int min = source == null
                        ? 0
                        : 1;
                throw new GomException(format(
                        "Method %s should take %d or %d argument(s), while currently expects %s",
                        method,
                        min,
                        min + 1,
                        method.getParameterCount()
                ));
        }
        return converters.convert(returnedValue, context);
    }

}
