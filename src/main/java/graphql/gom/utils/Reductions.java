package graphql.gom.utils;

import lombok.NoArgsConstructor;

import java.util.function.BinaryOperator;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Reductions {

    public static <T> BinaryOperator<T> failingCombiner() {
        return (x, y) -> {
            throw new IllegalStateException("This combiner shouldn't have been called");
        };
    }

}
