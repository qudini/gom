package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.function.BinaryOperator;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Reductions {

    public static <T> BinaryOperator<T> failIfDifferent() {
        return (x, y) -> {
            if (x.equals(y)) {
                return x;
            } else {
                throw new IllegalStateException(format(
                        "%s and %s shouldn't have been different",
                        x,
                        y
                ));
            }
        };
    }

    public static <T> BinaryOperator<T> fail() {
        return (x, y) -> {
            throw new IllegalStateException("This combiner shouldn't have been called");
        };
    }

}
