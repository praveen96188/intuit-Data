package com.intuit.sbd.payroll.psp.jss.util;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author dchoudhary1
 * Handles converting between json strings and {@link Date}.  
 */
public class DateTypeAdapter implements JsonDeserializer<LocalDate>,JsonSerializer<LocalDate> {
	DateTimeFormatter longFormat;
	DateTimeFormatter shortFormat;
	SimpleDateFormat formatter;
	private final static Logger LOGGER = Logger.getLogger(DateTypeAdapter.class.getName());

	public DateTypeAdapter() {
		//Handles qbo dates with time/zone format: 2015-08-27T14:59:48-07:00
		longFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
		//Handles other qbo dates with out time information: 2015-08-25
		shortFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Override
	public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		String jsonString = json.getAsString();

		// First try parsing the long date format, short format dates will encounter an exception and continue
		try {
			return longFormat.parseLocalDate(jsonString);
		} catch (IllegalArgumentException ignored) {
			LOGGER.warning("ignored"+ignored.getMessage());
		}

		// Date format was not the longFormat so attempt to parse the shortFormat
		try {
			LocalDate localDate = shortFormat.parseLocalDate(jsonString);
			return localDate;
		} catch (IllegalArgumentException exception) {
			throw new RuntimeException("Unable to parse date string " + jsonString, exception);
		}
	}

	@Override
	public  JsonElement serialize(LocalDate date,Type type,JsonSerializationContext jsonSerializationContext) {
		//Need to serialize Date to specific format QBO expects
		String formattedDate = date.toString("yyyy-MM-dd'T'HH:mm:ssZZ");
		return new JsonPrimitive(formattedDate);
	}
}