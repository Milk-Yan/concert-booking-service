package se325.assignment01.concert.service.jaxrs;

import se325.assignment01.concert.service.domain.Concert;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class allows us to receive {@link LocalDateTime} instances as query or path parameters. For example:
 * <code>
 *     public void myWebMethod(@QueryParam("date") LocalDateTimeParam dateParam) {
 *         LocalDateTime date = dateParam.getLocalDateTime();
 *         // ....
 *     }
 * </code>
 * The reason this is required is that classes may only be used as query or path parameters if they are primitive types,
 * enumerated types, or contain a constructor accepting a single String argument. {@link LocalDateTime} meets none of
 * these criteria by default, so this is used as a wrapper.
 */
public class LocalDateTimeParam {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LocalDateTime ltd;

    public LocalDateTimeParam(String ldtString) {
        this.ltd = LocalDateTime.parse(ldtString, FORMATTER);
    }

    public LocalDateTime getLocalDateTime() {
        return ltd;
    }

    @ManyToOne
    @JoinColumn(name = "ID")
    private Concert concert;
}
