package dk.aarhustech.edu.rainbow.horario;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

class LocalTimeSerializer implements JsonDeserializer<LocalTime>, JsonSerializer<LocalTime> {

    private static final DateTimeFormatter TIME_FORMAT = ISODateTimeFormat.dateTime();

    @Override
    public LocalTime deserialize(final JsonElement je, final Type type,
                                 final JsonDeserializationContext jdc) throws JsonParseException {
        final String dateAsString = je.getAsString();
        if (dateAsString.length() == 0) {
            return null;
        } else {
            return TIME_FORMAT.parseLocalTime(dateAsString);
        }
    }

    @Override
    public JsonElement serialize(final LocalTime src, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        String retVal;
        if (src == null) {
            retVal = "";
        } else {
            retVal = TIME_FORMAT.print(src.toDateTimeToday().withZoneRetainFields(DateTimeZone.UTC));
        }
        return new JsonPrimitive(retVal);
    }
}