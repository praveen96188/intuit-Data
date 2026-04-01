package com.intuit.spc.foundations.portability.text;

import com.intuit.spc.foundations.portabilitySpecific.text.SpcfLocaleInfoUtility;

/**
* The SpcfLocaleInfo encapsulates Java's Locale
* and C#'s CultureInfo.  The following are the supported language and/or country:  
* English, English (Australia), English (Canada), English (United Kingdom), English (United States)
* 
*/
public class SpcfLocaleInfo
{
	/**
	 * English Locale
	 */
	public static final SpcfLocaleInfo English = new SpcfLocaleInfo("en");
	
	/**
	 * EnglishUs Locale
	 */
	public static final SpcfLocaleInfo EnglishUs = new SpcfLocaleInfo("en","US");  
	
	/**
	 * EnglishEngland Locale
	 */
	public static final SpcfLocaleInfo EnglishEngland = new SpcfLocaleInfo("en","GB");  
	
	/**
	 * EnglishAustralia Locale
	 */
	public static final SpcfLocaleInfo EnglishAustralia = new SpcfLocaleInfo("en","AU");
	
	/**
	 * EnglishCanada Locale
	 */
	public static final SpcfLocaleInfo EnglishCanada = new SpcfLocaleInfo("en","CA");
	
	/**
	 * French Locale
	 */
	public static final SpcfLocaleInfo French = new SpcfLocaleInfo("fr");
	
	/**
	 * FrenchCanada Locale
	 */
	public static final SpcfLocaleInfo FrenchCanada = new SpcfLocaleInfo("fr","CA");
	
	/**
	 * Spanish Locale
	 */
	public static final SpcfLocaleInfo Spanish = new SpcfLocaleInfo("es");

	/**
	 * SpanishMexico Locale
	 */
	public static final SpcfLocaleInfo SpanishMexico = new SpcfLocaleInfo("es", "MX");

	
	
	/**
	 * String representing language as ISO 639
	 */
	protected String mLanguage;
	
	/**
	 * String representing country as ISO 3166 
	 */
	protected String mCountry;
	
	/**
	 * Return a locale info language and country. Country can be null.
	 * @param language 2 lower case letters as ISO 639 of language
	 * @param country 2 upper case letters as ISO 3166 of country (can be null)
	 * @return SpcfLocaleInfo object if combination of language and country 
	 * is supported by Spcf, or null otherwise
	 */
	public static SpcfLocaleInfo getLocaleInfoFromString(String language, String country )
	{
		if (language == null)
		{
			return null;
		}
		String localeString = language;
		if (country != null)
		{
			localeString += "-" + country;
		}
		
		if (localeString.equals(English.toString()))
		{
			return English;
		} 
		else if (localeString.equals(EnglishUs.toString()))
		{
			return EnglishUs;
		} 
		else if (localeString.equals(EnglishEngland.toString()))
		{
			return EnglishEngland;
		} 
		else if (localeString.equals(EnglishAustralia.toString()))
		{
			return EnglishAustralia;
		} 
		else if (localeString.equals(EnglishCanada.toString()))
		{
			return EnglishCanada;
		}
		else if (localeString.equals(French.toString()))
		{
			return French;
		}
		else if (localeString.equals(FrenchCanada.toString()))
		{
			return FrenchCanada;
		}
		else if (localeString.equals(Spanish.toString()))
		{
			return Spanish;
		}
		else if (localeString.equals(SpanishMexico.toString()))
		{
			return SpanishMexico;
		}
		return null;
	}
	
	/**
	 * Private ctor 
	 * @param language string representing language as ISO 639
	 */
	private SpcfLocaleInfo(String language)
	{
		setLocaleInfo(language, null);
	}
	
	/**
	 * Private ctor
	 * @param language string representing language as ISO 639
	 * @param country string representing country as ISO 3166 
	 */
	private SpcfLocaleInfo(String language, String country)
	{
		setLocaleInfo(language, country);
	}
	
	/**
	 * Returns language in 2 lower case letters as ISO 639 
	 * @return language
	 */
	public String getLanguage()
	{
		return mLanguage;
	}
    
    public void setLocaleInfo(String language, String country)
    {
        mLanguage = language;
        mCountry = country;
    }
	
	/**
	 * Returns country in 2 upper case letters as ISO 3166 
	 * @return country or null if country is null or not set.
	 */
	public String getCountry()
	{
		return mCountry;
	}
	
	/**
	 * Returns SpcfLocaleInfo for the default country and language.
	 * @return SpcfLocaleInfo instance
	 */
	public static SpcfLocaleInfo getDefault()
	{
		String language;
		String country;
		language = SpcfLocaleInfoUtility.getDefaultLanguage();
		country = SpcfLocaleInfoUtility.getDefaultCountry();
		
		// SF000655 - return en-US if OS fails to return proper default
		if (language == null || country == null)
		{
			return SpcfLocaleInfo.EnglishUs;
		}
		
		if (language.length() == 0 || country.length() == 0)
		{
			return SpcfLocaleInfo.EnglishUs;
		}
		
		return new SpcfLocaleInfo(language, country);
	}
	
      /**
	 * Returns display name as language (country).  
	 * If there is no 'country', returns the display name as language
	 * @return English display name 
	 */
	public String getDisplayName()
	{
		return com.intuit.spc.foundations.portabilitySpecific.text.SpcfLocaleInfoUtility.getDisplayName(this);
	}
	
	/**
	 * Returns language-country or just language if country is null.
	 * @return the 2 letter language code if country is null or 
	 * the 2 letter language code followed by '-' followed by the 2 letter country code
	 */
	@Override
	public String toString()
	{
		if (mCountry == null)
		{
			return mLanguage;
		}
			
		return mLanguage + "-" + mCountry;
	}
}
