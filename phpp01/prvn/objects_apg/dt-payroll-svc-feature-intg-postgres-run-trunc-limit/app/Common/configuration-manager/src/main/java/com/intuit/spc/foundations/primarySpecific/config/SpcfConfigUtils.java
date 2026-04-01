package com.intuit.spc.foundations.primarySpecific.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;


/**
 * A Utility class to replace matching pattern ($$m.someValue$$) with the
 * value fetched from the configuration constants (key, value pairs)  
 * 
 * @author barunachalam
 */
public class SpcfConfigUtils
{
    /**
     * Cosntant Prefix
     */
    private static final String ConstantPrefix = "$$m.";

    /**
     * Constant Postfix
     */
    private static final String ConstantPostfix = "$$";

    /**
     * System Property Prefix
     */
    private static final String SystemPropertyPrefix = "${";

    /**
     * System Property Postfix
     */
    private static final String SystemPropertyPostfix = "}";

    /**
     * Dollar Sign Pattern
     */
    private static final Pattern DollarSignPattern = Pattern.compile("\\$");
    
    /**
     * Start Bracket Sign Pattern
     */
    private static final Pattern StartBracketSignPattern = Pattern.compile("\\{");
    
    /**
     * End Bracket Sign Pattern
     */
    private static final Pattern EndBracketSignPattern = Pattern.compile("\\}");
    
    /**
     * Constant Pattern
     */
    private static final Pattern ConstantPattern = Pattern.compile("\\$\\$m\\.[^$]+\\$\\$");
    
    /**
     * System Property Pattern
     */
    private static final Pattern SystemPropertyPattern = Pattern.compile("\\$\\{[^$]+\\}");
    
    /**
     * This method would replace the patterns ($$m.someKey$$) with the value corresponding
     * to the key "someKey" in the constants being passed. 
     * @param constants Key, Value Configuration
     * @param text Text which has to be parsed
     * @return expanded text, or null if text contains an invalid constant reference.
     */
    public static String expandConstant(ISpcfImmutableConfiguration constants, String text)
    {
        if (constants == null)
        {
            return text;
        }
        
        SpcfStringBuilder expandedValue = SpcfFactory.getInstance().createStringBuilder(text);
        Matcher matcher = ConstantPattern.matcher(text);
        while (matcher.find())
        {
            String matchedString = matcher.group();
            String constantKey = matchedString.substring(ConstantPrefix.length(), matchedString.length()
                    - ConstantPostfix.length());
            if (constants.containsKey(constantKey))
            {
                String constantValue = constants.getString(constantKey); 
                String strippedValue = constantValue.replaceAll("\\\\", "\\\\\\\\");
                // $ is treated specially by Matcher so must escape it
                strippedValue = escapeDollarSign(strippedValue);
                expandedValue.replaceAll(escapeDollarSign(matchedString), strippedValue);
            }
            else
            {
                return null;
            }
        }
        return expandedValue.toString();
    }
    
    /**
     * This method would replace the patterns (${someKey}) with the value corresponding
     * to the key "someKey" in the System Properties. 
     * This method will also recursively expand the keys if necessary (e.g. if the system property
     * value for someKey is ${someOtherKey}. 
     * @param text Text which has to be parsed
     * @return expanded text
     * @throws SpcfIllegalStateException If it's not able to resolve a system property or if it finds a circular reference in the system properties.
     */
    public static String expandSystemProperties(String text)
    {       
        //Recursively find all the matching strings and replace it with the system property value.
        String expandedValue = expandSystemProperties(text, SpcfFactory.getInstance().<String>createArrayList());

        return expandedValue.toString();
    }
    
    /**
     * This method would replace the patterns (${someKey}) with the value corresponding
     * to the key "someKey" in the System Properties. 
     * This method will also recursively expand the keys if necessary (e.g. if the system property
     * value for someKey is ${someOtherKey}. 
     * @param text Text which has to be parsed
     * @param processedList Nodes processed  (to find circular dependency)
     * @return expanded text
     * @throws SpcfIllegalStateException If it's not able to resolve a system property or if it finds a circular reference in the system properties.
     */
    private static String expandSystemProperties(String text, SpcfList<String> processedList)
    {       
        SpcfStringBuilder expandedValue = SpcfFactory.getInstance().createStringBuilder(text);
        
        //Finding the matching patterns
        Matcher matcher = SystemPropertyPattern.matcher(text);
        
        //For each matched string, do a depth first expansion until there is no more system property referred (recursively call the same method).
        while (matcher.find())
        {
            String matchedString = matcher.group();
            String propertyKey = matchedString.substring(SystemPropertyPrefix.length(), matchedString.length()
                    - SystemPropertyPostfix.length());
            
            //Check whether we have already visited this property. If so, then there is a circular reference.
            if(processedList.contains(propertyKey))     
                throw new SpcfIllegalStateException("Circular reference found in " +
                        "the System Properties used in the configuration file.");
            
            //If not, add it to the processed list so that we dont step into this same property again ending up in circular reference.
            processedList.add(propertyKey);
            
            if (System.getProperty(propertyKey) != null)
            {
                String propertyValue = System.getProperty(propertyKey);
                
                //Recursively calling to expand until we dont have any system properties referred.
                String expandedPropertyValue = expandSystemProperties(propertyValue, processedList);                
                
                propertyValue = expandedPropertyValue;
                String strippedValue = propertyValue.replaceAll("\\\\", "\\\\\\\\");
                
                //$, { & }are treated specially by Matcher so must escape it
                strippedValue = escapeDollarSign(strippedValue);
                strippedValue = escapeBracketSigns(strippedValue);
                String strippedMatchedString = escapeDollarSign(matchedString);
                strippedMatchedString = escapeBracketSigns(strippedMatchedString);
                
                //Replace all the occurance of the matched string with the expanded string.
                expandedValue.replaceAll(strippedMatchedString, strippedValue);
            }
            else
            {
                //Throw exception if not able to resolve the system property.
                throw new SpcfIllegalStateException("Cannot resolve the System Property - " + propertyKey);
            }
        }
        //Before backtrack to the previous property (node in a graph), remove the last processed property.
        if(processedList.getSize() >= 1)
            processedList.removeAt(processedList.getSize() - 1);
                
        return expandedValue.toString();
    }
    
    /**
     * Escape the dollar sign
     * @param string String
     * @return String after dollar sign escape
     */
    private static String escapeDollarSign(String string)
    {
        String escapedString = string;
        Matcher matcher = DollarSignPattern.matcher(string);
        if (matcher.find())
        {
            escapedString = matcher.replaceAll("\\\\\\$");
        }
        
        return escapedString;
    }
    
    /**
     * Escape the bracket signs
     * @param string String
     * @return String after bracket sign escape
     */
    private static String escapeBracketSigns(String string)
    {
        String escapedString = string;
        Matcher matcher = StartBracketSignPattern.matcher(string);
        if (matcher.find())
        {
            escapedString = matcher.replaceAll("\\\\\\{");
        }
        
        matcher = EndBracketSignPattern.matcher(escapedString);
        if (matcher.find())
        {
            escapedString = matcher.replaceAll("\\\\\\}");
        }
        
        return escapedString;
    }
}