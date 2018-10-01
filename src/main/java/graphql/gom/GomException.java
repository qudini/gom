package graphql.gom;

public final class GomException extends RuntimeException {

    GomException(String message) {
        super(message);
    }

    GomException(String message, Throwable cause) {
        super(message, cause);
    }

}
