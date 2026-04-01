package com.intuit.spc.foundations.portabilitySpecific.text;

import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import java.util.Locale;

/**
* The SpcfLocaleInfoUtility is an Spcf internal class
* that is used to link SpecLocaleInfo with 
* Java's Locale and C#'s CultureInfo classes
*/
public class SpcfLocaleInfoUtility 
{
	/**
	 * Constructs a Locale from the localeInfo.
	 * If param is null, it returns the current locale 
	 */
	static public Locale getLocale(SpcfLocaleInfo localeInfo)
	{
		if (localeInfo == null)
		{
			return Locale.getDefault();
		}
		
		if (localeInfo.getCountry() != null)
		{
			return new Locale(localeInfo.getLanguage(), localeInfo.getCountry());
		}
		return new Locale(localeInfo.getLanguage());
	}

	/**
	 * Returns the language2-country2 of the default Locale
	 * (i.e. Locale.getDefault()) 
	 */
	static public String getDefaultLanguage()
	{
		return Locale.getDefault().getLanguage();
	}

	/**
	 * Returns the country of the default Locale 
	 * (i.e. Locale.getDefault()) 
	 */
	static public String getDefaultCountry()
	{
		return Locale.getDefault().getCountry();
	}

	/**
	 * Returns the English display name as language (country) from a localeInfo,
	 * or null if the argument is null 
	 */
	static public String getDisplayName(SpcfLocaleInfo localeInfo)
	{
		if (localeInfo == null)
		{
			return null;
		}
		Locale locale = getLocale(localeInfo);
		return locale.getDisplayName(new Locale("en"));
	}
	
}
