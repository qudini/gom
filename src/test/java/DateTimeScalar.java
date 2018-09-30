import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public final class DateTimeScalar extends GraphQLScalarType {

    private static final DateTimeFormatter FORMATTER = ISO_OFFSET_DATE_TIME;

    private static final class ZonedDateTimeCoercing implements Coercing<ZonedDateTime, String> {

        private static final String ERROR_FORMAT = "Invalid value '%s' for ZonedDateTime";

        @Override
        public String serialize(Object value) {
            if (value instanceof ZonedDateTime) {
                return FORMATTER.format((ZonedDateTime) value);
            } else {
                throw new CoercingSerializeException(format(ERROR_FORMAT, value));
            }
        }

        @Override
        public ZonedDateTime parseValue(Object value) {
            if (value instanceof String) {
                return ZonedDateTime.parse((String) value, FORMATTER);
            } else {
                throw new CoercingParseValueException(format(ERROR_FORMAT, value));
            }
        }

        @Override
        public ZonedDateTime parseLiteral(Object value) {
            if (value instanceof StringValue) {
                return ZonedDateTime.parse(((StringValue) value).getValue(), FORMATTER);
            } else {
                return null;
            }
        }

    }

    public DateTimeScalar() {
        super("DateTime", "ZonedDateTime", new ZonedDateTimeCoercing());
    }

}
