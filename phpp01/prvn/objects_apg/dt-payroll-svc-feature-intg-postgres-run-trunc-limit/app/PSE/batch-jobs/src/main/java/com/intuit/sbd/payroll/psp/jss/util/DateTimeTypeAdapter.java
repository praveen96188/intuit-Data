package com.intuit.sbd.payroll.psp.jss.util;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author dchoudhary1
 * Used to seralize  and deseralize datetime 
 *
 */
public class DateTimeTypeAdapter  implements JsonSerializer<DateTime>, JsonDeserializer<DateTime>  {

    //private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.date();
	private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime().withZoneUTC();

	private final static Logger LOGGER = Logger.getLogger(DateTimeTypeAdapter.class.getName());

	@Override
	public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
        final String dateAsString = json.getAsString();

        return dateAsString.length() == 0 ? null : DATE_FORMAT.parseDateTime(dateAsString);
	}

	@Override
	public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src == null ? "" : DATE_FORMAT.print(src));
	}


}